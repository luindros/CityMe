package es.esy.pruebaappluis.cityme.RecyclerViewUtilities;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import es.esy.pruebaappluis.cityme.Modelo.Promocion;
import es.esy.pruebaappluis.cityme.R;

/**
 * Adaptador para el RecyclerView que muestra las promociones creadas por un usuario.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class RecyclerPromocionesAdapter_MisPromociones extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    protected ArrayList<Promocion> listaPromociones;

    /**
     *
     * @param listaPromociones lista de promociones creadas por un usuario
     */
    public RecyclerPromocionesAdapter_MisPromociones (ArrayList<Promocion> listaPromociones){

        this.listaPromociones= listaPromociones;
    }

    /**
     * Crea una nueva instancia de PromocionViewHolder asociando a la misma el layout
     * que especifica cómo va a mostrarse la información de los eventos.
     *
     * @param parent ViewGroup al que se añadirá la nueva vista
     * @param viewType tipo de vista
     * @return instancia de PromocionViewHolder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_promocion, parent, false);
        return PromocionViewHolder.newInstance(itemView);
    }

    /**
     * Es llamado por el RecyclerView para visualizar los datos en la posición especificada.
     *
     * @param viewHolder ViewHolder que debe ser actualizado
     * @param position posición del elemento
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        PromocionViewHolder holder = (PromocionViewHolder) viewHolder;
        Promocion promocion = listaPromociones.get(position);
        holder.bindPromocion(promocion);
    }

    /**
     *
     * @return número de promociones creadas por un usuario
     */
    @Override
    public int getItemCount() {
        return listaPromociones == null ? 0 : listaPromociones.size();
    }


    /**
     * ViewHolder que describe una promoción (su vista y sus datos) y que será mostrado en un
     * RecyclerView.
     *
     * @author Luis Iglesias Muñoz
     * @version 1.0
     */
    public static class PromocionViewHolder extends RecyclerView.ViewHolder{

        private final TextView nombre;
        private final TextView descripcion;
        private final CircleImageView imagen;


        /**
         * Instancia los atributos de PromocionViewHolder
         *
         * @param itemView
         * @param nombre nombre de la promoción
         * @param descripcion descripción de la promoción
         * @param imagen imagen de la promoción
         */
        public PromocionViewHolder(View itemView, TextView nombre, TextView descripcion, CircleImageView imagen) {
            super(itemView);

            this.nombre = nombre;
            this.descripcion = descripcion;
            this.imagen = imagen;
        }

        /**
         * Crea una nueva instancia.
         *
         * @param parent
         * @return PromocionViewHolder
         */
        public static PromocionViewHolder newInstance(View parent) {
            TextView nombre = (TextView) parent.findViewById(R.id.nombre);
            TextView descripcion = (TextView) parent.findViewById(R.id.descripcion);
            CircleImageView imagen = (CircleImageView) parent.findViewById(R.id.imagen);
            return new PromocionViewHolder(parent, nombre, descripcion, imagen);
        }

        /**
         * Asocia los datos de la promoción
         *
         * @param p promoción
         */
        public void bindPromocion(Promocion p) {
            this.nombre.setText(p.getNombre());
            this.descripcion.setText(p.getDescripcion());
            this.imagen.setImageBitmap(p.getImagenBitmap());
        }

    }
}