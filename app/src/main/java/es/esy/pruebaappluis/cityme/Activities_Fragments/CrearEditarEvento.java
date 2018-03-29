package es.esy.pruebaappluis.cityme.Activities_Fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import es.esy.pruebaappluis.cityme.BuildConfig;
import es.esy.pruebaappluis.cityme.MapsUtilities.PlacesAPI.PlaceAPI;
import es.esy.pruebaappluis.cityme.MapsUtilities.PlacesAPI.PlacesAutoCompleteAdapter;
import es.esy.pruebaappluis.cityme.Modelo.Evento;
import es.esy.pruebaappluis.cityme.Modelo.Promocion;
import es.esy.pruebaappluis.cityme.Modelo.Usuario;
import es.esy.pruebaappluis.cityme.R;
import es.esy.pruebaappluis.cityme.Volley.PeticionesServidorVolley;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Gestiona la creación, edición y eliminación de eventos.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class CrearEditarEvento extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMarkerDragListener {

    private EditText nombre;
    private EditText descripcion;
    private EditText lugar;
    private Button botonOk;
    private ImageView imagen;
    private ImageView imagenAnterior1;
    private ImageView imagenAnterior2;
    private ImageView imagenAnterior3;
    private ImageView imagenAnterior4;
    private ImageView imagenAnterior5;
    private ImageView imagenAnterior6;
    private ImageView imagenAnterior7;
    private ImageView imagenAnterior8;
    private int imagenSeleccionada;
    private CoordinatorLayout vista;
    private String tipoEvento;
    private Evento eventoEditado;
    private Button botonEliminar;

    private boolean getPromocionesImagenesAsociadas = false;

    private EditText fechaInicio;
    private EditText fechaFin;
    private Date d_fechaInicio = null;
    private Date d_fechaFin = null;
    private ImageButton botonFechaInicio;
    private ImageButton botonFechaFin;
    private SimpleDateFormat formateadorFecha = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    private SlideDateTimeListener listenerFechaInicio = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {

            fechaInicio.setText(formateadorFecha.format(date));
            d_fechaInicio = date;
        }

        // Optional cancel listener
        @Override
        public void onDateTimeCancel() {

        }
    };

    private SlideDateTimeListener listenerFechaFin = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {

            fechaFin.setText(formateadorFecha.format(date));
            d_fechaFin = date;
        }

        // Optional cancel listener
        @Override
        public void onDateTimeCancel() {

        }
    };

    private Button botonSeleccionarPromociones;

    private GoogleMap mMap;
    private ImageButton btnFindMarker;
    private AutoCompleteTextView autoCompleteTextViewOrigen;
    private ImageButton btnmyLocation;
    private Marker markerEvento = null;

    // Constantes de finalización para la actividad de cámara y galería
    private final int IMAGEN_GALERIA = 4;
    private final int IMAGEN_CAMARA = 5;
    // Constante de permisos
    private final int MY_PERMISSIONS = 100;

    // Constante para solicitar localización actual
    private static final int PETICION_PERMISO_LOCALIZACION = 101;
    private static final int PETICION_CONFIG_UBICACION = 201;

    private GoogleApiClient apiClient;
    private boolean flagClientGoogleApi = false;
    private LocationRequest locRequest;
    private Location mLastLocation;

    // Constantes para guardar la foto en la/s galerias
    private static String APP_DIRECTORY = "PruebaApp/";
    private static String MEDIA_DIRECTORY = APP_DIRECTORY + "PruebaApp";
    private String rutaFoto;

    private ProgressDialog pDialog;

    private static String TAG = CrearEditarEvento.class.getSimpleName();

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
        setContentView(R.layout.activity_crear_editar_evento);

        View view = getLayoutInflater().inflate(R.layout.activity_crear_editar_evento, null);

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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(this);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.map, mapFragment).commit();


        nombre = (EditText) findViewById(R.id.nombreEvento);
        descripcion = (EditText) findViewById(R.id.descripcionEvento);
        lugar = (EditText) findViewById(R.id.lugarEvento);
        vista = (CoordinatorLayout) findViewById(R.id.activity_crear_editar_evento);

        fechaInicio = (EditText) findViewById(R.id.textViewFechaInicio);
        fechaInicio.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setFechaInicio();

            }
        });
        botonFechaInicio = (ImageButton) findViewById(R.id.botonFechaInicio);
        botonFechaInicio.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setFechaInicio();

            }
        });

        fechaFin = (EditText) findViewById(R.id.textViewFechaFin);
        fechaFin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                setFechaFin();

            }
        });
        botonFechaFin = (ImageButton) findViewById(R.id.botonFechaFin);
        botonFechaFin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                setFechaFin();

            }
        });

        autoCompleteTextViewOrigen = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewOrigen);
        autoCompleteTextViewOrigen.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item_autocomplete));
        autoCompleteTextViewOrigen.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                sendRequestFindMarker();
            }
        });

        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getText(R.string.por_favor_espere));
        pDialog.setCancelable(false);

        imagen = (ImageView) findViewById(R.id.imageButtonView);
        imagenAnterior1 = (ImageView) findViewById(R.id.imagenAnterior1);
        imagenAnterior2 = (ImageView) findViewById(R.id.imagenAnterior2);
        imagenAnterior3 = (ImageView) findViewById(R.id.imagenAnterior3);
        imagenAnterior4 = (ImageView) findViewById(R.id.imagenAnterior4);
        imagenAnterior5 = (ImageView) findViewById(R.id.imagenAnterior5);
        imagenAnterior6 = (ImageView) findViewById(R.id.imagenAnterior6);
        imagenAnterior7 = (ImageView) findViewById(R.id.imagenAnterior7);
        imagenAnterior8 = (ImageView) findViewById(R.id.imagenAnterior8);

        if (mayRequestStoragePermission()) {
            imagen.setEnabled(true);
            imagenAnterior1.setEnabled(true);
            imagenAnterior2.setEnabled(true);
            imagenAnterior3.setEnabled(true);
            imagenAnterior4.setEnabled(true);
            imagenAnterior5.setEnabled(true);
            imagenAnterior6.setEnabled(true);
            imagenAnterior7.setEnabled(true);
            imagenAnterior8.setEnabled(true);
        }

        imagen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mayRequestStoragePermission();
                imagenSeleccionada = 0;
                captura_galeria();
            }
        });
        imagenAnterior1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mayRequestStoragePermission();
                imagenSeleccionada = 1;
                captura_galeria();
            }
        });
        imagenAnterior2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mayRequestStoragePermission();
                imagenSeleccionada = 2;
                captura_galeria();
            }
        });
        imagenAnterior3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mayRequestStoragePermission();
                imagenSeleccionada = 3;
                captura_galeria();
            }
        });
        imagenAnterior4.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mayRequestStoragePermission();
                imagenSeleccionada = 4;
                captura_galeria();
            }
        });
        imagenAnterior5.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mayRequestStoragePermission();
                imagenSeleccionada = 5;
                captura_galeria();
            }
        });
        imagenAnterior6.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mayRequestStoragePermission();
                imagenSeleccionada = 6;
                captura_galeria();
            }
        });
        imagenAnterior7.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mayRequestStoragePermission();
                imagenSeleccionada = 7;
                captura_galeria();
            }
        });
        imagenAnterior8.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mayRequestStoragePermission();
                imagenSeleccionada = 8;
                captura_galeria();
            }
        });


        botonOk = (Button) findViewById(R.id.botonOk);
        botonOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (TextUtils.equals(tipoEvento, "crear")) {

                    solicitudCrearNuevoEvento();

                } else if (TextUtils.equals(tipoEvento, "editar")) {

                    solicitudEliminarTodasPromocionesAsociadas();
                    solicitudEliminarImagenesAnterioresDelEvento();
                    solicitudEditarEvento();
                }

            }
        });

        botonEliminar = (Button) findViewById(R.id.botonEliminar);
        botonEliminar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(CrearEditarEvento.this);
                builder.setTitle(R.string.confirmacion);
                builder.setMessage(R.string.desea_eliminar_evento);
                builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        solicitudEliminarEvento();

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
                dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.rgb(97, 97, 97));


            }
        });

        botonSeleccionarPromociones = (Button) findViewById(R.id.botonSeleccionarPromociones);
        botonSeleccionarPromociones.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(CrearEditarEvento.this, SeleccionarPromocion.class);
                startActivity(intent);

            }
        });

        btnFindMarker = (ImageButton) findViewById(R.id.btnFind);
        btnFindMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequestFindMarker();
            }
        });

        btnmyLocation = (ImageButton) findViewById(R.id.btnMyLocation);
        btnmyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableLocationUpdates();
            }
        });


        if (flagClientGoogleApi == false) {
            buildGoogleApiClient();
            flagClientGoogleApi = true;
        }

        //Se comprueba si es un nuevo evento o es la edición de uno ya existente

        tipoEvento = MainActivity.tipoEvento;
        if (TextUtils.equals(tipoEvento, "crear")) {

            getSupportActionBar().setTitle(R.string.crear_evento);

            botonEliminar.setVisibility(View.INVISIBLE);
            imagen.setImageResource(R.drawable.add_image);
            imagenAnterior1.setImageResource(R.drawable.add_photo);
            imagenAnterior2.setImageResource(R.drawable.add_photo);
            imagenAnterior3.setImageResource(R.drawable.add_photo);
            imagenAnterior4.setImageResource(R.drawable.add_photo);
            imagenAnterior5.setImageResource(R.drawable.add_photo);
            imagenAnterior6.setImageResource(R.drawable.add_photo);
            imagenAnterior7.setImageResource(R.drawable.add_photo);
            imagenAnterior8.setImageResource(R.drawable.add_photo);

        } else if (TextUtils.equals(tipoEvento, "editar")) {

            getSupportActionBar().setTitle(R.string.editar_evento);

            eventoEditado = MainActivity.evento;
            nombre.setText(eventoEditado.getNombre());
            lugar.setText(eventoEditado.getLugar());
            descripcion.setText(eventoEditado.getDescripcion());
            eventoEditado.setImagen(eventoEditado.getImagen());
            imagen.setImageBitmap(eventoEditado.getImagenBitmap());
            imagenAnterior1.setImageResource(R.drawable.add_photo);
            imagenAnterior2.setImageResource(R.drawable.add_photo);
            imagenAnterior3.setImageResource(R.drawable.add_photo);
            imagenAnterior4.setImageResource(R.drawable.add_photo);
            imagenAnterior5.setImageResource(R.drawable.add_photo);
            imagenAnterior6.setImageResource(R.drawable.add_photo);
            imagenAnterior7.setImageResource(R.drawable.add_photo);
            imagenAnterior8.setImageResource(R.drawable.add_photo);
            autoCompleteTextViewOrigen.setText(PlaceAPI.getCompleteAddressString(eventoEditado.getLatitud(), eventoEditado.getLongitud(), this));

            fechaInicio.setText(eventoEditado.getFecha_inicio());
            fechaFin.setText(eventoEditado.getFecha_fin());

            botonEliminar.setVisibility(View.VISIBLE);

            if (getPromocionesImagenesAsociadas == false) {
                solicitudPromocionesAsociadas();
                solicitudImagenesAnterioresDelEvento();
                getPromocionesImagenesAsociadas = true;
            }


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
                MainActivity.promociones = new ArrayList<>();
                MainActivity.promocionesAntiguas = new ArrayList<>();
                finish();
                break;
        }
        return true;
    }

    /**
     * Permite establecer la fecha de inicio en el SlideDateTimePicker.
     * Se tendrá en cuenta si es un nuevo evento o es un evento existente.
     */
    public void setFechaInicio() {

        if (TextUtils.equals(tipoEvento, "crear")) {

            if (d_fechaInicio == null) {
                d_fechaInicio = new Date();
            }

            new SlideDateTimePicker.Builder(getSupportFragmentManager())
                    .setListener(listenerFechaInicio)
                    .setInitialDate(d_fechaInicio)
                    .setIs24HourTime(true)
                    .build()
                    .show();
        } else if (TextUtils.equals(tipoEvento, "editar")) {
            try {
                if (d_fechaInicio == null) {
                    d_fechaInicio = formateadorFecha.parse(eventoEditado.getFecha_inicio());
                }

                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listenerFechaInicio)
                        .setInitialDate(d_fechaInicio)
                        .setIs24HourTime(true)
                        .build()
                        .show();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Permite establecer la fecha de fin en el SlideDateTimePicker.
     * Se tendrá en cuenta si es un nuevo evento o es un evento existente.
     */
    public void setFechaFin() {

        if (TextUtils.equals(tipoEvento, "crear")) {

            if (d_fechaFin == null) {
                d_fechaFin = new Date();
            }

            new SlideDateTimePicker.Builder(getSupportFragmentManager())
                    .setListener(listenerFechaFin)
                    .setInitialDate(d_fechaFin)
                    .setIs24HourTime(true)
                    .build()
                    .show();
        } else if (TextUtils.equals(tipoEvento, "editar")) {
            try {
                if (d_fechaFin == null) {
                    d_fechaFin = formateadorFecha.parse(eventoEditado.getFecha_fin());
                }

                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listenerFechaFin)
                        .setInitialDate(d_fechaFin)
                        .setIs24HourTime(true)
                        .build()
                        .show();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
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

        mMap.setOnMarkerDragListener(this);

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.41, -3.69), 4));

        //Se habilitan los gestos sobre el mapa
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setScrollGesturesEnabled(true);
        uiSettings.setTiltGesturesEnabled(true);
        uiSettings.setRotateGesturesEnabled(true);
        uiSettings.setAllGesturesEnabled(true);

        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (TextUtils.equals(tipoEvento, "editar")) {
            sendRequestFindMarker();
        }

    }


    /**
     * Establece un marker en el mapa que se corresponderá a las coordenadas del evento.
     */
    private void sendRequestFindMarker() {

        if (TextUtils.isEmpty(autoCompleteTextViewOrigen.getText().toString())) {
            Toast.makeText(this, R.string.introduzca_localizacion_evento, Toast.LENGTH_SHORT).show();
        } else {
            LatLng coordenadas = PlaceAPI.getCoordenadasFromAddress(autoCompleteTextViewOrigen.getText().toString(), this);

            mMap.clear();
            MarkerOptions markerOptionsEvento = new MarkerOptions().position(coordenadas).title(autoCompleteTextViewOrigen.getText().toString()).draggable(true);
            markerEvento = mMap.addMarker(markerOptionsEvento);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenadas, 18));

        }

    }

    /**
     * Realiza la petición POST al servicio REST indicando la creación de un nuevo evento.
     */
    private void solicitudCrearNuevoEvento() {

        String uriTodosEventos = "http://pruebaappluis.esy.es/eventos";

        if (!PeticionesServidorVolley.compruebaConexion(this)) {
            Toast.makeText(this, R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(nombre.getText().toString())) {
            Toast.makeText(this, R.string.introduzca_nombre_evento, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(lugar.getText().toString())) {
            Toast.makeText(this, R.string.introduzca_lugar_evento, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(descripcion.getText().toString())) {
            Toast.makeText(this, R.string.introduzca_descripcion_evento, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(fechaInicio.getText().toString())) {
            Toast.makeText(this, R.string.introduzca_fecha_inicio_evento, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(fechaFin.getText().toString())) {
            Toast.makeText(this, R.string.introduzca_fecha_fin_evento, Toast.LENGTH_SHORT).show();
            return;
        }
        Date dateFechaInicio = null;
        Date dateFechaFin = null;
        Date dateActual = null;
        try {
            dateFechaInicio = formateadorFecha.parse(fechaInicio.getText().toString());
            dateFechaFin = formateadorFecha.parse(fechaFin.getText().toString());
            dateActual = formateadorFecha.parse(formateadorFecha.format(new Date()));
            if (dateFechaInicio.before(dateActual)) {
                Toast.makeText(this, R.string.fecha_inicio_anterior_actual, Toast.LENGTH_SHORT).show();
                return;
            }
            if (dateFechaFin.before(dateFechaInicio)) {
                Toast.makeText(this, R.string.fecha_fin_anterior_fecha_inicio, Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (imagen.getDrawable().getConstantState().equals(ContextCompat.getDrawable(this, R.drawable.add_image).getConstantState())) {
            Toast.makeText(this, R.string.selecciona_imagen_evento, Toast.LENGTH_SHORT).show();
            return;
        }
        if (markerEvento == null) {
            Toast.makeText(this, R.string.introduzca_localizacion_evento, Toast.LENGTH_SHORT).show();
            return;
        }

        showpDialog();

        // Se establecen los parametros que van asociados a la petición
        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("nombre_evento", nombre.getText().toString());
        parametros.put("descripcion_evento", descripcion.getText().toString());
        parametros.put("lugar_evento", lugar.getText().toString());
        parametros.put("ciudad_evento", PlaceAPI.getCityString(markerEvento.getPosition().latitude, markerEvento.getPosition().longitude, this));

        // Se obtiene el Bitmap de la imagen
        BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        // Se transforma el Bitmap a Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imagenBytes = baos.toByteArray();
        String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);


        parametros.put("imagen_evento", imagen);
        parametros.put("latitud_evento", String.valueOf(markerEvento.getPosition().latitude));
        parametros.put("longitud_evento", String.valueOf(markerEvento.getPosition().longitude));
        parametros.put("id_usuario", MainActivity.usuario.getId());
        parametros.put("fecha_inicio_evento", fechaInicio.getText().toString());
        parametros.put("fecha_fin_evento", fechaFin.getText().toString());

        // Se obtiene el objeto JSON que irá asociado a la petición
        JSONObject parametrosJson = new JSONObject(parametros);

        // PETICIÓN POST
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                uriTodosEventos, parametrosJson, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());


                try {
                    if (response.getInt("estado") == 1) { //EXITO
                        hidepDialog();

                        String id_evento = response.getString("datos");

                        // Se asocian las promociones con el evento

                        if (!MainActivity.promociones.isEmpty()) {
                            ArrayList<Promocion> promocionesMain = MainActivity.promociones;

                            for (int i = 0; i < promocionesMain.size(); i++) {

                                solicitudAsociarPromocionEvento(id_evento, promocionesMain.get(i).getId());

                            }
                        }

                        MainActivity.promociones = new ArrayList<Promocion>();

                        // Se asocian las imagenes con el evento

                        if (!imagenAnterior1.getDrawable().getConstantState().equals(ContextCompat.getDrawable(CrearEditarEvento.this, R.drawable.add_photo).getConstantState())) {
                            // Se obtiene el Bitmap de la imagen
                            BitmapDrawable drawable = (BitmapDrawable) imagenAnterior1.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            // Se transforma el Bitmap a Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imagenBytes = baos.toByteArray();
                            String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

                            solicitudAsociarImagenAnterioreAlEvento(id_evento, imagen);
                        }
                        if (!imagenAnterior2.getDrawable().getConstantState().equals(ContextCompat.getDrawable(CrearEditarEvento.this, R.drawable.add_photo).getConstantState())) {
                            // Se obtiene el Bitmap de la imagen
                            BitmapDrawable drawable = (BitmapDrawable) imagenAnterior2.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            // Se transforma el Bitmap a Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imagenBytes = baos.toByteArray();
                            String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

                            solicitudAsociarImagenAnterioreAlEvento(id_evento, imagen);
                        }
                        if (!imagenAnterior3.getDrawable().getConstantState().equals(ContextCompat.getDrawable(CrearEditarEvento.this, R.drawable.add_photo).getConstantState())) {
                            // Se obtiene el Bitmap de la imagen
                            BitmapDrawable drawable = (BitmapDrawable) imagenAnterior3.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            // Se transforma el Bitmap a Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imagenBytes = baos.toByteArray();
                            String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

                            solicitudAsociarImagenAnterioreAlEvento(id_evento, imagen);
                        }
                        if (!imagenAnterior4.getDrawable().getConstantState().equals(ContextCompat.getDrawable(CrearEditarEvento.this, R.drawable.add_photo).getConstantState())) {
                            // Se obtiene el Bitmap de la imagen
                            BitmapDrawable drawable = (BitmapDrawable) imagenAnterior4.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            // Se transforma el Bitmap a Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imagenBytes = baos.toByteArray();
                            String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

                            solicitudAsociarImagenAnterioreAlEvento(id_evento, imagen);
                        }
                        if (!imagenAnterior5.getDrawable().getConstantState().equals(ContextCompat.getDrawable(CrearEditarEvento.this, R.drawable.add_photo).getConstantState())) {
                            // Se obtiene el Bitmap de la imagen
                            BitmapDrawable drawable = (BitmapDrawable) imagenAnterior5.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            // Se transforma el Bitmap a Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imagenBytes = baos.toByteArray();
                            String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

                            solicitudAsociarImagenAnterioreAlEvento(id_evento, imagen);
                        }
                        if (!imagenAnterior6.getDrawable().getConstantState().equals(ContextCompat.getDrawable(CrearEditarEvento.this, R.drawable.add_photo).getConstantState())) {
                            // Se obtiene el Bitmap de la imagen
                            BitmapDrawable drawable = (BitmapDrawable) imagenAnterior6.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            // Se transforma el Bitmap a Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imagenBytes = baos.toByteArray();
                            String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

                            solicitudAsociarImagenAnterioreAlEvento(id_evento, imagen);
                        }
                        if (!imagenAnterior7.getDrawable().getConstantState().equals(ContextCompat.getDrawable(CrearEditarEvento.this, R.drawable.add_photo).getConstantState())) {
                            // Se obtiene el Bitmap de la imagen
                            BitmapDrawable drawable = (BitmapDrawable) imagenAnterior7.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            // Se transforma el Bitmap a Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imagenBytes = baos.toByteArray();
                            String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

                            solicitudAsociarImagenAnterioreAlEvento(id_evento, imagen);
                        }
                        if (!imagenAnterior8.getDrawable().getConstantState().equals(ContextCompat.getDrawable(CrearEditarEvento.this, R.drawable.add_photo).getConstantState())) {
                            // Se obtiene el Bitmap de la imagen
                            BitmapDrawable drawable = (BitmapDrawable) imagenAnterior8.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            // Se transforma el Bitmap a Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imagenBytes = baos.toByteArray();
                            String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

                            solicitudAsociarImagenAnterioreAlEvento(id_evento, imagen);
                        }


                        finish();
                    } else { // ERROR API
                        Toast.makeText(CrearEditarEvento.this,
                                response.getString("datos"), Toast.LENGTH_SHORT).show();

                        hidepDialog();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(CrearEditarEvento.this, R.string.error_conexion, Toast.LENGTH_SHORT).show();

                hidepDialog();
            }
        });

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }

    /**
     * Realiza la petición POST al servicio REST indicando la asociación del evento con una determinada promoción.
     *
     * @param id_evento identificador del evento
     * @param id_promocion identificador de la promoción
     */
    private void solicitudAsociarPromocionEvento(String id_evento, String id_promocion) {

        String uriTodasPromociones = "http://pruebaappluis.esy.es/promociones";

        // Se establecen los parametros que van asociados a la petición
        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("id_promocion", id_promocion);
        parametros.put("id_evento", id_evento);

        // Se obtiene el objeto JSON que irá asociado a la petición
        JSONObject parametrosJson = new JSONObject(parametros);

        // PETICIÓN POST
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                uriTodasPromociones + "/" + id_promocion + "/" + id_evento, parametrosJson, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());


                try {
                    if (response.getInt("estado") == 1) { //EXITO

                    } else { // ERROR API
                        Toast.makeText(CrearEditarEvento.this,
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
                Toast.makeText(CrearEditarEvento.this, R.string.error_asociar_promociones, Toast.LENGTH_SHORT).show();

            }
        });

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }

    /** 
     * Realiza la petición POST al servicio REST indicando la asociación de una imagen de otra edición al evento.
     *
     * @param id_evento identificador del evento
     * @param imagenBase64 imagen en formato Base64
     */
    private void solicitudAsociarImagenAnterioreAlEvento(String id_evento, String imagenBase64) {

        String uri = "http://pruebaappluis.esy.es/eventos/imagenes";

        // Se establecen los parametros que van asociados a la petición
        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("id_evento", id_evento);
        parametros.put("imagen", imagenBase64);

        // Se obtiene el objeto JSON que irá asociado a la petición
        JSONObject parametrosJson = new JSONObject(parametros);

        // PETICIÓN POST
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                uri + "/" + id_evento, parametrosJson, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());


                try {
                    if (response.getInt("estado") == 1) { //EXITO

                    } else { // ERROR API
                        Toast.makeText(CrearEditarEvento.this,
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
                Toast.makeText(CrearEditarEvento.this, R.string.error_asociar_imagenes_anteriores, Toast.LENGTH_SHORT).show();

            }
        });

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }

    /**
     * Realiza la petición PUT al servicio REST indicando la modificación de la información del evento.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void solicitudEditarEvento() {

        String uriTodosEventos = "http://pruebaappluis.esy.es/eventos";

        if (!PeticionesServidorVolley.compruebaConexion(this)) {
            Toast.makeText(this, R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(nombre.getText().toString())) {
            Toast.makeText(this, R.string.introduzca_nombre_evento, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(lugar.getText().toString())) {
            Toast.makeText(this, R.string.introduzca_lugar_evento, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(descripcion.getText().toString())) {
            Toast.makeText(this, R.string.introduzca_descripcion_evento, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(fechaInicio.getText().toString())) {
            Toast.makeText(this, R.string.introduzca_fecha_inicio_evento, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(fechaFin.getText().toString())) {
            Toast.makeText(this, R.string.introduzca_fecha_fin_evento, Toast.LENGTH_SHORT).show();
            return;
        }
        Date dateFechaInicio = null;
        Date dateFechaFin = null;
        Date dateActual = null;
        try {
            dateFechaInicio = formateadorFecha.parse(fechaInicio.getText().toString());
            dateFechaFin = formateadorFecha.parse(fechaFin.getText().toString());
            dateActual = formateadorFecha.parse(formateadorFecha.format(new Date()));
            if (dateFechaInicio.before(dateActual)) {
                Toast.makeText(this, R.string.fecha_inicio_anterior_actual, Toast.LENGTH_SHORT).show();
                return;
            }
            if (dateFechaFin.before(dateFechaInicio)) {
                Toast.makeText(this, R.string.fecha_fin_anterior_fecha_inicio, Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (imagen.getDrawable().getConstantState().equals(ContextCompat.getDrawable(this, R.drawable.add_image).getConstantState())) {
            Toast.makeText(this, R.string.selecciona_imagen_evento, Toast.LENGTH_SHORT).show();
            return;
        }
        if (markerEvento == null) {
            Toast.makeText(this, R.string.introduzca_localizacion_evento, Toast.LENGTH_SHORT).show();
            return;
        }

        showpDialog();

        // Se establecen los parametros que van asociados a la petición
        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("nombre_evento", nombre.getText().toString());
        parametros.put("descripcion_evento", descripcion.getText().toString());
        parametros.put("lugar_evento", lugar.getText().toString());
        parametros.put("ciudad_evento", PlaceAPI.getCityString(markerEvento.getPosition().latitude, markerEvento.getPosition().longitude, this));

        // Se obtiene el Bitmap de la imagen
        BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        // Se transforma el Bitmap a Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imagenBytes = baos.toByteArray();
        String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

        parametros.put("imagen_evento", imagen);
        parametros.put("latitud_evento", String.valueOf(markerEvento.getPosition().latitude));
        parametros.put("longitud_evento", String.valueOf(markerEvento.getPosition().longitude));
        parametros.put("fecha_inicio_evento", fechaInicio.getText().toString());
        parametros.put("fecha_fin_evento", fechaFin.getText().toString());

        // Se obtiene el objeto JSON que irá asociado a la petición
        JSONObject parametrosJson = new JSONObject(parametros);

        eventoEditado.setNombre(nombre.getText().toString());
        eventoEditado.setDescripcion(descripcion.getText().toString());
        eventoEditado.setLugar(lugar.getText().toString());
        eventoEditado.setCiudad(PlaceAPI.getCityString(markerEvento.getPosition().latitude, markerEvento.getPosition().longitude, this));
        eventoEditado.setImagen(imagen);
        eventoEditado.setLatitud(markerEvento.getPosition().latitude);
        eventoEditado.setLongitud(markerEvento.getPosition().longitude);
        eventoEditado.setFecha_inicio(fechaInicio.getText().toString());
        eventoEditado.setFecha_fin(fechaFin.getText().toString());

        MainActivity.evento = eventoEditado;

        // PETICIÓN PUT
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.PUT,
                uriTodosEventos + "/" + eventoEditado.getId(), parametrosJson, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {
                    if (response.getInt("estado") == 1) { //EXITO
                        hidepDialog();

                        // Se asocian las promociones con el evento

                        if (!MainActivity.promociones.isEmpty()) {
                            ArrayList<Promocion> promocionesMain = MainActivity.promociones;

                            for (int i = 0; i < promocionesMain.size(); i++) {
                                solicitudAsociarPromocionEvento(eventoEditado.getId(), promocionesMain.get(i).getId());

                            }
                        }

                        MainActivity.promociones = new ArrayList<Promocion>();
                        MainActivity.promocionesAntiguas = new ArrayList<Promocion>();

                        // Se asocian las imagenes con el evento

                        if (!imagenAnterior1.getDrawable().getConstantState().equals(ContextCompat.getDrawable(CrearEditarEvento.this, R.drawable.add_photo).getConstantState())) {
                            // Se obtiene el Bitmap de la imagen
                            BitmapDrawable drawable = (BitmapDrawable) imagenAnterior1.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            // Se transforma el Bitmap a Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imagenBytes = baos.toByteArray();
                            String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

                            solicitudAsociarImagenAnterioreAlEvento(eventoEditado.getId(), imagen);
                        }
                        if (!imagenAnterior2.getDrawable().getConstantState().equals(ContextCompat.getDrawable(CrearEditarEvento.this, R.drawable.add_photo).getConstantState())) {
                            // Se obtiene el Bitmap de la imagen
                            BitmapDrawable drawable = (BitmapDrawable) imagenAnterior2.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            // Se transforma el Bitmap a Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imagenBytes = baos.toByteArray();
                            String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

                            solicitudAsociarImagenAnterioreAlEvento(eventoEditado.getId(), imagen);
                        }
                        if (!imagenAnterior3.getDrawable().getConstantState().equals(ContextCompat.getDrawable(CrearEditarEvento.this, R.drawable.add_photo).getConstantState())) {
                            // Se obtiene el Bitmap de la imagen
                            BitmapDrawable drawable = (BitmapDrawable) imagenAnterior3.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            // Se transforma el Bitmap a Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imagenBytes = baos.toByteArray();
                            String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

                            solicitudAsociarImagenAnterioreAlEvento(eventoEditado.getId(), imagen);
                        }
                        if (!imagenAnterior4.getDrawable().getConstantState().equals(ContextCompat.getDrawable(CrearEditarEvento.this, R.drawable.add_photo).getConstantState())) {
                            // Se obtiene el Bitmap de la imagen
                            BitmapDrawable drawable = (BitmapDrawable) imagenAnterior4.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            // Se transforma el Bitmap a Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imagenBytes = baos.toByteArray();
                            String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

                            solicitudAsociarImagenAnterioreAlEvento(eventoEditado.getId(), imagen);
                        }
                        if (!imagenAnterior5.getDrawable().getConstantState().equals(ContextCompat.getDrawable(CrearEditarEvento.this, R.drawable.add_photo).getConstantState())) {
                            // Se obtiene el Bitmap de la imagen
                            BitmapDrawable drawable = (BitmapDrawable) imagenAnterior5.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            // Se transforma el Bitmap a Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imagenBytes = baos.toByteArray();
                            String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

                            solicitudAsociarImagenAnterioreAlEvento(eventoEditado.getId(), imagen);
                        }
                        if (!imagenAnterior6.getDrawable().getConstantState().equals(ContextCompat.getDrawable(CrearEditarEvento.this, R.drawable.add_photo).getConstantState())) {
                            // Se obtiene el Bitmap de la imagen
                            BitmapDrawable drawable = (BitmapDrawable) imagenAnterior6.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            // Se transforma el Bitmap a Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imagenBytes = baos.toByteArray();
                            String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

                            solicitudAsociarImagenAnterioreAlEvento(eventoEditado.getId(), imagen);
                        }
                        if (!imagenAnterior7.getDrawable().getConstantState().equals(ContextCompat.getDrawable(CrearEditarEvento.this, R.drawable.add_photo).getConstantState())) {
                            // Se obtiene el Bitmap de la imagen
                            BitmapDrawable drawable = (BitmapDrawable) imagenAnterior7.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            // Se transforma el Bitmap a Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imagenBytes = baos.toByteArray();
                            String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

                            solicitudAsociarImagenAnterioreAlEvento(eventoEditado.getId(), imagen);
                        }
                        if (!imagenAnterior8.getDrawable().getConstantState().equals(ContextCompat.getDrawable(CrearEditarEvento.this, R.drawable.add_photo).getConstantState())) {
                            // Se obtiene el Bitmap de la imagen
                            BitmapDrawable drawable = (BitmapDrawable) imagenAnterior8.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            // Se transforma el Bitmap a Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imagenBytes = baos.toByteArray();
                            String imagen = Base64.encodeToString(imagenBytes, Base64.DEFAULT);

                            solicitudAsociarImagenAnterioreAlEvento(eventoEditado.getId(), imagen);
                        }

                        setResult(1000); // Evento editado
                        finish();

                    } else { // ERROR API
                        Toast.makeText(CrearEditarEvento.this,
                                response.getString("datos"), Toast.LENGTH_SHORT).show();

                        hidepDialog();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(CrearEditarEvento.this,
                        R.string.error_conexion, Toast.LENGTH_SHORT).show();

                hidepDialog();
            }
        });

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }

    /**
     * Realiza la petición DELETE al servicio REST indicando la eliminación del evento.
     */
    private void solicitudEliminarEvento() {

        String uriTodosEventos = "http://pruebaappluis.esy.es/eventos";

        if (!PeticionesServidorVolley.compruebaConexion(this)) {
            Toast.makeText(this, R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        showpDialog();

        // PETICIÓN DELETE
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.DELETE,
                uriTodosEventos + "/" + eventoEditado.getId(), null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());


                try {
                    if (response.getInt("estado") == 1) { //EXITO

                        hidepDialog();

                        setResult(1001); // Evento eliminado
                        finish();
                    } else { // ERROR API
                        Toast.makeText(CrearEditarEvento.this,
                                response.getString("datos"), Toast.LENGTH_SHORT).show();

                        hidepDialog();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(CrearEditarEvento.this,
                        R.string.error_conexion, Toast.LENGTH_SHORT).show();

                hidepDialog();
            }
        });

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }

    /**
     * Realiza la petición DELETE al servicio REST indicando la eliminación
     * de la asociación de todas las promociones del evento.
     */
    private void solicitudEliminarTodasPromocionesAsociadas() {

        String uriTodasPromociones = "http://pruebaappluis.esy.es/promociones";

        if (!PeticionesServidorVolley.compruebaConexion(this)) {
            Toast.makeText(this, R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        // PETICIÓN DELETE
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.DELETE,
                uriTodasPromociones + "/evento/" + eventoEditado.getId(), null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());


                try {
                    if (response.getInt("estado") == 1) { //EXITO

                    } else { // ERROR API
                        Toast.makeText(CrearEditarEvento.this,
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
                Toast.makeText(CrearEditarEvento.this,
                        R.string.error_conexion, Toast.LENGTH_SHORT).show();

            }
        });

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }

    /**
     * Realiza la petición DELETE al servicio REST indicando la eliminación
     * de todas las imágenes de ediciones anteriores del evento.
     */
    private void solicitudEliminarImagenesAnterioresDelEvento() {

        String uri = "http://pruebaappluis.esy.es/eventos/imagenes";

        if (!PeticionesServidorVolley.compruebaConexion(this)) {
            Toast.makeText(this, R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        // PETICIÓN DELETE
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.DELETE,
                uri + "/" + eventoEditado.getId(), null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());


                try {
                    if (response.getInt("estado") == 1) { //EXITO

                    } else { // ERROR API
                        Toast.makeText(CrearEditarEvento.this,
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
                Toast.makeText(CrearEditarEvento.this,
                        R.string.error_conexion, Toast.LENGTH_SHORT).show();

            }
        });

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }

    /**
     * Realiza la petición GET al servicio REST mediante la cual se obtienen
     * todas las promociones del evento.
     */
    private void solicitudPromocionesAsociadas() {

        String uriTodasPromociones = "http://pruebaappluis.esy.es/promociones";

        if (!PeticionesServidorVolley.compruebaConexion(this)) {
            Toast.makeText(this, R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        // PETICIÓN GET
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                uriTodasPromociones + "/evento/" + eventoEditado.getId(), null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());


                try {
                    if (response.getInt("estado") == 1) { //EXITO

                        ArrayList<Promocion> promocionesAsociadas = new ArrayList<>();

                        JSONArray datos = response.getJSONArray("datos");
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


                            promocionesAsociadas.add(promocion);
                        }

                        MainActivity.promocionesAntiguas = promocionesAsociadas;

                    } else { // ERROR API
                        Toast.makeText(CrearEditarEvento.this,
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
                Toast.makeText(CrearEditarEvento.this,
                        R.string.error_conexion, Toast.LENGTH_SHORT).show();
                finish();

            }
        });

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }

    /**
     * Realiza la petición GET al servicio REST mediante la cual se obtienen
     * todas las imágenes de ediciones anteriores del evento.
     */
    private void solicitudImagenesAnterioresDelEvento() {

        String uri = "http://pruebaappluis.esy.es/eventos/imagenes";

        if (!PeticionesServidorVolley.compruebaConexion(this)) {
            Toast.makeText(this, R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        // PETICIÓN GET
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                uri + "/" + eventoEditado.getId(), null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());


                try {
                    if (response.getInt("estado") == 1) { //EXITO

                        ArrayList<String> imagenesAnterioresDelEvento = new ArrayList<>();
                        for (int i = 0; i < 8; i++) {
                            imagenesAnterioresDelEvento.add("");
                        }

                        JSONArray datos = response.getJSONArray("datos");
                        for (int i = 0; i < datos.length(); i++) {

                            JSONObject c = datos.getJSONObject(i);
                            String imagen = c.getString("imagen");

                            imagenesAnterioresDelEvento.set(i, imagen);
                        }

                        if (!(imagenesAnterioresDelEvento.get(0).equals(""))) {
                            byte[] byteimagen = Base64.decode(imagenesAnterioresDelEvento.get(0), Base64.DEFAULT);
                            Bitmap imagenBitmap = BitmapFactory.decodeByteArray(byteimagen, 0, byteimagen.length);
                            imagenAnterior1.setImageBitmap(imagenBitmap);
                        }
                        if (!(imagenesAnterioresDelEvento.get(1).equals(""))) {
                            byte[] byteimagen = Base64.decode(imagenesAnterioresDelEvento.get(1), Base64.DEFAULT);
                            Bitmap imagenBitmap = BitmapFactory.decodeByteArray(byteimagen, 0, byteimagen.length);
                            imagenAnterior2.setImageBitmap(imagenBitmap);
                        }
                        if (!(imagenesAnterioresDelEvento.get(2).equals(""))) {
                            byte[] byteimagen = Base64.decode(imagenesAnterioresDelEvento.get(2), Base64.DEFAULT);
                            Bitmap imagenBitmap = BitmapFactory.decodeByteArray(byteimagen, 0, byteimagen.length);
                            imagenAnterior3.setImageBitmap(imagenBitmap);
                        }
                        if (!(imagenesAnterioresDelEvento.get(3).equals(""))) {
                            byte[] byteimagen = Base64.decode(imagenesAnterioresDelEvento.get(3), Base64.DEFAULT);
                            Bitmap imagenBitmap = BitmapFactory.decodeByteArray(byteimagen, 0, byteimagen.length);
                            imagenAnterior4.setImageBitmap(imagenBitmap);
                        }
                        if (!(imagenesAnterioresDelEvento.get(4).equals(""))) {
                            byte[] byteimagen = Base64.decode(imagenesAnterioresDelEvento.get(4), Base64.DEFAULT);
                            Bitmap imagenBitmap = BitmapFactory.decodeByteArray(byteimagen, 0, byteimagen.length);
                            imagenAnterior5.setImageBitmap(imagenBitmap);
                        }
                        if (!(imagenesAnterioresDelEvento.get(5).equals(""))) {
                            byte[] byteimagen = Base64.decode(imagenesAnterioresDelEvento.get(5), Base64.DEFAULT);
                            Bitmap imagenBitmap = BitmapFactory.decodeByteArray(byteimagen, 0, byteimagen.length);
                            imagenAnterior6.setImageBitmap(imagenBitmap);
                        }
                        if (!(imagenesAnterioresDelEvento.get(6).equals(""))) {
                            byte[] byteimagen = Base64.decode(imagenesAnterioresDelEvento.get(6), Base64.DEFAULT);
                            Bitmap imagenBitmap = BitmapFactory.decodeByteArray(byteimagen, 0, byteimagen.length);
                            imagenAnterior7.setImageBitmap(imagenBitmap);
                        }
                        if (!(imagenesAnterioresDelEvento.get(7).equals(""))) {
                            byte[] byteimagen = Base64.decode(imagenesAnterioresDelEvento.get(7), Base64.DEFAULT);
                            Bitmap imagenBitmap = BitmapFactory.decodeByteArray(byteimagen, 0, byteimagen.length);
                            imagenAnterior8.setImageBitmap(imagenBitmap);
                        }

                    } else { // ERROR API
                        Toast.makeText(CrearEditarEvento.this,
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
                Toast.makeText(CrearEditarEvento.this,
                        R.string.error_conexion, Toast.LENGTH_SHORT).show();
                finish();

            }
        });

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }

    /**
     * Indica si se desea acceder a la cámara o a la galería
     * y procesa la acción indicada.
     */
    private void captura_galeria() {

        try {
            if (imagenSeleccionada == 0) {

                final CharSequence[] items = {getText(R.string.seleccionar_galeria), getText(R.string.seleccionar_camara)};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.seleccionar_foto);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intent.setType("image/*");
                                startActivityForResult(intent, IMAGEN_GALERIA);
                                break;
                            case 1:


                                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), MEDIA_DIRECTORY);
                                boolean isDirectoryCreated = dir.exists();

                                if (!isDirectoryCreated)
                                    isDirectoryCreated = dir.mkdirs();

                                if (isDirectoryCreated) {

                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
                                    String date = dateFormat.format(new Date());
                                    String nombre = "pic_" + date + ".jpg";

                                    File file = new File(dir, nombre);
                                    rutaFoto = "file:" + file.getAbsolutePath();
                                    Uri fotoURI = FileProvider.getUriForFile(CrearEditarEvento.this, BuildConfig.APPLICATION_ID + ".provider", file);

                                    Intent intentCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    intentCamara.putExtra(MediaStore.EXTRA_OUTPUT, fotoURI);

                                    startActivityForResult(intentCamara, IMAGEN_CAMARA);
                                }
                                break;


                        }

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

            } else if (imagenSeleccionada != 0) {

                final CharSequence[] items = {getText(R.string.seleccionar_galeria), getText(R.string.seleccionar_camara), getText(R.string.eliminar_imagen)};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.seleccionar_foto);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intent.setType("image/*");
                                startActivityForResult(intent, IMAGEN_GALERIA);
                                break;
                            case 1:

                                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), MEDIA_DIRECTORY);
                                boolean isDirectoryCreated = dir.exists();

                                if (!isDirectoryCreated)
                                    isDirectoryCreated = dir.mkdirs();

                                if (isDirectoryCreated) {

                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
                                    String date = dateFormat.format(new Date());
                                    String nombre = "pic_" + date + ".jpg";

                                    File file = new File(dir, nombre);
                                    rutaFoto = "file:" + file.getAbsolutePath();
                                    Uri fotoURI = FileProvider.getUriForFile(CrearEditarEvento.this, BuildConfig.APPLICATION_ID + ".provider", file);

                                    Intent intentCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    intentCamara.putExtra(MediaStore.EXTRA_OUTPUT, fotoURI);

                                    startActivityForResult(intentCamara, IMAGEN_CAMARA);
                                }
                                break;
                            case 2:

                                if (imagenSeleccionada == 1) {
                                    imagenAnterior1.setImageResource(R.drawable.add_photo);
                                }
                                else if (imagenSeleccionada == 2) {
                                    imagenAnterior2.setImageResource(R.drawable.add_photo);
                                }
                                else if (imagenSeleccionada == 3) {
                                    imagenAnterior3.setImageResource(R.drawable.add_photo);
                                }
                                else if (imagenSeleccionada == 4) {
                                    imagenAnterior4.setImageResource(R.drawable.add_photo);
                                }
                                else if (imagenSeleccionada == 5) {
                                    imagenAnterior5.setImageResource(R.drawable.add_photo);
                                }
                                else if (imagenSeleccionada == 6) {
                                    imagenAnterior6.setImageResource(R.drawable.add_photo);
                                }
                                else if (imagenSeleccionada == 7) {
                                    imagenAnterior7.setImageResource(R.drawable.add_photo);
                                }
                                else if (imagenSeleccionada == 8) {
                                    imagenAnterior8.setImageResource(R.drawable.add_photo);
                                }
                                break;

                        }

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        } catch (Exception e) {
        }


    }


    /**
     * Procesa los resultados obtenidos tras acceder a otras Actividades,
     * como son la cámara, la galería, configuración de la ubicación.
     *
     * @param requestCode permite identificar de qué Actividad procede el resultado
     * @param resultCode código de resultado devuelto por la otra Actividad
     * @param data puede contener datos devueltos por la Actividad
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PETICION_CONFIG_UBICACION) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    btnmyLocation.setEnabled(true);
                    startLocationUpdates();
                    break;
                case Activity.RESULT_CANCELED:
                    Log.i(TAG, "El usuario no ha realizado los cambios de configuración necesarios");
                    break;
            }
        } else {

            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case IMAGEN_CAMARA:

                        Uri imageUri = Uri.parse(rutaFoto);
                        File file = new File(imageUri.getPath());

                        InputStream ims = null;
                        try {
                            ims = new FileInputStream(file);

                            switch (imagenSeleccionada) {
                                case 0:
                                    imagen.setImageBitmap(BitmapFactory.decodeStream(ims));
                                    break;
                                case 1:
                                    imagenAnterior1.setImageBitmap(BitmapFactory.decodeStream(ims));
                                    break;
                                case 2:
                                    imagenAnterior2.setImageBitmap(BitmapFactory.decodeStream(ims));
                                    break;
                                case 3:
                                    imagenAnterior3.setImageBitmap(BitmapFactory.decodeStream(ims));
                                    break;
                                case 4:
                                    imagenAnterior4.setImageBitmap(BitmapFactory.decodeStream(ims));
                                    break;
                                case 5:
                                    imagenAnterior5.setImageBitmap(BitmapFactory.decodeStream(ims));
                                    break;
                                case 6:
                                    imagenAnterior6.setImageBitmap(BitmapFactory.decodeStream(ims));
                                    break;
                                case 7:
                                    imagenAnterior7.setImageBitmap(BitmapFactory.decodeStream(ims));
                                    break;
                                case 8:
                                    imagenAnterior8.setImageBitmap(BitmapFactory.decodeStream(ims));
                                    break;
                            }


                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }


                        MediaScannerConnection.scanFile(this,
                                new String[]{imageUri.getPath()}, null,
                                new MediaScannerConnection.OnScanCompletedListener() {

                                    @Override
                                    public void onScanCompleted(String path, Uri uri) {
                                        Log.i("ExternalStorage", "Scanned " + path + ":");
                                        Log.i("ExternalStorage", "-> Uri = " + uri);
                                    }
                                });


                        break;
                    case IMAGEN_GALERIA:

                        Uri path = data.getData();

                        switch (imagenSeleccionada) {
                            case 0:
                                imagen.setImageURI(path);
                                break;
                            case 1:
                                imagenAnterior1.setImageURI(path);
                                break;
                            case 2:
                                imagenAnterior2.setImageURI(path);
                                break;
                            case 3:
                                imagenAnterior3.setImageURI(path);
                                break;
                            case 4:
                                imagenAnterior4.setImageURI(path);
                                break;
                            case 5:
                                imagenAnterior5.setImageURI(path);
                                break;
                            case 6:
                                imagenAnterior6.setImageURI(path);
                                break;
                            case 7:
                                imagenAnterior7.setImageURI(path);
                                break;
                            case 8:
                                imagenAnterior8.setImageURI(path);
                                break;
                        }

                        break;

                }
            }
        }
    }

    /**
     * Muestra un diálogo de progreso
     */
    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    /**
     * Oculta un diálogo de progreso
     */
    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    /**
     * Comprueba si se tienen los permisos de acceso a la cámara y a la galería.
     * @return indica si se tienen o no los permisos
     */
    private boolean mayRequestStoragePermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        if ((ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED))
            return true;

        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) || (shouldShowRequestPermissionRationale(CAMERA))) {
            Snackbar.make(vista, R.string.permisos_son_necesarios_app,
                    Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
                }
            });
        } else {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
        }

        return false;
    }

    /**
     * Gestiona el resultado de la petición de permisos, en este caso,
     * de los permisos de localización, de la cámara y la galería.
     *
     * @param requestCode permite identificar de dónde procede el resultado
     * @param permissions permisos solicitados
     * @param grantResults indica si el permiso ha sido concedido o denegado
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

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

        if (requestCode == MY_PERMISSIONS) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permisos_aceptados, Toast.LENGTH_SHORT).show();
                imagen.setEnabled(true);
                imagenAnterior1.setEnabled(true);
                imagenAnterior2.setEnabled(true);
                imagenAnterior3.setEnabled(true);
                imagenAnterior4.setEnabled(true);
                imagenAnterior5.setEnabled(true);
                imagenAnterior6.setEnabled(true);
                imagenAnterior7.setEnabled(true);
                imagenAnterior8.setEnabled(true);
            }
        } else {
            showExplanation();
        }
    }

    /**
     * Muestra un diálogo indicando que los permisos son necesarios para utilizar
     * la aplicación.
     */
    private void showExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permisos_denegados);
        builder.setMessage(R.string.permisos_son_necesarios_app);
        builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", CrearEditarEvento.this.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                finish();
            }
        });

        builder.show();
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
                            status.startResolutionForResult(CrearEditarEvento.this, PETICION_CONFIG_UBICACION);
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
     * Es llamado cuando se conoce una nueva ubicación del dispositivo.
     * @param location nueva ubicación
     */
    @Override
    public void onLocationChanged(Location location) {

        Log.i(TAG, "Recibida nueva ubicación!");

        // Se muestra la nueva ubicación recibida
        mLastLocation = location;

        if (mLastLocation != null) {

            autoCompleteTextViewOrigen.setText(PlaceAPI.getCompleteAddressString(mLastLocation.getLatitude(), mLastLocation.getLongitude(), CrearEditarEvento.this));
            sendRequestFindMarker();
        } else {

            Toast.makeText(this, R.string.ubicacion_desconocida, Toast.LENGTH_SHORT).show();
        }

        disableLocationUpdates();
    }

    /**
     * Es llamado cuando un marcador comienza a ser arrastrado.
     * @param marker marcador
     */
    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    /**
     * Gestiona los marcados que están siendo arrastrados sobre el mapa de Google.
     * @param marker marcador
     */
    @Override
    public void onMarkerDrag(Marker marker) {

    }

    /**
     * Es llamado cuando un marcador ha terminado de ser arrastrado.
     * Es entonces cuando se gestiona su nueva ubicación en el mapa.
     * @param marker marcador
     */
    @Override
    public void onMarkerDragEnd(Marker marker) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        marker.setTitle(PlaceAPI.getCompleteAddressString(marker.getPosition().latitude, marker.getPosition().longitude, this));
        autoCompleteTextViewOrigen.setText(PlaceAPI.getCompleteAddressString(marker.getPosition().latitude, marker.getPosition().longitude, this));

        markerEvento.setPosition(marker.getPosition());
        markerEvento.setTitle(marker.getTitle());
    }


}
