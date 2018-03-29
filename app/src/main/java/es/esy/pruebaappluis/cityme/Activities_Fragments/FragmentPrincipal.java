package es.esy.pruebaappluis.cityme.Activities_Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import es.esy.pruebaappluis.cityme.MapsUtilities.PlacesAPI.PlaceAPI;
import es.esy.pruebaappluis.cityme.MapsUtilities.PlacesAPI.PlacesAutoCompleteAdapter;
import es.esy.pruebaappluis.cityme.Modelo.CompararEventosPorCercanía;
import es.esy.pruebaappluis.cityme.Modelo.CompararEventosPorFechas;
import es.esy.pruebaappluis.cityme.Modelo.Evento;
import es.esy.pruebaappluis.cityme.Modelo.Usuario;
import es.esy.pruebaappluis.cityme.R;
import es.esy.pruebaappluis.cityme.RecyclerViewUtilities.RecyclerEventosAdapter_Principal;
import es.esy.pruebaappluis.cityme.RecyclerViewUtilities.RecyclerItemClickListener;
import es.esy.pruebaappluis.cityme.Utilities.HidingScrollListener_Principal;
import es.esy.pruebaappluis.cityme.Volley.PeticionesServidorVolley;


/**
 * Gestiona la búsqueda de eventos realizadas por el usuario.
 * <p>
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentPrincipal.OnFragmentInteractionListener} interface
 * to handle interaction events.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class FragmentPrincipal extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        SeekBar.OnSeekBarChangeListener, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private OnFragmentInteractionListener mListener;

    private static String TAG = FragmentPrincipal.class.getSimpleName();
    private RecyclerView recyclerView;

    ArrayList<Evento> listaEventos = new ArrayList<>();

    private TextView radarText;
    private SeekBar radar;
    int step = 1;
    int max = 150;
    int min = 1;
    int valorRadar;
    boolean flagRadar = false;
    RelativeLayout barRadar;

    private AutoCompleteTextView autoCompleteTextViewOrigen;
    private ImageButton btnFind;
    private ImageButton btnmyLocation;

    //constante para solicitar localización actual
    private static final int PETICION_PERMISO_LOCALIZACION = 101;
    private static final int PETICION_CONFIG_UBICACION = 201;

    private GoogleApiClient apiClient;
    private boolean flagClientGoogleApi = false;
    private LocationRequest locRequest;
    private Location mLastLocation;

    private SwipeRefreshLayout swipeRefreshLayout;

    private CheckBox checkBoxFecha;
    private CheckBox checkBoxDistancia;
    private boolean checkFecha_Distancia;

    private TextView sinDatos;

    boolean cargarDatosInicial = false;

    RecyclerEventosAdapter_Principal adapter;

    private SimpleDateFormat formateadorFecha = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    public FragmentPrincipal() {
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
        final View view = inflater.inflate(R.layout.fragment_principal, container, false);

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

        radarText = (TextView) view.findViewById(R.id.textoRadar);
        radar = (SeekBar) view.findViewById(R.id.seekBarRadar);

        radar.setOnSeekBarChangeListener(this);
        radar.setMax((max - min) / step);

        btnFind = (ImageButton) view.findViewById(R.id.btnFind);
        btnmyLocation = (ImageButton) view.findViewById(R.id.btnMyLocation);
        autoCompleteTextViewOrigen = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextViewOrigen);
        autoCompleteTextViewOrigen.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    refreshRecyclerView();
                    return true;
                }
                return false;
            }
        });
        autoCompleteTextViewOrigen.setAdapter(new PlacesAutoCompleteAdapter(getContext(), R.layout.list_item_autocomplete));
        autoCompleteTextViewOrigen.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                refreshRecyclerView();

            }
        });

        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                refreshRecyclerView();
            }
        });

        btnmyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                enableLocationUpdates();

            }
        });

        barRadar = (RelativeLayout) view.findViewById(R.id.barRadar);

        checkFecha_Distancia = true;
        checkBoxFecha = (CheckBox) view.findViewById(R.id.checkBoxFecha);
        checkBoxFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBoxDistancia.setChecked(false);
                checkBoxFecha.setChecked(true);
                checkFecha_Distancia = true;
                refreshRecyclerView();
            }
        });
        checkBoxDistancia = (CheckBox) view.findViewById(R.id.checkBoxDistancia);
        checkBoxDistancia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBoxFecha.setChecked(false);
                checkBoxDistancia.setChecked(true);
                checkFecha_Distancia = false;
                refreshRecyclerView();
            }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        adapter = new RecyclerEventosAdapter_Principal(listaEventos);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isPositionHeader(position) ? gridLayoutManager.getSpanCount() : 1;
            }
        });
        recyclerView.addOnScrollListener(new HidingScrollListener_Principal() {
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
                    @Override
                    public void onItemClick(View view, int position) {
                        // TODO Handle item click
                        if (position != 0) {
                            Evento evento = listaEventos.get(position - 1);
                            MainActivity.evento = evento;
                            Intent intent = new Intent(getActivity(), DescripcionEvento.class);
                            getActivity().startActivity(intent);
                        }
                    }
                })
        );

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            swipeRefreshLayout.setProgressViewOffset(false, 0, 500);
        }

        sinDatos = (TextView) view.findViewById(R.id.sinDatos);
        sinDatos.setVisibility(View.INVISIBLE);

        if (cargarDatosInicial == true) {
            if (listaEventos.size() == 0) {
                sinDatos.setVisibility(View.VISIBLE);
            } else {
                sinDatos.setVisibility(View.INVISIBLE);
            }
        }

        if (flagClientGoogleApi == false) {
            buildGoogleApiClient();
            flagClientGoogleApi = true;
        }

        return view;
    }

    /**
     * Encargado de ocultar el buscador cuando se desliza la pantalla.
     */
    private void hideViews() {
        barRadar.animate().translationY(-barRadar.getHeight()).setInterpolator(new AccelerateInterpolator(1));
        barRadar.setVisibility(View.INVISIBLE);

    }

    /**
     * Encargado de mostrar el buscador cuando se desliza la pantalla.
     */
    private void showViews() {
        barRadar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(1));
        barRadar.setVisibility(View.VISIBLE);

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

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    /**
     * Crea un cliente Google API, que permite el acceso a los servicios de
     * Google Play Services, mediante el cual se podrá determinar
     * la localicación actual del dispositivo.
     */
    protected synchronized void buildGoogleApiClient() {

        //Construcción cliente API Google
        apiClient = new GoogleApiClient.Builder(getContext())
                .enableAutoManage(getActivity(), this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Comprueba si se tiene la configuración necesaria para actualizar los datos
     * de localización. En caso de ser así inicia la actualización.
     */
    private void enableLocationUpdates() {

        locRequest = new LocationRequest();
        locRequest.setInterval(2000);
        locRequest.setFastestInterval(1000);
        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest locSettingsRequest =
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(locRequest)
                        .build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        apiClient, locSettingsRequest);

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:

                        Log.i(TAG, "Configuración correcta");
                        startLocationUpdates();

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            Log.i(TAG, "Se requiere actuación del usuario");
                            status.startResolutionForResult(getActivity(), PETICION_CONFIG_UBICACION);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "Error al intentar solucionar configuración de ubicación");
                        }

                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "No se puede cumplir la configuración de ubicación necesaria");
                        break;
                }
            }
        });
    }

    /**
     * Deshabilita la actualización de localización.
     */
    private void disableLocationUpdates() {

        LocationServices.FusedLocationApi.removeLocationUpdates(
                apiClient, this);

    }

    /**
     * Inicia la actualización de localización y se comienza a recibir
     * la ubicación actual del dispositivo.
     */
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Log.i(TAG, "Inicio de recepción de ubicaciones");

            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locRequest, this);
            btnmyLocation.setEnabled(true);

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Mostrar diálogo explicativo
            } else {
                // Solicitar permiso
                ActivityCompat.requestPermissions(
                        getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PETICION_PERMISO_LOCALIZACION);
            }
        }
    }

    /**
     * Es llamado cuando la solicitud de conexión al cliente de Google API
     * resulta fallida.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        //Se ha producido un error que no se puede resolver automáticamente
        //y la conexión con los Google Play Services no se ha establecido.

        Log.e(TAG, "Error grave al conectar con Google Play Services");
    }

    /**
     * Es llamado cuando la solicitud de conexión al cliente de Google API
     * se realiza con éxito.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Conectado correctamente a Google Play Services

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PETICION_PERMISO_LOCALIZACION);
        } else {
            if (cargarDatosInicial == false) {
                enableLocationUpdates();
            }
        }
    }

    /**
     * Es llamado cuando la conexión del cliente de Google API
     * es suspendida.
     */
    @Override
    public void onConnectionSuspended(int i) {
        //Se ha interrumpido la conexión con Google Play Services

        Log.e(TAG, "Se ha interrumpido la conexión con Google Play Services");
    }

    /**
     * Gestiona el resultado de la petición de permisos, en este caso,
     * de los permisos de localización.
     *
     * @param requestCode permite identificar de dónde procede el resultado
     * @param permissions permisos solicitados
     * @param grantResults indica si el permiso ha sido concedido o denegado
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PETICION_PERMISO_LOCALIZACION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Permiso concedido
                btnmyLocation.setEnabled(true);

                @SuppressWarnings("MissingPermission")
                Location lastLocation =
                        LocationServices.FusedLocationApi.getLastLocation(apiClient);

                mLastLocation = lastLocation;

                enableLocationUpdates();


            } else {
                //Permiso denegado:
                autoCompleteTextViewOrigen.setText("Madrid");
                refreshRecyclerView();
                Log.e(TAG, "Permiso denegado");
            }
        }
    }

    /**
     * Procesa los resultados obtenidos tras acceder a otras Actividades,
     * como son de la configuración de la ubicación.
     *
     * @param requestCode permite identificar de qué Actividad procede el resultado
     * @param resultCode código de resultado devuelto por la otra Actividad
     * @param data puede contener datos devueltos por la Actividad
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PETICION_CONFIG_UBICACION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        btnmyLocation.setEnabled(true);
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "El usuario no ha realizado los cambios de configuración necesarios");
                        break;
                }
                break;
        }
    }

    /**
     * Es llamado cuando se conoce una nueva ubicación del dispositivo.
     * @param location nueva ubicación
     */
    @Override
    public void onLocationChanged(Location location) {

        Log.i(TAG, "Recibida nueva ubicación!");

        //Mostramos la nueva ubicación recibida
        mLastLocation = location;

        if (mLastLocation != null) {

            autoCompleteTextViewOrigen.setText(PlaceAPI.getCompleteAddressString(mLastLocation.getLatitude(), mLastLocation.getLongitude(), getContext()));
            refreshRecyclerView();
        } else {

            Toast.makeText(getContext(), R.string.ubicacion_desconocida, Toast.LENGTH_SHORT).show();
        }

        disableLocationUpdates();
    }


    /**
     * Es llamado cuando se realiza el gesto de deslizar sobre la pantalla.
     * Por tanto, se actualizan los datos correspondiente.
     */
    @Override
    public void onRefresh() {

        solicitudEventosRadar();
    }

    /**
     * Llama al método onRefresh para actualizar los datos correspondiente de la
     * misma manera que se haría si se realiza el gesto de deslizar sobre la
     * pantalla.
     */
    public void refreshRecyclerView() {
        this.onRefresh();
    }

    /**
     * Gestiona el progreso del seekbar cuando el usuario está realizando un gesto
     * táctil sobre el mismo.
     *
     * @param seekBar
     * @param i progreso actual
     * @param b verdadero si el progreso fue iniciado por el usuario
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        valorRadar = min + (i * step);
        radarText.setText(getText(R.string.en) + " " + valorRadar + " km");

    }

    /**
     * Notificación de que el usuario ha iniciado un gesto táctil sobre el seekbar.
     *
     * @param seekBar
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    /**
     * Notificación de que el usuario ha terminado un gesto táctil sobre el seekbar.
     *
     * @param seekBar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (flagRadar == false) {
            flagRadar = true;
            refreshRecyclerView();
        } else {
            refreshRecyclerView();
        }
    }


    /**
     * Realiza la petición GET al servicio REST mediante la cual se obtienen los eventos
     * que cumplen las condiciones del radar.
     */
    private void solicitudEventosRadar() {

        PeticionesServidorVolley.getInstance().getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {

                return true;
            }
        });

        String uriTodosEventos = "http://pruebaappluis.esy.es/eventos";

        if (!PeticionesServidorVolley.compruebaConexion(getContext())) {
            Toast.makeText(getContext(), R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }


        if (TextUtils.isEmpty(autoCompleteTextViewOrigen.getText().toString())) {
            Toast.makeText(getContext(), R.string.introduzca_localizacion_origen, Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }


        swipeRefreshLayout.setRefreshing(true);

        final LatLng coordenadas = PlaceAPI.getCoordenadasFromAddress(autoCompleteTextViewOrigen.getText().toString(), getContext());
        if (coordenadas == null) {
            Toast.makeText(getContext(), R.string.reintentelo_de_nuevo, Toast.LENGTH_SHORT).show();
            return;
        }
        String uri = uriTodosEventos + "/radar/" + coordenadas.latitude + "/" + coordenadas.longitude + "/" + valorRadar;

        // PETICIÓN GET
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                uri, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {

                    if (response.getInt("estado") == 1) { // EXITO

                        listaEventos.clear();

                        JSONArray datos = response.getJSONArray("datos");

                        if (datos.length() == 0) {
                            sinDatos.setVisibility(View.VISIBLE);
                        } else {
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

                        if (checkFecha_Distancia == true) { // Ordenar por fecha

                            Comparator comparator = new CompararEventosPorFechas();
                            Collections.sort(listaEventos, comparator);
                        } else { // Ordenar por distancia

                            Comparator comparator = new CompararEventosPorCercanía(coordenadas.latitude, coordenadas.longitude);
                            Collections.sort(listaEventos, comparator);
                        }

                        adapter = new RecyclerEventosAdapter_Principal(listaEventos);

                        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.setAdapter(adapter);
                        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                            @Override
                            public int getSpanSize(int position) {
                                return adapter.isPositionHeader(position) ? gridLayoutManager.getSpanCount() : 1;
                            }
                        });

                        cargarDatosInicial = true;

                    } else { // ERROR API
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

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }
}
