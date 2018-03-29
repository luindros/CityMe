package es.esy.pruebaappluis.cityme.Activities_Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
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

import java.util.ArrayList;

import es.esy.pruebaappluis.cityme.Modelo.Promocion;
import es.esy.pruebaappluis.cityme.Modelo.Usuario;
import es.esy.pruebaappluis.cityme.R;
import es.esy.pruebaappluis.cityme.RecyclerViewUtilities.RecyclerItemClickListener;
import es.esy.pruebaappluis.cityme.RecyclerViewUtilities.RecyclerPromocionesAdapter_MisPromociones;
import es.esy.pruebaappluis.cityme.Utilities.HidingScrollListener_EventosCreados_MisPromociones;
import es.esy.pruebaappluis.cityme.Volley.PeticionesServidorVolley;


/**
 * Gestiona las promociones creadas por el usuario.
 * <p>
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentMisPromociones.OnFragmentInteractionListener} interface
 * to handle interaction events.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class FragmentMisPromociones extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private OnFragmentInteractionListener mListener;

    private static String TAG = FragmentMisPromociones.class.getSimpleName();
    private RecyclerView recyclerView;

    ArrayList<Promocion> listaPromociones = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    private FloatingActionButton botonNuevaPromocion;

    private TextView sinDatos;

    boolean cargarDatosInicial = false;

    public FragmentMisPromociones() {
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
        final View view = inflater.inflate(R.layout.fragment_mis_promociones, container, false);

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

        botonNuevaPromocion = (FloatingActionButton) view.findViewById(R.id.botonNuevaPromocion);
        botonNuevaPromocion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.tipoPromocion = "crear";
                Intent intent = new Intent(getActivity(),CrearEditarPromocion.class);
                getActivity().startActivity(intent);
            }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        RecyclerPromocionesAdapter_MisPromociones adapter = new RecyclerPromocionesAdapter_MisPromociones(listaPromociones);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new HidingScrollListener_EventosCreados_MisPromociones() {
            @Override
            public void onHide() {
                hideViews();
            }

            @Override
            public void onShow() {
                showViews();
            }
        });
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Promocion promocion = listaPromociones.get(position);
                        MainActivity.promocion = promocion;
                        Intent intent = new Intent(getActivity(),DescripcionPromocion.class);
                        getActivity().startActivity(intent);
                    }
                })
        );

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        sinDatos = (TextView) view.findViewById(R.id.sinDatos);
        sinDatos.setVisibility(View.INVISIBLE);

        if(cargarDatosInicial == false) {

            refreshRecyclerView();

        }

        if(cargarDatosInicial == true) {
            if(listaPromociones.size() == 0) {
                sinDatos.setVisibility(View.VISIBLE);
            }else {
                sinDatos.setVisibility(View.INVISIBLE);
            }
        }


        return view;
    }

    /**
     * Encargado de ocultar el botón de añadir una nueva promoción cuando se desliza la pantalla.
     */
    private void hideViews() {

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) botonNuevaPromocion.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        botonNuevaPromocion.animate().translationY(botonNuevaPromocion.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(1)).start();

    }

    /**
     * Encargado de mostrar el botón de añadir una nueva promoción cuando se desliza la pantalla.
     */
    private void showViews() {

        botonNuevaPromocion.animate().translationY(0).setInterpolator(new DecelerateInterpolator(1)).start();

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
        solicitudPromocionesCreadas();
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
     * Realiza la petición GET al servicio REST mediante la cual se obtienen las promociones
     * creadas por el usuario.
     */
    private void solicitudPromocionesCreadas() {

        String uri = "http://pruebaappluis.esy.es/promociones/usuario/" + MainActivity.usuario.getId();

        if (!PeticionesServidorVolley.compruebaConexion(getContext())) {
            Toast.makeText(getContext(),R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        swipeRefreshLayout.setRefreshing(true);

        // PETICIÓN GET
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                uri, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {

                    if(response.getInt("estado") == 1) { // EXITO

                        listaPromociones.clear();

                        JSONArray datos = response.getJSONArray("datos");

                        if(datos.length() == 0) {
                            sinDatos.setVisibility(View.VISIBLE);
                        }else {
                            sinDatos.setVisibility(View.INVISIBLE);
                        }

                        for (int i = 0; i < datos.length(); i++) {
                            JSONObject c = datos.getJSONObject(i);
                            String id_promocion = c.getString("id_promocion");
                            String nombre_promocion = c.getString("nombre_promocion");
                            String descripcion_promocion = c.getString("descripcion_promocion");
                            String imagen_promocion = c.getString("imagen_promocion");

                            String id_usuario = c.getString("id_usuario");
                            String nombre_usuario = c.getString("nombre_usuario");
                            String email_usuario = c.getString("email_usuario");
                            String link_usuario = c.getString("link_usuario");
                            String token_usuario = c.getString("token_usuario");
                            String imagenUrl_usuario = c.getString("imagenUrl_usuario");
                            Usuario usuario = new Usuario(id_usuario, nombre_usuario, email_usuario, link_usuario, token_usuario, imagenUrl_usuario);

                            Promocion promocion = new Promocion(id_promocion, nombre_promocion, descripcion_promocion, usuario);
                            promocion.setImagen(imagen_promocion);


                            listaPromociones.add(promocion);
                        }

                        RecyclerPromocionesAdapter_MisPromociones adapter = new RecyclerPromocionesAdapter_MisPromociones(listaPromociones);
                        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),1);
                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.setAdapter(adapter);

                        cargarDatosInicial = true;

                    }
                    else { // ERROR API
                        Toast.makeText(getContext(), response.getString("datos"), Toast.LENGTH_SHORT).show();

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
                Toast.makeText(getContext(), R.string.recargue_de_nuevo, Toast.LENGTH_SHORT).show();

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }
}
