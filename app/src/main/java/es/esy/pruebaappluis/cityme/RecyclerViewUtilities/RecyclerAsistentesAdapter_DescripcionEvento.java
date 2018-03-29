package es.esy.pruebaappluis.cityme.RecyclerViewUtilities;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import es.esy.pruebaappluis.cityme.Modelo.Usuario;
import es.esy.pruebaappluis.cityme.R;

/**
 * Adaptador para el RecyclerView que muestra los asistentes a un evento.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class RecyclerAsistentesAdapter_DescripcionEvento extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    protected ArrayList<Usuario> listaAsistentes;

    /**
     *
     * @param listaAsistentes
     */
    public RecyclerAsistentesAdapter_DescripcionEvento (ArrayList<Usuario> listaAsistentes){

        this.listaAsistentes= listaAsistentes;
    }

    /**
     * Crea una nueva instancia de AsistenteViewHolder asociando a la misma el layout
     * que especifica cómo va a mostrarse la información de un asistente.
     *
     * @param parent ViewGroup al que se añadirá la nueva vista
     * @param viewType tipo de vista
     * @return instancia de AsistenteViewHolder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_asistente, parent, false);
        return AsistenteViewHolder.newInstance(itemView);
    }

    /**
     * Es llamado por el RecyclerView para visualizar los datos en la posición especificada.
     *
     * @param viewHolder ViewHolder que debe ser actualizado
     * @param position posición del elemento
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        AsistenteViewHolder holder = (AsistenteViewHolder) viewHolder;
        Usuario asistente = listaAsistentes.get(position);
        holder.bindAsistente(asistente);
    }

    /**
     *
     * @return número de asistentes
     */
    @Override
    public int getItemCount() {
        return listaAsistentes == null ? 0 : listaAsistentes.size();
    }


    /**
     * ViewHolder que describe un asistente (su vista y sus datos) y que será mostrado en un
     * RecyclerView.
     *
     * @author Luis Iglesias Muñoz
     * @version 1.0
     */
    public static class AsistenteViewHolder extends RecyclerView.ViewHolder{

        private final TextView nombre;
        private final ImageView imagen;

        /**
         * Instancia los atributos de AsistenteViewHolder.
         *
         * @param itemView
         * @param nombre nombre del asistente
         * @param imagen imagen del perfil de Facebook del asistente
         */
        public AsistenteViewHolder(View itemView, TextView nombre, ImageView imagen) {
            super(itemView);

            this.nombre = nombre;
            this.imagen = imagen;
        }

        /**
         * Crea una nueva instancia.
         *
         * @param parent
         * @return AsistenteViewHolder
         */
        public static AsistenteViewHolder newInstance(View parent) {
            TextView nombre = (TextView) parent.findViewById(R.id.nombre);
            ImageView imagen = (ImageView) parent.findViewById(R.id.imagen);
            return new AsistenteViewHolder(parent, nombre, imagen);
        }

        /**
         * Asocia los datos del asistente.
         *
         * @param a asistente
         */
        public void bindAsistente(Usuario a) {
            this.nombre.setText(a.getNombre());
            Picasso.with(itemView.getContext()).load(a.getImagenUrl()).into(this.imagen);
        }

    }
}