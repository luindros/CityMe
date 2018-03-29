package es.esy.pruebaappluis.cityme.Utilities;

import android.support.v7.widget.RecyclerView;

/**
 * Gestiona la ocultación o visualización de los elementos cuando se realiza scroll en un RecyclerView tanto
 * en la ventana de eventos creados como en la de promociones creadas. También es utilizado
 * en la ventana de selección de promociones.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public abstract class HidingScrollListener_EventosCreados_MisPromociones extends RecyclerView.OnScrollListener {

    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    /**
     * Es invocado cuando el RecyclerView es desplazado y gestiona la ocultación o visualización
     * de los elementos.
     *
     * @param recyclerView
     * @param dx desplazamiento horizontal
     * @param dy desplazamiento vertical
     */
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
            onHide();
            controlsVisible = false;
            scrolledDistance = 0;
        } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
            onShow();
            controlsVisible = true;
            scrolledDistance = 0;
        }

        if((controlsVisible && dy>0) || (!controlsVisible && dy<0)) {
            scrolledDistance += dy;
        }
    }

    /**
     * Método abstracto que gestionará la ocultación de los elementos.
     */
    public abstract void onHide();

    /**
     * Método abstracto que gestionará la visualización de los elementos.
     */
    public abstract void onShow();
}
