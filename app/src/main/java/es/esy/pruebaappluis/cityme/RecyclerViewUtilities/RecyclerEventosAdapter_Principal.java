package es.esy.pruebaappluis.cityme.RecyclerViewUtilities;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import es.esy.pruebaappluis.cityme.Modelo.Evento;
import es.esy.pruebaappluis.cityme.R;

/**
 * Adaptador para el RecyclerView que muestra los eventos a de la ventana principal.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class RecyclerEventosAdapter_Principal extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 2;
    private static final int TYPE_ITEM = 1;

    protected ArrayList<Evento> listaEventos;

    /**
     *
     * @param listaEventos lista de eventos encontrados por radar
     */
    public RecyclerEventosAdapter_Principal(ArrayList<Evento> listaEventos) {
        this.listaEventos = listaEventos;
    }

    /**
     * Crea una nueva instancia de EventoViewHolder asociando a la misma el layout
     * que especifica cómo va a mostrarse la información de los eventos.
     *
     * @param parent ViewGroup al que se añadirá la nueva vista
     * @param viewType tipo de vista
     * @return instancia de EventoViewHolder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        if (viewType == TYPE_ITEM) {
            final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_evento_principal, parent, false);
            return EventoViewHolder.newInstance(itemView);

        } else if (viewType == TYPE_HEADER) {
            final View view = LayoutInflater.from(context).inflate(R.layout.recycler_header_principal, parent, false);
            return new HeaderViewHolder(view);
        }
        throw new RuntimeException("There is no type that matches the type " + viewType + " + make sure your using types    correctly");

    }

    /**
     * Es llamado por el RecyclerView para visualizar los datos en la posición especificada.
     *
     * @param viewHolder ViewHolder que debe ser actualizado
     * @param position posición del elemento
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (!isPositionHeader(position)) {
            EventoViewHolder holder = (EventoViewHolder) viewHolder;
            Evento evento = listaEventos.get(position - 1); // quitando la cabecera
            holder.bindEvento(evento);
        }
    }

    /**
     *
     * @return número de eventos encontrados por radar
     */
    public int getBasicItemCount() {
        return listaEventos == null ? 0 : listaEventos.size();
    }

    /**
     *
     * @return número de eventos encontrados por radar, incluyendo una cabecera
     */
    @Override
    public int getItemCount() {
        return getBasicItemCount() + 1; // header
    }

    /**
     * Obtiene el tipo de vista de la posición.
     *
     * @param position
     * @return el tipo de vista
     */
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    /**
     * Comprueba si la posición se corresponde con la cabecera.
     *
     * @param position
     * @return si la posición es la cabecera o no
     */
    public boolean isPositionHeader(int position) {
        return position == 0;
    }

    /**
     * ViewHolder que describe un evento (su vista y sus datos) y que será mostrado en un
     * RecyclerView.
     *
     * @author Luis Iglesias Muñoz
     * @version 1.0
     */
    public static class EventoViewHolder extends RecyclerView.ViewHolder{

        private String[] listaMeses = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};

        private final TextView nombre;
        private final TextView lugar;
        private final TextView diaInicio;
        private final TextView mesInicio;
        private final TextView horaInicio;
        private final TextView fechaFin;
        private final ImageView imagen;
        private Context context;

        /**
         * Instancia los atributos de EventoViewHolder
         *
         * @param itemView
         * @param nombre nombre del evento
         * @param lugar establecimiento o lugar del evento
         * @param diaInicio día de inicio del evento
         * @param mesInicio mes de inicio del evento
         * @param horaInicio hora de inicio del evento
         * @param fechaFin fecha de finalización del evento
         * @param imagen cartel del evento
         */
        public EventoViewHolder(View itemView, TextView nombre, TextView lugar, TextView diaInicio, TextView mesInicio, TextView horaInicio, TextView fechaFin, ImageView imagen) {
            super(itemView);

            this.nombre = nombre;
            this.lugar = lugar;
            this.diaInicio = diaInicio;
            this.mesInicio = mesInicio;
            this.horaInicio = horaInicio;
            this.fechaFin = fechaFin;
            this.imagen = imagen;
            this.context = itemView.getContext();

        }

        /**
         * Crea una nueva instancia.
         *
         * @param parent
         * @return EventoViewHolder
         */
        public static EventoViewHolder newInstance(View parent) {
            TextView nombre = (TextView) parent.findViewById(R.id.nombre);
            TextView lugar = (TextView) parent.findViewById(R.id.lugar);
            TextView diaInicio = (TextView) parent.findViewById(R.id.diaInicio);
            TextView mesInicio = (TextView) parent.findViewById(R.id.mesInicio);
            TextView horaInicio = (TextView) parent.findViewById(R.id.horaInicio);
            TextView fechaFin = (TextView) parent.findViewById(R.id.fechaFin);
            ImageView imagen = (ImageView) parent.findViewById(R.id.imagen);
            return new EventoViewHolder(parent, nombre, lugar, diaInicio, mesInicio, horaInicio, fechaFin, imagen);
        }

        /**
         * Asocia los datos del evento
         *
         * @param e evento
         */
        public void bindEvento(Evento e) {
            this.nombre.setText(e.getNombre());
            this.lugar.setText(e.getLugar());

            String[] separadorFecha_Hora_Inicio = e.getFecha_inicio().split(" ");
            String[] separadorFecha_Inicio = separadorFecha_Hora_Inicio[0].split("-");
            int mesInicio = Integer.parseInt(separadorFecha_Inicio[1])-1;
            this.diaInicio.setText(separadorFecha_Inicio[0]);
            this.mesInicio.setText(listaMeses[mesInicio]);
            this.horaInicio.setText(separadorFecha_Hora_Inicio[1]);

            String[] separadorFecha_Hora_Fin = e.getFecha_fin().split(" ");
            String[] separadorFecha_Fin = separadorFecha_Hora_Fin[0].split("-");
            int mesFin = Integer.parseInt(separadorFecha_Fin[1])-1;
            this.fechaFin.setText(context.getText(R.string.hasta) + " " + separadorFecha_Fin[0] + " " + listaMeses[mesFin] + " " + separadorFecha_Hora_Fin[1]);

            this.imagen.setImageBitmap(e.getImagenBitmap());
        }

    }

    /**
     * ViewHolder que describe la cabecera que será mostrada en un
     * RecyclerView.
     *
     * @author Luis Iglesias Muñoz
     * @version 1.0
     */
    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }


}
