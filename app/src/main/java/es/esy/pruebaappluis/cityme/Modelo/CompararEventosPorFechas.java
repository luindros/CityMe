package es.esy.pruebaappluis.cityme.Modelo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Permite comparar eventos por su fecha de inicio.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class CompararEventosPorFechas implements Comparator<Evento> {

    public SimpleDateFormat formateadorFecha = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    /**
     * Compara dos eventos.
     *
     * @param evento1
     * @param evento2
     * @return
     */
    @Override
    public int compare(Evento evento1, Evento evento2) {

        try {
            Date fechaInicioEvento1 = formateadorFecha.parse(evento1.getFecha_inicio());
            Date fechaInicioEvento2 = formateadorFecha.parse(evento2.getFecha_inicio());

            if(fechaInicioEvento1.before(fechaInicioEvento2)){

                return -1;
            }
            else if(fechaInicioEvento1.after(fechaInicioEvento2)){

                return 1;
            }
            else{
                return 0;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
