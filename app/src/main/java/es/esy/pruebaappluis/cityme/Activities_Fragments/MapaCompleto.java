package es.esy.pruebaappluis.cityme.Activities_Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import es.esy.pruebaappluis.cityme.MapsUtilities.DirectionsAPI.DirectionFinder;
import es.esy.pruebaappluis.cityme.MapsUtilities.DirectionsAPI.DirectionFinderListener;
import es.esy.pruebaappluis.cityme.MapsUtilities.DirectionsAPI.Route;
import es.esy.pruebaappluis.cityme.MapsUtilities.PlacesAPI.PlaceAPI;
import es.esy.pruebaappluis.cityme.MapsUtilities.PlacesAPI.PlacesAutoCompleteAdapter;
import es.esy.pruebaappluis.cityme.R;

/**
 * Gestiona el mapa que muestra la ubicación de un evento.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class MapaCompleto extends AppCompatActivity implements OnMapReadyCallback, DirectionFinderListener,
        PopupMenu.OnMenuItemClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private ImageButton btnFindPath;
    private ImageView btnDriving;
    private ImageView btnWalking;
    private TextView durationDriving;
    private TextView distaceDriving;
    private TextView durationWalking;
    private TextView distaceWalking;

    private AutoCompleteTextView autoCompleteTextViewOrigen;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private ImageButton btnTipoMapa;
    private ImageButton btnmyLocation;
    private ImageButton btnOcultarComoLlegar;
    private Button btnComoLlegar;
    private RelativeLayout relativeLayoutComoLlegar;

    //constante para solicitar localización actual
    private static final int PETICION_PERMISO_LOCALIZACION = 101;
    private static final int PETICION_CONFIG_UBICACION = 201;

    private GoogleApiClient apiClient;
    private boolean flagClientGoogleApi = false;
    private LocationRequest locRequest;
    private Location mLastLocation;

    private static String TAG = MapaCompleto.class.getSimpleName();

    double latitudEvento;
    double longitudEvento;

    View view;

    /**
     * Es llamado cuando la actividad se está iniciando.
     * Contiene la inicialización de todos los elementos de la Actividad.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_completo);

        // Inflate the layout for this fragment
        View view = getLayoutInflater().inflate(R.layout.activity_mapa_completo, null);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        finish();
                        return true;
                    }
                }
                return false;
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.ubicacion_del_evento);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(this);
        } else {

            mMap.clear();
            LatLng coordenadas = new LatLng(latitudEvento, longitudEvento);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenadas, 18));
            originMarkers.clear();
            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(PlaceAPI.getCompleteAddressString(coordenadas.latitude, coordenadas.longitude, this))
                    .position(coordenadas)));
            originMarkers.get(0).showInfoWindow();

        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // R.id.map is a layout
        transaction.replace(R.id.map, mapFragment).commit();

        btnFindPath = (ImageButton) findViewById(R.id.btnFind);
        btnDriving = (ImageView) findViewById(R.id.btnDriving);
        btnWalking = (ImageView) findViewById(R.id.btnWalking);
        durationDriving = (TextView) findViewById(R.id.tvDurationDriving);
        distaceDriving = (TextView) findViewById(R.id.tvDistanceDriving);
        durationWalking = (TextView) findViewById(R.id.tvDurationWalking);
        distaceWalking = (TextView) findViewById(R.id.tvDistanceWalking);

        btnTipoMapa = (ImageButton) findViewById(R.id.btntipoMapa);
        btnmyLocation = (ImageButton) findViewById(R.id.btnMyLocation);
        btnOcultarComoLlegar = (ImageButton) findViewById(R.id.btnhide);
        btnComoLlegar = (Button) findViewById(R.id.btnComoLlegar);
        relativeLayoutComoLlegar = (RelativeLayout) findViewById(R.id.relativelayoutComoLlegar);
        autoCompleteTextViewOrigen = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewOrigen);
        autoCompleteTextViewOrigen.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item_autocomplete));
        autoCompleteTextViewOrigen.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);

                durationDriving.setText("");
                distaceDriving.setText("");
                durationWalking.setText("");
                distaceWalking.setText("");
                sendRequestFindPath(0);

            }
        });

        relativeLayoutComoLlegar.setVisibility(View.INVISIBLE);

        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);

                durationDriving.setText("");
                distaceDriving.setText("");
                durationWalking.setText("");
                distaceWalking.setText("");
                sendRequestFindPath(0);
            }
        });
        btnDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequestFindPath(0);
            }
        });
        btnWalking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequestFindPath(1);
            }
        });
        btnTipoMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MapaCompleto.this, v);
                popupMenu.setOnMenuItemClickListener(MapaCompleto.this);
                popupMenu.inflate(R.menu.popup_menu_maps);
                popupMenu.show();
            }
        });
        btnmyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableLocationUpdates();
            }
        });
        btnOcultarComoLlegar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayoutComoLlegar.setVisibility(View.INVISIBLE);
                btnComoLlegar.setVisibility(View.VISIBLE);
            }
        });
        btnComoLlegar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayoutComoLlegar.setVisibility(View.VISIBLE);
                btnComoLlegar.setVisibility(View.INVISIBLE);

            }
        });

        latitudEvento = getIntent().getExtras().getDouble("latitudEvento");
        longitudEvento = getIntent().getExtras().getDouble("longitudEvento");


        if(flagClientGoogleApi == false) {
            buildGoogleApiClient();
            flagClientGoogleApi = true;
        }
    }

    /**
     * Gestiona el botón "ir hacia atrás" de la Actividad.
     *
     * @param item elemento seleccionado por el usuario
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    /**
     * Es llamado cuando el mapa está listo para usarse.
     * Por tanto, se realiza la configuración de las características del mismo:
     * tipo de mapa, gestos, zoom, etc.
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LatLng coordenadas = new LatLng(latitudEvento, longitudEvento);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenadas, 16));
        originMarkers.clear();
        originMarkers.add(mMap.addMarker(new MarkerOptions()
                .title(PlaceAPI.getCompleteAddressString(coordenadas.latitude, coordenadas.longitude, this))
                .position(coordenadas)));
        originMarkers.get(0).showInfoWindow();

        //Se habilitan los gestos sobre el mapa
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setScrollGesturesEnabled(true);
        uiSettings.setTiltGesturesEnabled(true);
        uiSettings.setRotateGesturesEnabled(true);
        uiSettings.setAllGesturesEnabled(true);

        //mMap.getUiSettings().setZoomControlsEnabled(true);
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
        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
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
                            status.startResolutionForResult(MapaCompleto.this, PETICION_CONFIG_UBICACION);
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
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Log.i(TAG, "Inicio de recepción de ubicaciones");

            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locRequest, this);
            btnmyLocation.setEnabled(true);

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Mostrar diálogo explicativo
            } else {
                // Solicitar permiso
                ActivityCompat.requestPermissions(
                        this,
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

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PETICION_PERMISO_LOCALIZACION);
        } else {

            Location lastLocation =
                    LocationServices.FusedLocationApi.getLastLocation(apiClient);

            mLastLocation = lastLocation;


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

            } else {
                //Permiso denegado:
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

        if(mLastLocation != null) {
            autoCompleteTextViewOrigen.setText(PlaceAPI.getCompleteAddressString(mLastLocation.getLatitude(), mLastLocation.getLongitude(), MapaCompleto.this));
            durationDriving.setText("");
            distaceDriving.setText("");
            durationWalking.setText("");
            distaceWalking.setText("");
            sendRequestFindPath(0);
        }
        else {

            Toast.makeText(this,R.string.ubicacion_desconocida,Toast.LENGTH_SHORT).show();
        }

        disableLocationUpdates();
    }

    /**
     * Realiza la búsqueda de la ruta entre un origen y un destino (la ubicación del evento)
     *
     * @param modo indica si desea la ruta en coche o andando
     */
    private void sendRequestFindPath(int modo) {
        mMap.clear();

        String origin = autoCompleteTextViewOrigen.getText().toString();
        String destination = latitudEvento + "," + longitudEvento;
        if (origin.isEmpty()) {
            Toast.makeText(this, R.string.introduzca_localizacion_origen, Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, R.string.introduzca_localizacion_destino, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination, this).execute(modo);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gestiona el inicio de la búsqueda de la ruta
     */
    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, getText(R.string.por_favor_espere),
                getText(R.string.buscando_ruta), true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    /**
     * Gestiona la ruta encontrada
     *
     * @param modo indica si la ruta es en coche o andando
     * @param routes lista de enlaces
     * @param color color de la polilínea que muestra la ruta
     */
    @Override
    public void onDirectionFinderSuccess(int modo, List<Route> routes, int color) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();


        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));

            if (modo == 0) {
                durationDriving.setText(route.duration.text);
                distaceDriving.setText(route.distance.text);
            } else {
                durationWalking.setText(route.duration.text);
                distaceWalking.setText(route.distance.text);
            }

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(color).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

    /**
     * Gestiona la selección de un tipo de mapa
     *
     * @param menuItem item seleccionado por el usuario
     * @return verdadero
     */
    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.item_mapa_nomal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.item_mapa_satelite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.item_mapa_hibrido:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.item_mapa_tierra:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            default:
                return true;
        }
    }


}
