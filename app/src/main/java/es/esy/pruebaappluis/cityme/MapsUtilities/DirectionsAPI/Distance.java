package es.esy.pruebaappluis.cityme.MapsUtilities.DirectionsAPI;

/**
 * Representa la distancia de una ruta.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class Distance {
    public String text;
    public int value;

    public Distance(String text, int value) {
        this.text = text;
        this.value = value;
    }
}