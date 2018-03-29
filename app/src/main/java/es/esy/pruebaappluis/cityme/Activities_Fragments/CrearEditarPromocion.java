package es.esy.pruebaappluis.cityme.Activities_Fragments;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import es.esy.pruebaappluis.cityme.BuildConfig;
import es.esy.pruebaappluis.cityme.Modelo.Promocion;
import es.esy.pruebaappluis.cityme.R;
import es.esy.pruebaappluis.cityme.Volley.PeticionesServidorVolley;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Gestiona la creación, edición y eliminación de promociones.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class CrearEditarPromocion extends AppCompatActivity {

    private EditText nombre;
    private EditText descripcion;
    private Button botonOk;
    private ImageView imagen;
    private Button botonEliminar;
    private CoordinatorLayout vista;
    private String tipoPromocion;
    private Promocion promocionEditada;

    // Constantes de finalización
    private final int IMAGEN_GALERIA = 4;
    private final int IMAGEN_CAMARA = 5;
    // Constante de permisos
    private final int MY_PERMISSIONS = 100;

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
        setContentView(R.layout.activity_crear_editar_promocion);

        // Inflate the layout for this fragment
        View view = getLayoutInflater().inflate(R.layout.activity_crear_editar_promocion, null);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if(keyEvent.getAction()==KeyEvent.ACTION_DOWN){
                    if(keyCode==KeyEvent.KEYCODE_BACK){
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

        nombre = (EditText) findViewById(R.id.nombrePromocion);
        descripcion = (EditText) findViewById(R.id.descripcionPromocion);
        botonOk = (Button) findViewById(R.id.botonOk);
        botonEliminar = (Button) findViewById(R.id.botonEliminar);

        vista=(CoordinatorLayout) findViewById(R.id.activity_crear_editar_promocion);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getText(R.string.por_favor_espere));
        pDialog.setCancelable(false);

        imagen = (ImageView) findViewById(R.id.imageButtonView);
        imagen.setEnabled(true);

        if(mayRequestStoragePermission())
            imagen.setEnabled(true);

        imagen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mayRequestStoragePermission();
                captura_galeria();
            }
        });

        botonOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(TextUtils.equals(tipoPromocion,"crear")){
                    solicitudCrearNuevaPromocion();
                }else if (TextUtils.equals(tipoPromocion,"editar")) {
                    solicitudEditarPromocion();
                }

            }
        });
        botonEliminar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(CrearEditarPromocion.this);
                builder.setTitle(R.string.confirmacion);
                builder.setMessage(R.string.desea_eliminar_promocion);
                builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        solicitudEliminarPromocion();

                    }
                });
                builder.setNegativeButton(R.string.aceptar, new DialogInterface.OnClickListener() {
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

        //Se comprueba si es una nueva promoción o es la edición de una ya existente

        tipoPromocion = MainActivity.tipoPromocion;
        if(TextUtils.equals(tipoPromocion,"crear")){

            getSupportActionBar().setTitle(R.string.crear_promocion);

            botonEliminar.setVisibility(View.INVISIBLE);
            imagen.setImageResource(R.drawable.add_image);

        }else if (TextUtils.equals(tipoPromocion,"editar")) {

            getSupportActionBar().setTitle(R.string.editar_promocion);

            promocionEditada = MainActivity.promocion;
            nombre.setText(promocionEditada.getNombre());
            descripcion.setText(promocionEditada.getDescripcion());
            promocionEditada.setImagen(promocionEditada.getImagen());
            imagen.setImageBitmap(promocionEditada.getImagenBitmap());

            botonEliminar.setVisibility(View.VISIBLE);

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
     * Realiza la petición POST al servicio REST indicando la creación de una nueva promoción.
     */
    private void solicitudCrearNuevaPromocion() {

        String uriTodasPromociones = "http://pruebaappluis.esy.es/promociones";

        if (!PeticionesServidorVolley.compruebaConexion(this)) {
            Toast.makeText(this,R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(nombre.getText().toString())) {
            Toast.makeText(this, R.string.introduzca_nombre_promocion, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(descripcion.getText().toString())) {
            Toast.makeText(this, R.string.introduzca_descripcion_promocion, Toast.LENGTH_SHORT).show();
            return;
        }
        if(imagen.getDrawable().getConstantState().equals(ContextCompat.getDrawable(this, R.drawable.add_image).getConstantState())){
            Toast.makeText(this, R.string.selecciona_imagen_promocion, Toast.LENGTH_SHORT).show();
            return;
        }

        showpDialog();

        // Se establecen los parametros que van asociados a la petición
        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("nombre_promocion", nombre.getText().toString());
        parametros.put("descripcion_promocion", descripcion.getText().toString());

        // Se obtiene el Bitmap de la imagen
        BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        // Se transforma el Bitmap a Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
        byte[] imagenBytes = baos.toByteArray();
        String imagen = Base64.encodeToString(imagenBytes,Base64.DEFAULT);

        parametros.put("imagen_promocion", imagen);
        parametros.put("id_usuario", MainActivity.usuario.getId());

        // Se obtiene el objeto JSON que irá asociado a la petición
        JSONObject parametrosJson = new JSONObject(parametros);

        // PETICIÓN POST
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                uriTodasPromociones, parametrosJson, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());


                try {
                    if(response.getInt("estado")==1) { //EXITO
                        hidepDialog();

                        setResult(1002); // Promocion creada
                        finish();
                    }
                    else { // ERROR API
                        Toast.makeText(CrearEditarPromocion.this,
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
                Toast.makeText(CrearEditarPromocion.this,
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
     * Realiza la petición PUT al servicio REST indicando la modificación de la información de la promoción.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void solicitudEditarPromocion() {

        String uriTodasPromociones = "http://pruebaappluis.esy.es/promociones";

        if (!PeticionesServidorVolley.compruebaConexion(this)) {
            Toast.makeText(this,R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(nombre.getText().toString())) {
            Toast.makeText(this, R.string.introduzca_nombre_promocion, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(descripcion.getText().toString())) {
            Toast.makeText(this, R.string.introduzca_descripcion_promocion, Toast.LENGTH_SHORT).show();
            return;
        }
        if(imagen.getDrawable().getConstantState().equals(ContextCompat.getDrawable(this, R.drawable.add_image).getConstantState())){
            Toast.makeText(this, R.string.selecciona_imagen_promocion, Toast.LENGTH_SHORT).show();
            return;
        }


        showpDialog();

        // Se establecen los parametros que van asociados a la petición
        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("nombre_promocion", nombre.getText().toString());
        parametros.put("descripcion_promocion", descripcion.getText().toString());

        // Se obtiene el Bitmap de la imagen
        BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        // Se transforma el Bitmap a Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
        byte[] imagenBytes = baos.toByteArray();
        String imagen = Base64.encodeToString(imagenBytes,Base64.DEFAULT);

        parametros.put("imagen_promocion", imagen);

        // Se obtiene el objeto JSON que irá asociado a la petición
        JSONObject parametrosJson = new JSONObject(parametros);

        promocionEditada.setNombre(nombre.getText().toString());
        promocionEditada.setDescripcion(descripcion.getText().toString());
        promocionEditada.setImagen(imagen);

        MainActivity.promocion = promocionEditada;

        // PETICIÓN PUT
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.PUT,
                uriTodasPromociones + "/" + promocionEditada.getId(), parametrosJson, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {
                    if(response.getInt("estado")==1) { //EXITO
                        hidepDialog();


                        setResult(1000); // Promocion editada
                        finish();
                    }
                    else { // ERROR API
                        Toast.makeText(CrearEditarPromocion.this,
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
                Toast.makeText(CrearEditarPromocion.this,
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
     * Realiza la petición DELETE al servicio REST indicando la eliminación de la promoción.
     */
    private void solicitudEliminarPromocion() {

        String uriTodasPromociones = "http://pruebaappluis.esy.es/promociones";

        if (!PeticionesServidorVolley.compruebaConexion(this)) {
            Toast.makeText(this,R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        showpDialog();

        // PETICIÓN DELETE
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.DELETE,
                uriTodasPromociones + "/promocion/" + promocionEditada.getId(), null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());


                try {
                    if(response.getInt("estado")==1) { //EXITO

                        hidepDialog();

                        setResult(1001); // Promocion eliminada
                        finish();

                    }
                    else { // ERROR API
                        Toast.makeText(CrearEditarPromocion.this,
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
                Toast.makeText(CrearEditarPromocion.this,
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
     * Indica si se desea acceder a la cámara o a la galería
     * y procesa la acción indicada.
     */
    private void captura_galeria() {

        try{
            final CharSequence[] items = {getText(R.string.seleccionar_galeria), getText(R.string.seleccionar_camara)};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.seleccionar_foto);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch(item){
                        case 0:
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent, IMAGEN_GALERIA);
                            break;
                        case 1:


                            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), MEDIA_DIRECTORY);
                            boolean isDirectoryCreated = dir.exists();

                            if(!isDirectoryCreated)
                                isDirectoryCreated = dir.mkdirs();

                            if(isDirectoryCreated){

                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
                                String date = dateFormat.format(new Date() );
                                String nombre = "pic_" + date + ".jpg";

                                File file = new File(dir,nombre);
                                rutaFoto = "file:" + file.getAbsolutePath();
                                Uri fotoURI = FileProvider.getUriForFile(CrearEditarPromocion.this, BuildConfig.APPLICATION_ID + ".provider",file);

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
        } catch(Exception e){}


    }


    /**
     * Procesa los resultados obtenidos tras acceder a otras Actividades,
     * como son la cámara, la galería.
     *
     * @param requestCode permite identificar de qué Actividad procede el resultado
     * @param resultCode código de resultado devuelto por la otra Actividad
     * @param data puede contener datos devueltos por la Actividad
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case IMAGEN_CAMARA:

                    Uri imageUri = Uri.parse(rutaFoto);
                    File file = new File(imageUri.getPath());

                    InputStream ims = null;
                    try {
                        ims = new FileInputStream(file);
                        imagen.setImageBitmap(BitmapFactory.decodeStream(ims));

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }


                    MediaScannerConnection.scanFile(this,
                            new String[]{imageUri.getPath()},null,
                            new MediaScannerConnection.OnScanCompletedListener(){

                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> Uri = " + uri);
                                }
                            });


                    break;
                case IMAGEN_GALERIA:

                    Uri path = data.getData();
                    imagen.setImageURI(path);

                    break;

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

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        if((ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED))
            return true;

        if((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) || (shouldShowRequestPermissionRationale(CAMERA))){
            Snackbar.make(vista, R.string.permisos_son_necesarios_app,
                    Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
                }
            });
        }else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
        }

        return false;
    }

    /**
     * Gestiona el resultado de la petición de permisos, en este caso,
     * de los permisos de la cámara y la galería.
     *
     * @param requestCode permite identificar de dónde procede el resultado
     * @param permissions permisos solicitados
     * @param grantResults indica si el permiso ha sido concedido o denegado
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MY_PERMISSIONS){
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, R.string.permisos_aceptados, Toast.LENGTH_SHORT).show();
                imagen.setEnabled(true);
            }
        }else{
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
                Uri uri = Uri.fromParts("package", CrearEditarPromocion.this.getPackageName(), null);
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
}
