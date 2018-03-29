package es.esy.pruebaappluis.cityme.MapsUtilities.DirectionsAPI;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Representa una ruta entre ubicaciones.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class Route {
    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;
}
