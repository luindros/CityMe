package es.esy.pruebaappluis.cityme.Activities_Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;
import es.esy.pruebaappluis.cityme.R;
import es.esy.pruebaappluis.cityme.Volley.PeticionesServidorVolley;


/**
 * Gestiona la pantalla de configuración del usuario.
 * <p>
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentConfiguracion.OnFragmentInteractionListener} interface
 * to handle interaction events.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class FragmentConfiguracion extends Fragment {

    private OnFragmentInteractionListener mListener;

    private static String TAG = FragmentConfiguracion.class.getSimpleName();

    private CircleImageView imagenUsuario;
    private TextView nombreUsuario;

    private RelativeLayout salir;
    private TextView baja;

    public FragmentConfiguracion() {
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
        final View view = inflater.inflate(R.layout.fragment_configuracion, container, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
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

        imagenUsuario = (CircleImageView) view.findViewById(R.id.imagen);
        imagenUsuario.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("fb://facewebmodal/f?href="+MainActivity.usuario.getLink()); //desde Facebook
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
            }
        });
        nombreUsuario = (TextView) view.findViewById(R.id.nombre);

        salir = (RelativeLayout) view.findViewById(R.id.salir);
        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();

                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }
        });

        baja = (TextView) view.findViewById(R.id.baja);
        baja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!PeticionesServidorVolley.compruebaConexion(getContext())) {
                    Toast.makeText(getContext(), R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.eliminar_cuenta);
                builder.setMessage(R.string.desea_eliminar_cuenta);
                builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        solicitudBaja();
                    }
                });
                builder.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });


                AlertDialog dialog = builder.create();
                dialog.show();

                TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                textView.setTextSize(12);

                dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.rgb(97,97,97));


            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Picasso.with(getContext()).load(MainActivity.usuario.getImagenUrl()).into(imagenUsuario);
        nombreUsuario.setText(MainActivity.usuario.getNombre());
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
     * Realiza la petición DELETE al servicio REST indicando la baja del usuario.
     */
    private void solicitudBaja(){

        if (!PeticionesServidorVolley.compruebaConexion(getContext())) {
            Toast.makeText(getContext(), R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        String uri = "http://pruebaappluis.esy.es/usuarios/" + MainActivity.usuario.getId();

        // PETICIÓN DELETE
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.DELETE,
                uri, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {
                    if(response.getInt("estado") == 1) { //EXITO
                        LoginManager.getInstance().logOut();
                        getActivity().finish();
                    }
                    else { // ERROR API
                        Toast.makeText(getContext(),
                                response.getString("datos"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getContext(),
                        R.string.error_conexion, Toast.LENGTH_SHORT).show();
            }
        });

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);
    }
}
