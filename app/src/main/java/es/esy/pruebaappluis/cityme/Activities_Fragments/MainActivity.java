package es.esy.pruebaappluis.cityme.Activities_Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

import java.util.ArrayList;

import es.esy.pruebaappluis.cityme.Modelo.Evento;
import es.esy.pruebaappluis.cityme.Modelo.Promocion;
import es.esy.pruebaappluis.cityme.Modelo.Usuario;
import es.esy.pruebaappluis.cityme.R;

/**
 * Gestiona las diferencias secciones de la aplicación.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity implements FragmentPrincipal.OnFragmentInteractionListener,
        FragmentConfiguracion.OnFragmentInteractionListener, FragmentCalendario.OnFragmentInteractionListener,
        FragmentCreador.OnFragmentInteractionListener, FragmentMisEventos.OnFragmentInteractionListener,
        FragmentMisPromociones.OnFragmentInteractionListener{

    private AdaptadorViewPager_MainActivity adaptadorViewPager_mainActivity;
    protected ViewPager viewPager;
    protected TabLayout tabLayout;

    protected static Usuario usuario;
    protected static Evento evento;
    protected static Promocion promocion;
    protected static ArrayList<Promocion> promociones = new ArrayList<>();
    protected static ArrayList<Promocion> promocionesAntiguas = new ArrayList<>();
    protected static String tipoEvento;
    protected static String tipoPromocion;

    /**
     * Es llamado cuando la actividad se está iniciando.
     * Contiene la inicialización de todos los elementos de la Actividad.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View view = getLayoutInflater().inflate(R.layout.activity_main, null);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if(keyEvent.getAction()==KeyEvent.ACTION_DOWN){
                    if(keyCode== KeyEvent.KEYCODE_BACK){
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

        tabLayout = (TabLayout) findViewById(R.id.TabLayoutPrincipal);

        // Se añaden las 4 tabs
        // Modo "fixed" para que todas las tabs tengan el mismo tamaño. También se asigna una gravedad centrada.
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());

        viewPager = (ViewPager) findViewById(R.id.ViewPagerPrincipal);
        adaptadorViewPager_mainActivity = new AdaptadorViewPager_MainActivity(getSupportFragmentManager(),tabLayout.getTabCount(),this);
        viewPager.setAdapter(adaptadorViewPager_mainActivity);
        viewPager.setOffscreenPageLimit(1);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Y por último, se vincula el viewpager con el control de tabs para sincronizar ambos
        tabLayout.setupWithViewPager(viewPager);

        TabLayout.Tab tabHome = tabLayout.getTabAt(0);
        tabHome.setIcon(R.drawable.home);

        TabLayout.Tab tabEvents = tabLayout.getTabAt(1);
        tabEvents.setIcon(R.drawable.calendario);

        TabLayout.Tab tabOffer = tabLayout.getTabAt(2);
        tabOffer.setIcon(R.drawable.creador);

        TabLayout.Tab tabSettings = tabLayout.getTabAt(3);
        tabSettings.setIcon(R.drawable.settings);


        //Información del usuario registrado
        usuario = getIntent().getExtras().getParcelable("usuario");

    }


    /**
     * Interfaz FragmentInteraction mediante la cual se gestionan los eventos generados por los
     * fragments que contiene la actividad.
     * @param uri
     */
    @Override
    public void onFragmentInteraction(Uri uri) {

    }


}
