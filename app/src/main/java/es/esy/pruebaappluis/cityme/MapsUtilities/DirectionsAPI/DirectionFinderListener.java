package es.esy.pruebaappluis.cityme.MapsUtilities.DirectionsAPI;

import java.util.List;

/**
 * Interfaz que gestiona el inicio de la búsqueda de una ruta y la ruta tras ser encontrada.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public interface DirectionFinderListener {
    /**
     * Gestiona el inicio de la búsqueda de una ruta.
     */
    void onDirectionFinderStart();

    /**
     * Gestiona la ruta encontrada
     *
     * @param modo indica si la ruta es en coche o andando
     * @param route lista de enlaces
     * @param color color de la polilínea que muestra la ruta
     */
    void onDirectionFinderSuccess(int modo, List<Route> route, int color);
}
