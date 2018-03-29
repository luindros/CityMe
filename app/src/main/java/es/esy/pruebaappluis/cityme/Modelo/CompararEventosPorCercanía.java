package es.esy.pruebaappluis.cityme.Modelo;

import java.util.Comparator;

/**
 * Permite comparar eventos por su distancia a la
 * ubicación indicada por el usuario.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class CompararEventosPorCercanía implements Comparator<Evento> {

    //En radianes
    double latitudReferencia;
    double longitudReferencia;

    /**
     *
     * @param latitudReferencia latitud de la ubicación indicada por el usuario
     * @param longitudReferencia longitud de la ubicación indicada por el usuario
     */
    public CompararEventosPorCercanía (double latitudReferencia, double longitudReferencia){
        this.latitudReferencia = latitudReferencia;
        this.longitudReferencia = longitudReferencia;
    }

    /**
     * Compara dos eventos.
     *
     * @param evento1
     * @param evento2
     * @return
     */
    @Override
    public int compare(Evento evento1, Evento evento2) {

        double lat1 = evento1.getLatitud();
        double lon1 = evento1.getLongitud();
        double lat2 = evento2.getLatitud();
        double lon2 = evento2.getLongitud();

        double distanceToPlace1 = distance(latitudReferencia, longitudReferencia, lat1, lon1);
        double distanceToPlace2 = distance(latitudReferencia, longitudReferencia, lat2, lon2);

        return (int) (distanceToPlace1 - distanceToPlace2);
    }

    /**
     * Calcula la distancia ortodrómica entre dos ubicaciones
     * dadas sus latitudes y longitudes.
     *
     * @param fromLat latitud ubicación 1
     * @param fromLon longitud ubicación 1
     * @param toLat latitud ubicación 2
     * @param toLon longitud ubicación 2
     * @return
     */
    public double distance(double fromLat, double fromLon, double toLat, double toLon) {

        double radius = 6378137;   // Radio aproximado de la tierra en metros
        double deltaLat = toLat - fromLat;
        double deltaLon = toLon - fromLon;
        double angle = 2 * Math.asin( Math.sqrt(
                Math.pow(Math.sin(deltaLat/2), 2) +
                        Math.cos(fromLat) * Math.cos(toLat) *
                                Math.pow(Math.sin(deltaLon/2), 2) ) );

        return radius * angle;
    }
}
