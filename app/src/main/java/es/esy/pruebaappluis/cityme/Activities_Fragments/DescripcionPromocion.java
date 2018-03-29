package es.esy.pruebaappluis.cityme.Activities_Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import es.esy.pruebaappluis.cityme.Modelo.Promocion;
import es.esy.pruebaappluis.cityme.R;

/**
 * Gestiona la descripción de una promoción.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class DescripcionPromocion extends AppCompatActivity {


    private CircleImageView imagen;
    private TextView nombre;
    private TextView descripcion;

    private ImageButton btnEditar;

    Promocion promocion;

    private static String TAG = DescripcionPromocion.class.getSimpleName();

    private static final int CODIGO_EDITAR_PROMOCION = 1;

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
        setContentView(R.layout.activity_descripcion_promocion);

        // Inflate the layout for this fragment
        View view = getLayoutInflater().inflate(R.layout.activity_descripcion_promocion, null);

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


        imagen = (CircleImageView) findViewById(R.id.imagen);
        nombre = (TextView) findViewById(R.id.nombre);
        descripcion = (TextView) findViewById(R.id.descripcion);

        promocion = MainActivity.promocion;

        getSupportActionBar().setTitle(promocion.getNombre());

        imagen.setImageBitmap(promocion.getImagenBitmap());
        nombre.setText(promocion.getNombre());
        descripcion.setText(promocion.getDescripcion());

        btnEditar = (ImageButton) findViewById(R.id.btnEditar);
        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.tipoPromocion = "editar";
                Intent intent = new Intent(DescripcionPromocion.this,CrearEditarPromocion.class);
                startActivityForResult(intent,CODIGO_EDITAR_PROMOCION);
            }
        });
        if(TextUtils.equals(MainActivity.usuario.getId(),promocion.getUsuarioCreador().getId())) {
            btnEditar.setVisibility(View.VISIBLE);
        }
        else{
            btnEditar.setVisibility(View.INVISIBLE);
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
     * Procesa los resultados obtenidos tras acceder a otras Actividades.
     * Permite determinar si la promoción ha sido eliminada u editada, y realizar
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

            promocion = MainActivity.promocion;

            imagen.setImageBitmap(promocion.getImagenBitmap());
            nombre.setText(promocion.getNombre());
            descripcion.setText(promocion.getDescripcion());

        }
    }
}
