package es.esy.pruebaappluis.cityme.Activities_Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

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
import es.esy.pruebaappluis.cityme.RecyclerViewUtilities.RecyclerPromocionesAdapter_SeleccionPromocion;
import es.esy.pruebaappluis.cityme.Utilities.HidingScrollListener_EventosCreados_MisPromociones;
import es.esy.pruebaappluis.cityme.Volley.PeticionesServidorVolley;

/**
 * Gestiona la selección de promociones para un evento.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class SeleccionarPromocion extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private String uri = "http://pruebaappluis.esy.es/promociones/usuario/" + MainActivity.usuario.getId();

    private static String TAG = SeleccionarPromocion.class.getSimpleName();
    private RecyclerView recyclerView;

    ArrayList<Promocion> listaPromociones;
    RecyclerPromocionesAdapter_SeleccionPromocion adapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private FloatingActionButton botonNuevaPromocion;

    private FloatingActionButton botonOk;

    private static final int CODIGO_NUEVA_PROMOCION = 1;

    protected String tipoEvento;

    /**
     * Es llamado cuando la actividad se está iniciando.
     * Contiene la inicialización de todos los elementos de la Actividad.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_promocion);

        // Inflate the layout for this fragment
        View view = getLayoutInflater().inflate(R.layout.activity_seleccionar_promocion, null);

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
        getSupportActionBar().setTitle(R.string.seleccionar_promociones);

        botonOk = (FloatingActionButton) findViewById(R.id.botonOk);
        botonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArrayList<Promocion> promocionesSeleccionadas = new ArrayList<>();

                for (Promocion p : adapter.getAllCheckBox()) {
                    if (p.isCheckBox()){

                        promocionesSeleccionadas.add(p);
                    }
                }

                if(TextUtils.equals(tipoEvento, "crear")) {

                    MainActivity.promociones = promocionesSeleccionadas;

                }
                else if(TextUtils.equals(tipoEvento, "editar")) {

                    MainActivity.promociones = promocionesSeleccionadas;
                    MainActivity.promocionesAntiguas = promocionesSeleccionadas;

                }

                finish();
            }
        });

        botonNuevaPromocion = (FloatingActionButton) findViewById(R.id.botonNuevaPromocion);
        botonNuevaPromocion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.tipoPromocion = "crear";
                Intent intent = new Intent(SeleccionarPromocion.this,CrearEditarPromocion.class);
                startActivityForResult(intent,CODIGO_NUEVA_PROMOCION);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        adapter = new RecyclerPromocionesAdapter_SeleccionPromocion(listaPromociones);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this,1);
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

        listaPromociones = new ArrayList<>();


        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                solicitudObtenerPromociones();

            }
        });

        tipoEvento = MainActivity.tipoEvento;
    }

    /**
     * Encargado de ocultar el botón de añadir una nueva promoción cuando se desliza la pantalla.
     */
    private void hideViews() {

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) botonNuevaPromocion.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        botonNuevaPromocion.animate().translationY(botonNuevaPromocion.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(1)).start();

    }

    /**
     * Encargado de mostrar el botón de añadir una nueva promoción cuando se desliza la pantalla.
     */
    private void showViews() {

        botonNuevaPromocion.animate().translationY(0).setInterpolator(new DecelerateInterpolator(1)).start();

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
     * Permite determinar si se ha creado una nueva promoción.
     *
     * @param requestCode permite identificar de qué Actividad procede el resultado
     * @param resultCode código de resultado devuelto por la otra Actividad
     * @param data puede contener datos devueltos por la Actividad
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == 1002){ // Nueva promocion
            this.onRefresh();
        }
    }

    /**
     * Es llamado cuando se realiza el gesto de deslizar sobre la pantalla.
     * Por tanto, se actualizan los datos correspondiente.
     */
    @Override
    public void onRefresh() {
        solicitudObtenerPromociones();
    }


    /**
     * Realiza la petición GET al servicio REST mediante la cual se obtienen las promociones
     * creadas por el usuario.
     */
    private void solicitudObtenerPromociones() {

        if (!PeticionesServidorVolley.compruebaConexion(this)) {
            Toast.makeText(this,R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
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

                        adapter = new RecyclerPromocionesAdapter_SeleccionPromocion(listaPromociones);
                        final GridLayoutManager gridLayoutManager = new GridLayoutManager(SeleccionarPromocion.this,1);
                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.setAdapter(adapter);

                        if(TextUtils.equals(tipoEvento, "crear")) {

                            if(!MainActivity.promociones.isEmpty()) {

                                ArrayList<Promocion> promocionesMain = MainActivity.promociones;

                                for (int i = 0; i < promocionesMain.size(); i++) {

                                    for (int j = 0; j < recyclerView.getAdapter().getItemCount(); j++) {

                                        if (TextUtils.equals(promocionesMain.get(i).getId(), listaPromociones.get(j).getId())) {
                                            listaPromociones.get(j).setCheckBox(true);
                                        }
                                    }

                                }

                            }
                        }
                        else if(TextUtils.equals(tipoEvento, "editar")) {

                            if(!MainActivity.promocionesAntiguas.isEmpty()) {

                                ArrayList<Promocion> promocionesMain = MainActivity.promocionesAntiguas;

                                for (int i = 0; i < promocionesMain.size(); i++) {

                                    for (int j = 0; j < recyclerView.getAdapter().getItemCount(); j++) {

                                        if (TextUtils.equals(promocionesMain.get(i).getId(), listaPromociones.get(j).getId())) {
                                            listaPromociones.get(j).setCheckBox(true);
                                        }
                                    }

                                }

                            }
                        }



                    }
                    else { // ERROR API
                        Toast.makeText(SeleccionarPromocion.this,
                                response.getString("datos"), Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(SeleccionarPromocion.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                swipeRefreshLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(SeleccionarPromocion.this,
                        R.string.error_conexion, Toast.LENGTH_SHORT).show();

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }

}
