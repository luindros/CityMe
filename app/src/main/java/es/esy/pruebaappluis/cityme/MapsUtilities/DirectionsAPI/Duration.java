package es.esy.pruebaappluis.cityme.MapsUtilities.DirectionsAPI;

/**
 * Representa la duración de una ruta.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class Duration {
    public String text;
    public int value;

    public Duration(String text, int value) {
        this.text = text;
        this.value = value;
    }
}
