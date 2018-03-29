package es.esy.pruebaappluis.cityme.RecyclerViewUtilities;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import es.esy.pruebaappluis.cityme.Modelo.Promocion;
import es.esy.pruebaappluis.cityme.R;

/**
 * Adaptador para el RecyclerView que muestra las promociones creadas por un usuario y
 * gestiona la selección de las mismas.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class RecyclerPromocionesAdapter_SeleccionPromocion extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected ArrayList<Promocion> listaPromociones;

    /**
     *
     * @param listaPromociones lista de promociones creadas por un usuario
     */
    public RecyclerPromocionesAdapter_SeleccionPromocion(ArrayList<Promocion> listaPromociones) {

        this.listaPromociones = listaPromociones;
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

        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_promocion_seleccionar_promocion, parent, false);
        return new PromocionViewHolder(itemView);
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
        holder.bindPromocion(promocion, position);
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
     * Obtiene la promoción de la posición especificada
     *
     * @param position
     * @return Promocion
     */
    Promocion getPromocion(int position) {
        return listaPromociones.get(position);
    }

    /**
     * Obtiene todas las promociones que están seleccionadas
     *
     * @return lista de promociones seleccionadas
     */
    public ArrayList<Promocion> getAllCheckBox(){
        ArrayList<Promocion> box = new ArrayList<>();
        for (Promocion p : listaPromociones) {
            if (p.isCheckBox())
                box.add(p);
        }
        return box;
    }

    /**
     * Interfaz que gestiona la selección o deselección de una promoción
     */
    public CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            getPromocion((Integer) buttonView.getTag()).setCheckBox(isChecked);
        }
    };

    /**
     * ViewHolder que describe una promoción (su vista y sus datos) y que será mostrado en un
     * RecyclerView.
     *
     * @author Luis Iglesias Muñoz
     * @version 1.0
     */
    public class PromocionViewHolder extends RecyclerView.ViewHolder {

        private final TextView nombre;
        private final TextView descripcion;
        private final CircleImageView imagen;
        private final CheckBox checkBox;

        /**
         * Instancia los atributos de PromocionViewHolder
         *
         * @param itemView
         */
        public PromocionViewHolder(View itemView) {
            super(itemView);

            this.nombre = (TextView) itemView.findViewById(R.id.nombre);
            this.descripcion = (TextView) itemView.findViewById(R.id.descripcion);
            this.imagen = (CircleImageView) itemView.findViewById(R.id.imagen);
            this.checkBox = (CheckBox) itemView.findViewById(R.id.checkboxPromocion);
        }

        /**
         * Asocia los datos de la promoción
         *
         * @param p promoción
         * @param position posición de la promoción
         */
        public void bindPromocion(Promocion p, int position) {
            this.nombre.setText(p.getNombre());
            this.descripcion.setText(p.getDescripcion());
            this.imagen.setImageBitmap(p.getImagenBitmap());

            checkBox.setOnCheckedChangeListener(checkedChangeListener);
            checkBox.setTag(position);
            checkBox.setChecked(p.isCheckBox());
        }

    }

}