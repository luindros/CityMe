package es.esy.pruebaappluis.cityme.Activities_Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import es.esy.pruebaappluis.cityme.Modelo.CompararEventosPorFechas;
import es.esy.pruebaappluis.cityme.Modelo.Evento;
import es.esy.pruebaappluis.cityme.Modelo.Usuario;
import es.esy.pruebaappluis.cityme.R;
import es.esy.pruebaappluis.cityme.RecyclerViewUtilities.RecyclerEventosAdapter_EventosAsistidos;
import es.esy.pruebaappluis.cityme.RecyclerViewUtilities.RecyclerItemClickListener;
import es.esy.pruebaappluis.cityme.Volley.PeticionesServidorVolley;


/**
 * Gestiona el calendario de eventos a los que asiste el usuario.
 * <p>
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentCalendario.OnFragmentInteractionListener} interface
 * to handle interaction events.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class FragmentCalendario extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private OnFragmentInteractionListener mListener;

    private static String TAG = FragmentCalendario.class.getSimpleName();
    protected RecyclerView recyclerView;
    ArrayList<Evento> listaEventos = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView sinDatos;

    boolean cargarDatosInicial = false;

    private SimpleDateFormat formateadorFecha = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    public FragmentCalendario() {
        // Required empty public constructor
    }


    /**
     * Es llamado cuando el fragment se está iniciando.
     * Contiene la inicialización de todos los elementos del fragment.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_calendario, container, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if(keyEvent.getAction()==KeyEvent.ACTION_DOWN){
                    if(keyCode==KeyEvent.KEYCODE_BACK){
                        Intent intentHome = new Intent(Intent.ACTION_MAIN);
                        intentHome.addCategory(Intent.CATEGORY_HOME);
                        intentHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentHome);
                        return true;
                    }
                }
                return false;
            }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        final RecyclerEventosAdapter_EventosAsistidos adapter = new RecyclerEventosAdapter_EventosAsistidos(listaEventos);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),4);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // TODO Handle item click
                        Evento evento = listaEventos.get(position);
                        MainActivity.evento = evento;
                        Intent intent = new Intent(getActivity(),DescripcionEvento.class);
                        getActivity().startActivity(intent);
                    }
                })
        );

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        sinDatos = (TextView) view.findViewById(R.id.sinDatos);
        sinDatos.setVisibility(View.INVISIBLE);

        if(cargarDatosInicial == true) {
            if(listaEventos.size() == 0) {
                sinDatos.setVisibility(View.VISIBLE);
            }else {
                sinDatos.setVisibility(View.INVISIBLE);
            }
        }

        return view;
    }


    /**
     * Gestiona si la interfaz de usuario de este fragment es actualmente visible para
     * el usuario.
     *
     * @param isVisibleToUser indica si es visible o no
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && cargarDatosInicial == false){
            refreshRecyclerView();
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Es llamado cuando se realiza el gesto de deslizar sobre la pantalla.
     * Por tanto, se actualizan los datos correspondiente.
     */
    @Override
    public void onRefresh() {
        solicitudEventosAsistidos();
    }

    /**
     * Llama al método onRefresh para actualizar los datos correspondiente de la
     * misma manera que se haría si se realiza el gesto de deslizar sobre la
     * pantalla.
     */
    public void refreshRecyclerView(){
        this.onRefresh();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Realiza la petición GET al servicio REST mediante la cual se obtienen los eventos
     * a los que asiste el usuario.
     */
    private void solicitudEventosAsistidos() {

        String uriEventosAsistidos = "http://pruebaappluis.esy.es/asistentes/usuario/" + MainActivity.usuario.getId();

        if (!PeticionesServidorVolley.compruebaConexion(getContext())) {
            Toast.makeText(getContext(),R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        swipeRefreshLayout.setRefreshing(true);

        // PETICIÓN GET
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                uriEventosAsistidos, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {

                    if(response.getInt("estado") == 1) { // EXITO

                        listaEventos.clear();

                        JSONArray datos = response.getJSONArray("datos");

                        if(datos.length() == 0) {
                            sinDatos.setVisibility(View.VISIBLE);
                        }else {
                            sinDatos.setVisibility(View.INVISIBLE);
                        }

                        for (int i = 0; i < datos.length(); i++) {
                            JSONObject c = datos.getJSONObject(i);

                            String fecha_fin_evento = c.getString("fecha_fin_evento");

                            Date dateFechaFin = null;
                            Date dateActual = null;
                            try {
                                dateFechaFin = formateadorFecha.parse(fecha_fin_evento);
                                dateActual = formateadorFecha.parse(formateadorFecha.format(new Date()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            if (dateFechaFin.after(dateActual)) {
                                String id_evento = c.getString("id_evento");
                                String nombre_evento = c.getString("nombre_evento");
                                String descripcion_evento = c.getString("descripcion_evento");
                                String ciudad_evento = c.getString("ciudad_evento");
                                String imagen_evento = c.getString("imagen_evento");
                                double latitud_evento = c.getDouble("latitud_evento");
                                double longitud_evento = c.getDouble("longitud_evento");
                                String lugar_evento = c.getString("lugar_evento");
                                String fecha_inicio_evento = c.getString("fecha_inicio_evento");


                                String id_usuario = c.getString("id_usuario");
                                String nombre_usuario = c.getString("nombre_usuario");
                                String email_usuario = c.getString("email_usuario");
                                String link_usuario = c.getString("link_usuario");
                                String token_usuario = c.getString("token_usuario");
                                String imagenUrl_usuario = c.getString("imagenUrl_usuario");
                                Usuario usuario = new Usuario(id_usuario, nombre_usuario, email_usuario, link_usuario, token_usuario, imagenUrl_usuario);

                                Evento evento = new Evento(id_evento, nombre_evento, descripcion_evento, ciudad_evento, latitud_evento, longitud_evento, lugar_evento, usuario, fecha_inicio_evento, fecha_fin_evento);
                                evento.setImagen(imagen_evento);


                                listaEventos.add(evento);
                            }
                        }

                        Comparator comparator = new CompararEventosPorFechas();
                        Collections.sort(listaEventos,comparator);

                        final RecyclerEventosAdapter_EventosAsistidos adapter = new RecyclerEventosAdapter_EventosAsistidos(listaEventos);
                        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),4);
                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.setAdapter(adapter);

                        cargarDatosInicial = true;

                    }
                    else { // ERROR API
                        Toast.makeText(getContext(),response.getString("datos"), Toast.LENGTH_SHORT).show();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                swipeRefreshLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getContext(),R.string.recargue_de_nuevo, Toast.LENGTH_SHORT).show();


                swipeRefreshLayout.setRefreshing(false);
            }
        });

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }

}
