package es.esy.pruebaappluis.cityme.Activities_Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import es.esy.pruebaappluis.cityme.R;


/**
 * Gestiona las secciones de eventos y promociones creadas.
 * <p>
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentCreador.OnFragmentInteractionListener} interface
 * to handle interaction events.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class FragmentCreador extends Fragment {

    private OnFragmentInteractionListener mListener;

    protected TabLayout tabLayoutCreador;
    private FragmentMisEventos fragmentMisEventos;
    private FragmentMisPromociones fragmentMisPromociones;

    boolean cargaInicial =false;

    public FragmentCreador() {
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
        final View view = inflater.inflate(R.layout.fragment_creador, container, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if(keyEvent.getAction()==KeyEvent.ACTION_DOWN){
                    if(keyCode==KeyEvent.KEYCODE_BACK){
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

        tabLayoutCreador = (TabLayout) view.findViewById(R.id.TabLayoutCreador);

        // Se añaden las 2 tabs
        // Modo "fixed" para que todas las tabs tengan el mismo tamaño. También se asigna una gravedad centrada.
        tabLayoutCreador.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayoutCreador.setTabMode(TabLayout.MODE_FIXED);

        fragmentMisEventos = new FragmentMisEventos();
        fragmentMisPromociones = new FragmentMisPromociones();

        tabLayoutCreador.addTab(tabLayoutCreador.newTab().setText(R.string.eventos),true);
        tabLayoutCreador.addTab(tabLayoutCreador.newTab().setText(R.string.promociones));

        tabLayoutCreador.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                switch (tab.getPosition()) {
                    case 0:

                        fragmentTransaction.replace(R.id.content_creador, fragmentMisEventos);
                        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        fragmentTransaction.commit();
                        break;

                    case 1:

                        fragmentTransaction.replace(R.id.content_creador, fragmentMisPromociones);
                        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        fragmentTransaction.commit();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Inicialmente se muestra el Fragment eventos creados
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_creador, fragmentMisEventos);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commitAllowingStateLoss();

        return view;
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
}
