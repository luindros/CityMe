package es.esy.pruebaappluis.cityme.Activities_Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import es.esy.pruebaappluis.cityme.MapsUtilities.PlacesAPI.PlaceAPI;
import es.esy.pruebaappluis.cityme.Modelo.Evento;
import es.esy.pruebaappluis.cityme.Modelo.Promocion;
import es.esy.pruebaappluis.cityme.Modelo.Usuario;
import es.esy.pruebaappluis.cityme.R;
import es.esy.pruebaappluis.cityme.RecyclerViewUtilities.RecyclerAsistentesAdapter_DescripcionEvento;
import es.esy.pruebaappluis.cityme.RecyclerViewUtilities.RecyclerImagenesAnterioresAdapter_DescripcionEvento;
import es.esy.pruebaappluis.cityme.RecyclerViewUtilities.RecyclerItemClickListener;
import es.esy.pruebaappluis.cityme.RecyclerViewUtilities.RecyclerPromocionesAdapter_DescripcionEvento;
import es.esy.pruebaappluis.cityme.Utilities.TouchImageView;
import es.esy.pruebaappluis.cityme.Volley.PeticionesServidorVolley;

/**
 * Gestiona la descripción de un evento.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class DescripcionEvento extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private String[] listaMeses = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};

    private TextView descripcion;
    private TextView fechaInicio;
    private TextView fechaFin;
    private ImageView imagenCreadorEvento;
    private TextView nombreCreadorEvento;
    private ImageButton btnEditar;
    private Button btnAsistencia;

    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView indicadorPromociones;
    private RecyclerView recyclerViewPromociones;
    ArrayList<Promocion> listaPromociones = new ArrayList<>();

    private TextView indicadorAsistentes;
    private RecyclerView recyclerViewAsistentes;
    ArrayList<Usuario> listaAsistentes = new ArrayList<>();

    private TextView indicadorImagenesAnteriores;
    private RecyclerView recyclerViewImagenesAnteriores;
    ArrayList<String> listaImagenesAnteriores = new ArrayList<>();

    private ImageButton mapa;
    private TextView localizacion;
    private TextView lugar;
    private static final String MAP_API_KEY = "AIzaSyAN_-OvQwPY9xJRYx3HzofNaUFHkcYjcyI";

    private static final int CODIGO_EDITAR_EVENTO = 1;

    Evento evento;

    private static String TAG = DescripcionEvento.class.getSimpleName();

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
        setContentView(R.layout.activity_descripcion_evento);

        // Inflate the layout for this fragment
        View view = getLayoutInflater().inflate(R.layout.activity_descripcion_evento, null);

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

        net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout collapsingToolbar = (net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);

        imagenCreadorEvento = (ImageView) findViewById(R.id.imageViewCreadorEvento);
        nombreCreadorEvento = (TextView) findViewById(R.id.textViewNombreCreadorEvento);

        fechaInicio = (TextView) findViewById(R.id.fechaInicio);
        fechaFin = (TextView) findViewById(R.id.fechaFin);

        evento = MainActivity.evento;

        ImageView collapsingToolbarImage = (ImageView) findViewById(R.id.header);
        collapsingToolbarImage.setImageBitmap(evento.getImagenBitmap());
        collapsingToolbarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog = new Dialog(DescripcionEvento.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.imagen_completa);

                TouchImageView imagen_ampliada = (TouchImageView) dialog.findViewById(R.id.imagen_ampliada);
                imagen_ampliada.setImageBitmap(evento.getImagenBitmap());

                dialog.show();
            }
        });

        collapsingToolbar.setTitle(evento.getNombre());

        String[] separadorFecha_Hora_Inicio = evento.getFecha_inicio().split(" ");
        String[] separadorFecha_Inicio = separadorFecha_Hora_Inicio[0].split("-");
        int mesInicio = Integer.parseInt(separadorFecha_Inicio[1])-1;
        fechaInicio.setText(separadorFecha_Inicio[0] + " " + getText(R.string.de) + " " + listaMeses[mesInicio] + " " + getText(R.string.del) + " " + separadorFecha_Inicio[2] + " " + getText(R.string.a_las) + " " + separadorFecha_Hora_Inicio[1]);

        String[] separadorFecha_Hora_Fin = evento.getFecha_fin().split(" ");
        String[] separadorFecha_Fin = separadorFecha_Hora_Fin[0].split("-");
        int mesFin = Integer.parseInt(separadorFecha_Fin[1])-1;
        fechaFin.setText(getText(R.string.hasta_el) + " " + separadorFecha_Fin[0] + " " + getText(R.string.de) + " " + listaMeses[mesFin] + " " + getText(R.string.del) + " " + separadorFecha_Fin[2] + " " + getText(R.string.a_las) + " " + separadorFecha_Hora_Fin[1]);

        Picasso.with(this).load(evento.getUsuarioCreador().getImagenUrl()).into(imagenCreadorEvento);
        imagenCreadorEvento.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("fb://facewebmodal/f?href="+evento.getUsuarioCreador().getLink()); //desde Facebook
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
            }
        });
        nombreCreadorEvento.setText(evento.getUsuarioCreador().getNombre());

        descripcion = (TextView) findViewById(R.id.descripcion);
        descripcion.setText(evento.getDescripcion());

        btnEditar = (ImageButton) findViewById(R.id.btnEditar);
        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.tipoEvento = "editar";
                Intent intent = new Intent(DescripcionEvento.this,CrearEditarEvento.class);
                startActivityForResult(intent,CODIGO_EDITAR_EVENTO);
            }
        });
        if(TextUtils.equals(MainActivity.usuario.getId(),evento.getUsuarioCreador().getId())) {
            btnEditar.setVisibility(View.VISIBLE);
        }
        else{
            btnEditar.setVisibility(View.INVISIBLE);
        }

        btnAsistencia = (Button) findViewById(R.id.btnAsistencia);
        btnAsistencia.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                gestionarAsistencia();
            }
        });

        indicadorPromociones = (TextView) findViewById(R.id.indicadorPromociones);
        indicadorPromociones.setText(R.string.cargando_promociones);
        indicadorPromociones.setGravity(Gravity.CENTER_HORIZONTAL);

        recyclerViewPromociones = (RecyclerView) findViewById(R.id.recyclerViewPromociones);
        final RecyclerPromocionesAdapter_DescripcionEvento adapter_promociones = new RecyclerPromocionesAdapter_DescripcionEvento(listaPromociones);
        final LinearLayoutManager linearLayoutManager_p = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false);
        recyclerViewPromociones.setLayoutManager(linearLayoutManager_p);
        recyclerViewPromociones.setAdapter(adapter_promociones);
        recyclerViewPromociones.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Promocion promocion = listaPromociones.get(position);
                        MainActivity.promocion = promocion;
                        Intent intent = new Intent(DescripcionEvento.this,DescripcionPromocion.class);
                        startActivity(intent);
                    }
                })
        );

        indicadorAsistentes = (TextView) findViewById(R.id.indicadorAsistentes);
        indicadorAsistentes.setText(R.string.cargando_asistentes);
        indicadorAsistentes.setGravity(Gravity.CENTER_HORIZONTAL);

        recyclerViewAsistentes = (RecyclerView) findViewById(R.id.recyclerViewAsistentes);
        final RecyclerAsistentesAdapter_DescripcionEvento adapter_asistentes = new RecyclerAsistentesAdapter_DescripcionEvento(listaAsistentes);
        final LinearLayoutManager linearLayoutManager_a = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false);
        recyclerViewAsistentes.setLayoutManager(linearLayoutManager_a);
        recyclerViewAsistentes.setAdapter(adapter_asistentes);
        recyclerViewAsistentes.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Usuario asistente = listaAsistentes.get(position);
                        Uri uri = Uri.parse("fb://facewebmodal/f?href="+asistente.getLink()); //desde Facebook
                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                        startActivity(intent);
                    }
                })
        );

        indicadorImagenesAnteriores = (TextView) findViewById(R.id.indicadorImagenesAnteriores);
        indicadorImagenesAnteriores.setText(R.string.cargando_imagenes_anteriores);
        indicadorImagenesAnteriores.setGravity(Gravity.CENTER_HORIZONTAL);

        recyclerViewImagenesAnteriores = (RecyclerView) findViewById(R.id.recyclerViewImagenesAnteriores);
        final LinearLayoutManager linearLayoutManager_i = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false);
        recyclerViewImagenesAnteriores.setLayoutManager(linearLayoutManager_i);
        final RecyclerImagenesAnterioresAdapter_DescripcionEvento adapter_imagenes_anteriores = new RecyclerImagenesAnterioresAdapter_DescripcionEvento(listaImagenesAnteriores);
        recyclerViewImagenesAnteriores.setAdapter(adapter_imagenes_anteriores);
        recyclerViewImagenesAnteriores.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {

                        final String imagenBase64 = listaImagenesAnteriores.get(position);

                        final Dialog dialog = new Dialog(DescripcionEvento.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.imagen_completa);

                        TouchImageView imagen_ampliada = (TouchImageView) dialog.findViewById(R.id.imagen_ampliada);
                        byte[] byteimagen = Base64.decode(imagenBase64, Base64.DEFAULT);
                        Bitmap imagenBitmap = BitmapFactory.decodeByteArray(byteimagen, 0, byteimagen.length);
                        imagen_ampliada.setImageBitmap(imagenBitmap);

                        dialog.show();

                    }
                })
        );

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        mapa = (ImageButton) findViewById(R.id.map);
        mapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(DescripcionEvento.this,MapaCompleto.class);
                intent.putExtra("latitudEvento",evento.getLatitud());
                intent.putExtra("longitudEvento",evento.getLongitud());
                startActivity(intent);
            }
        });
        mapa.post(new Runnable() {
            @Override
            public void run() {

                String uriMapaEstatico = "https://maps.googleapis.com/maps/api/staticmap?center="+evento.getLatitud()+","+evento.getLongitud()+"&zoom=16&size=600x300&markers=color:red%7C"+evento.getLatitud()+","+evento.getLongitud()+"&key="+MAP_API_KEY+"&sensor=true";
                Picasso.with(DescripcionEvento.this).load(uriMapaEstatico).resize(mapa.getWidth(),mapa.getHeight()).into(mapa);

            }
        });

        localizacion = (TextView) findViewById(R.id.localizacion);
        localizacion.setText(PlaceAPI.getCompleteAddressString(evento.getLatitud(),evento.getLongitud(),this));

        lugar = (TextView) findViewById(R.id.lugar);
        lugar.setText(evento.getLugar());

        solicitudPromocionesAsociadas();
        solicitudListaAsistentes();
        solicitudImagenesAnterioresDelEvento();
        comprobarAsistencia();

    }

    /**
     * Es llamado cuando se realiza el gesto de deslizar sobre la pantalla.
     * Por tanto, se actualizan los datos correspondiente.
     */
    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);

        evento = MainActivity.evento;

        ImageView collapsingToolbarImage = (ImageView) findViewById(R.id.header);
        collapsingToolbarImage.setImageBitmap(evento.getImagenBitmap());

        net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout collapsingToolbar = (net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        collapsingToolbar.setTitle(evento.getNombre());

        String[] separadorFecha_Hora_Inicio = evento.getFecha_inicio().split(" ");
        String[] separadorFecha_Inicio = separadorFecha_Hora_Inicio[0].split("-");
        int mesInicio = Integer.parseInt(separadorFecha_Inicio[1])-1;
        fechaInicio.setText(separadorFecha_Inicio[0] + " de " + listaMeses[mesInicio] + " del " + separadorFecha_Inicio[2] + " a las " + separadorFecha_Hora_Inicio[1]);

        String[] separadorFecha_Hora_Fin = evento.getFecha_fin().split(" ");
        String[] separadorFecha_Fin = separadorFecha_Hora_Fin[0].split("-");
        int mesFin = Integer.parseInt(separadorFecha_Fin[1])-1;
        fechaFin.setText("Hasta el " + separadorFecha_Fin[0] + " de " + listaMeses[mesFin] + " del " + separadorFecha_Fin[2] + " a las " + separadorFecha_Hora_Fin[1]);

        Picasso.with(this).load(evento.getUsuarioCreador().getImagenUrl()).into(imagenCreadorEvento);

        nombreCreadorEvento.setText(evento.getUsuarioCreador().getNombre());

        descripcion.setText(evento.getDescripcion());

        indicadorPromociones.setText(R.string.cargando_promociones);
        indicadorPromociones.setTextColor(Color.BLACK);
        indicadorPromociones.setGravity(Gravity.CENTER_HORIZONTAL);
        listaPromociones.clear();
        RecyclerPromocionesAdapter_DescripcionEvento adapter_p = new RecyclerPromocionesAdapter_DescripcionEvento(listaPromociones);
        final LinearLayoutManager linearLayoutManager_p = new LinearLayoutManager(DescripcionEvento.this, LinearLayoutManager.HORIZONTAL,false);
        recyclerViewPromociones.setLayoutManager(linearLayoutManager_p);
        recyclerViewPromociones.setAdapter(adapter_p);
        solicitudPromocionesAsociadas();

        indicadorAsistentes.setText(R.string.cargando_asistentes);
        indicadorAsistentes.setTextColor(Color.BLACK);
        indicadorAsistentes.setGravity(Gravity.CENTER_HORIZONTAL);
        listaAsistentes.clear();
        RecyclerAsistentesAdapter_DescripcionEvento adapter_a = new RecyclerAsistentesAdapter_DescripcionEvento(listaAsistentes);
        final LinearLayoutManager linearLayoutManager_a = new LinearLayoutManager(DescripcionEvento.this, LinearLayoutManager.HORIZONTAL,false);
        recyclerViewAsistentes.setLayoutManager(linearLayoutManager_a);
        recyclerViewAsistentes.setAdapter(adapter_a);
        solicitudListaAsistentes();

        indicadorImagenesAnteriores.setText(R.string.cargando_imagenes_anteriores);
        indicadorImagenesAnteriores.setTextColor(Color.BLACK);
        indicadorImagenesAnteriores.setGravity(Gravity.CENTER_HORIZONTAL);
        listaImagenesAnteriores.clear();
        RecyclerImagenesAnterioresAdapter_DescripcionEvento adapter_i = new RecyclerImagenesAnterioresAdapter_DescripcionEvento(listaImagenesAnteriores);
        final LinearLayoutManager linearLayoutManager_i = new LinearLayoutManager(DescripcionEvento.this, LinearLayoutManager.HORIZONTAL,false);
        recyclerViewImagenesAnteriores.setLayoutManager(linearLayoutManager_i);
        recyclerViewImagenesAnteriores.setAdapter(adapter_i);
        solicitudImagenesAnterioresDelEvento();

        comprobarAsistencia();

        String uriMapaEstatico = "https://maps.googleapis.com/maps/api/staticmap?center="+evento.getLatitud()+","+evento.getLongitud()+"&zoom=16&size=600x300&markers=color:red%7C"+evento.getLatitud()+","+evento.getLongitud()+"&key="+MAP_API_KEY+"&sensor=true";
        Picasso.with(DescripcionEvento.this).load(uriMapaEstatico).resize(mapa.getWidth(),mapa.getHeight()).into(mapa);

        localizacion.setText(PlaceAPI.getCompleteAddressString(evento.getLatitud(),evento.getLongitud(),this));

        lugar.setText(evento.getLugar());

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
     * Procesa los resultados obtenidos tras acceder a otras Actividades.
     * Permite determinar si el evento ha sido eliminado u editado, y realizar
     * la acción correspondiente.
     *
     * @param requestCode permite identificar de qué Actividad procede el resultado
     * @param resultCode código de resultado devuelto por la otra Actividad
     * @param data puede contener datos devueltos por la Actividad
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == 1001){ // Se ha eliminado el evento
            finish();
        }
        else if(resultCode == 1000) { // Se ha editado el evento
            this.onRefresh();
        }
    }

    /**
     * Realiza la petición GET al servicio REST mediante la cual se obtienen
     * todas las promociones del evento.
     */
    private void solicitudPromocionesAsociadas() {

        if (!PeticionesServidorVolley.compruebaConexion(this)) {
            Toast.makeText(this,R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        String uri = "http://pruebaappluis.esy.es/promociones/evento/" + evento.getId();

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

                            Promocion promocion = new Promocion(id_promocion,nombre_promocion,descripcion_promocion,usuario);
                            promocion.setImagen(imagen_promocion);

                            listaPromociones.add(promocion);
                        }

                        if(listaPromociones.size() > 0) {

                            indicadorPromociones.setText(R.string.promociones_del_evento);
                            indicadorPromociones.setGravity(Gravity.NO_GRAVITY);
                            indicadorPromociones.setTextColor(Color.BLACK);

                        }else{

                            indicadorPromociones.setText(R.string.evento_sin_promociones);
                            indicadorPromociones.setGravity(Gravity.CENTER_HORIZONTAL);
                            indicadorPromociones.setTextColor(Color.rgb(117,117,117));
                        }

                        RecyclerPromocionesAdapter_DescripcionEvento adapter = new RecyclerPromocionesAdapter_DescripcionEvento(listaPromociones);
                        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DescripcionEvento.this, LinearLayoutManager.HORIZONTAL,false);
                        recyclerViewPromociones.setLayoutManager(linearLayoutManager);
                        recyclerViewPromociones.setAdapter(adapter);

                    }
                    else { // ERROR API
                        Toast.makeText(DescripcionEvento.this,
                                response.getString("datos"), Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(DescripcionEvento.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(DescripcionEvento.this,
                        R.string.error_conexion, Toast.LENGTH_SHORT).show();

                indicadorPromociones.setText(R.string.error_cargar_promociones);
                indicadorPromociones.setGravity(Gravity.CENTER_HORIZONTAL);
                indicadorPromociones.setTextColor(Color.rgb(117,117,117));

            }
        });

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }

    /**
     * Realiza la petición GET al servicio REST mediante la cual se obtienen
     * los asistentes al evento.
     */
    private void solicitudListaAsistentes() {

        if (!PeticionesServidorVolley.compruebaConexion(this)) {
            Toast.makeText(this,R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        String uri = "http://pruebaappluis.esy.es/asistentes/evento/" + evento.getId();

        // PETICIÓN GET
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                uri, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {

                    if(response.getInt("estado") == 1) { // EXITO

                        listaAsistentes.clear();

                        JSONArray datos = response.getJSONArray("datos");
                        for (int i = 0; i < datos.length(); i++) {
                            JSONObject c = datos.getJSONObject(i);

                            String id_usuario = c.getString("id_usuario");
                            String nombre_usuario = c.getString("nombre_usuario");
                            String email_usuario = c.getString("email_usuario");
                            String link_usuario = c.getString("link_usuario");
                            String token_usuario = c.getString("token_usuario");
                            String imagenUrl_usuario = c.getString("imagenUrl_usuario");
                            Usuario usuario = new Usuario(id_usuario, nombre_usuario, email_usuario, link_usuario, token_usuario, imagenUrl_usuario);


                            listaAsistentes.add(usuario);
                        }

                        if(listaAsistentes.size() > 0) {

                            indicadorAsistentes.setText(R.string.asistentes_del_evento);
                            indicadorAsistentes.setGravity(Gravity.NO_GRAVITY);
                            indicadorAsistentes.setTextColor(Color.BLACK);

                        }else{

                            indicadorAsistentes.setText(R.string.evento_sin_asistentes);
                            indicadorAsistentes.setGravity(Gravity.CENTER_HORIZONTAL);
                            indicadorAsistentes.setTextColor(Color.rgb(117,117,117));
                        }

                        RecyclerAsistentesAdapter_DescripcionEvento adapter = new RecyclerAsistentesAdapter_DescripcionEvento(listaAsistentes);
                        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DescripcionEvento.this, LinearLayoutManager.HORIZONTAL,false);
                        recyclerViewAsistentes.setLayoutManager(linearLayoutManager);
                        recyclerViewAsistentes.setAdapter(adapter);
                    }
                    else { // ERROR API
                        Toast.makeText(DescripcionEvento.this,
                                response.getString("datos"), Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(DescripcionEvento.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(DescripcionEvento.this,
                        R.string.error_conexion, Toast.LENGTH_SHORT).show();

                indicadorAsistentes.setText(R.string.error_cargar_asistentes);
                indicadorAsistentes.setGravity(Gravity.CENTER_HORIZONTAL);
                indicadorAsistentes.setTextColor(Color.rgb(117,117,117));
            }
        });

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }

    /**
     * Realiza la petición GET al servicio REST mediante la cual se obtienen
     * todas las imágenes de ediciones anteriores del evento.
     */
    private void solicitudImagenesAnterioresDelEvento() {

        if (!PeticionesServidorVolley.compruebaConexion(this)) {
            Toast.makeText(this,R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        String uri = "http://pruebaappluis.esy.es/eventos/imagenes/" + evento.getId();

        // PETICIÓN GET
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                uri, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {

                    if(response.getInt("estado") == 1) { // EXITO

                        listaImagenesAnteriores.clear();

                        JSONArray datos = response.getJSONArray("datos");
                        for (int i = 0; i < datos.length(); i++) {
                            JSONObject c = datos.getJSONObject(i);

                            String imagen = c.getString("imagen");

                            listaImagenesAnteriores.add(imagen);
                        }

                        if(listaImagenesAnteriores.size() > 0) {

                            indicadorImagenesAnteriores.setText(R.string.imagenes_anteriores_del_evento);
                            indicadorImagenesAnteriores.setGravity(Gravity.NO_GRAVITY);
                            indicadorImagenesAnteriores.setTextColor(Color.BLACK);

                        }else{

                            indicadorImagenesAnteriores.setText(R.string.evento_sin_imagenes_anteriores);
                            indicadorImagenesAnteriores.setGravity(Gravity.CENTER_HORIZONTAL);
                            indicadorImagenesAnteriores.setTextColor(Color.rgb(117,117,117));
                        }

                        RecyclerImagenesAnterioresAdapter_DescripcionEvento adapter = new RecyclerImagenesAnterioresAdapter_DescripcionEvento(listaImagenesAnteriores);
                        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DescripcionEvento.this, LinearLayoutManager.HORIZONTAL,false);
                        recyclerViewImagenesAnteriores.setLayoutManager(linearLayoutManager);
                        recyclerViewImagenesAnteriores.setAdapter(adapter);

                    }
                    else { // ERROR API
                        Toast.makeText(DescripcionEvento.this,
                                response.getString("datos"), Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(DescripcionEvento.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(DescripcionEvento.this,
                        R.string.error_conexion, Toast.LENGTH_SHORT).show();

                indicadorImagenesAnteriores.setText(R.string.error_cargar_imagenes_anteriores);
                indicadorImagenesAnteriores.setGravity(Gravity.CENTER_HORIZONTAL);
                indicadorImagenesAnteriores.setTextColor(Color.rgb(117,117,117));

            }
        });

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }

    /**
     * Realiza la petición GET al servicio REST mediante la cual se comprueba si el usuario
     * asiste o no al evento.
     */
    private void comprobarAsistencia() {

        if (!PeticionesServidorVolley.compruebaConexion(this)) {
            Toast.makeText(this,R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        String uri = "http://pruebaappluis.esy.es/asistentes/evento_usuario/" + evento.getId() + "/" + MainActivity.usuario.getId();

        // PETICIÓN GET
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                uri, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {

                    if(response.getInt("estado") == 1) { // EXITO

                        String comprobacionAsistencia = response.getString("datos");
                        if(TextUtils.equals(comprobacionAsistencia,"yes")) {
                            btnAsistencia.setText(R.string.no_asistir);
                        }else {
                            btnAsistencia.setText(R.string.asistir);
                        }
                    }
                    else { // ERROR API
                        Toast.makeText(DescripcionEvento.this,
                                response.getString("datos"), Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(DescripcionEvento.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(DescripcionEvento.this,
                        R.string.error_conexion, Toast.LENGTH_SHORT).show();
            }
        });

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }

    /**
     * Gestiona la asistencia del usuario al evento.
     * <p>
     * Si el usuario solicita Asistir, realiza la petición POST al servicio REST
     * indicando que existe un nuevo usuario que asiste al evento.
     * <p></p>
     * Si el usuario solicita Dejar de Asistir, realiza la petición DELETE al servicio REST
     * indicando que el usuario ya no asiste al evento.
     */
    private void gestionarAsistencia() {

        if (!PeticionesServidorVolley.compruebaConexion(this)) {
            Toast.makeText(this,R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.equals(btnAsistencia.getText().toString(),getText(R.string.asistir))) {

            String uri = "http://pruebaappluis.esy.es/asistentes/" + evento.getId() + "/" + MainActivity.usuario.getId();

            // PETICIÓN POST
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    uri, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());

                    try {

                        if(response.getInt("estado")==1) { // EXITO

                            btnAsistencia.setText(R.string.no_asistir);
                            solicitudListaAsistentes();
                        }
                        else { // ERROR API
                            Toast.makeText(DescripcionEvento.this,
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
                    Toast.makeText(DescripcionEvento.this,
                            R.string.error_conexion, Toast.LENGTH_SHORT).show();
                }
            });

            // Se añade la petición a la Cola de Peticiones de Volley
            PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);


        }else if (TextUtils.equals(btnAsistencia.getText().toString(),getText(R.string.no_asistir))) {

            String uri = "http://pruebaappluis.esy.es/asistentes/" + evento.getId() + "/" + MainActivity.usuario.getId();

            // PETICIÓN DELETE
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.DELETE,
                    uri, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());

                    try {

                        if(response.getInt("estado")==1) { // EXITO

                            btnAsistencia.setText(R.string.asistir);
                            solicitudListaAsistentes();
                        }
                        else { // ERROR API
                            Toast.makeText(DescripcionEvento.this,
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
                    Toast.makeText(DescripcionEvento.this,
                            R.string.error_conexion, Toast.LENGTH_SHORT).show();
                }
            });

            // Se añade la petición a la Cola de Peticiones de Volley
            PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);


        }

    }


}
