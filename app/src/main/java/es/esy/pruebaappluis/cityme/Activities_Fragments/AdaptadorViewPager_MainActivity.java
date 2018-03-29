package es.esy.pruebaappluis.cityme.Activities_Fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import es.esy.pruebaappluis.cityme.R;

/**
 * Gestiona los diferentes fragmentos, que se encuentran en el MainActivity,
 * a través de un ViewPager. Es decir, será la encargada de crear el contenido de cada
 * página.
 *
 * @see MainActivity
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */

public class AdaptadorViewPager_MainActivity extends FragmentPagerAdapter {

    int numeroSecciones;
    Activity activity;

    /**
     * @param fm interfaz que permite interactuar con los Fragments de una Actividad
     * @param numeroSecciones número de secciones que tiene el TabLayout el MainActivity
     * @param activity actividad que contiene los Fragments
     */
    public AdaptadorViewPager_MainActivity(FragmentManager fm, int numeroSecciones, Activity activity) {
        super(fm);
        this.numeroSecciones=numeroSecciones;
        this.activity = activity;
    }

    /**
     * Devuelve el Fragment de la posición indicada
     *
     * @param position posición de un Fragment
     * @return Fragment correspondiente a la posición indicada
     */
    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return new FragmentPrincipal();

            case 1:
                return new FragmentCalendario();

            case 2:
                return new FragmentCreador();

            case 3:
                return new FragmentConfiguracion();

            // si la posición recibida no se corresponde a ninguna sección
            default:
                return null;
        }
    }

    /**
     * Devuelve el número de secciones que tiene el TabLayout del MainActivity
     *
     * @return número de secciones
     */
    @Override
    public int getCount() {
        return numeroSecciones;
    }

    /**
     * Devuelve el título de cada sección
     *
     * @param position posición de un Fragment
     * @return título de la sección correspondiente a la posición indicada
     */
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {

            case 0:
                return activity.getText(R.string.inicio);
            case 1:
                return activity.getText(R.string.calendario);
            case 2:
                return activity.getText(R.string.creador);
            case 3:
                return activity.getText(R.string.configuracion);

            // si la posición recibida no se corresponde a ninguna sección
            default:
                return null;
        }
    }
}