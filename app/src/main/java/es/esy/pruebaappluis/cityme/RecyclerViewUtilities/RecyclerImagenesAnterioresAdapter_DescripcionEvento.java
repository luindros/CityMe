package es.esy.pruebaappluis.cityme.RecyclerViewUtilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import es.esy.pruebaappluis.cityme.R;

/**
 * Adaptador para el RecyclerView que muestra las imágenes de ediciones anteriores
 * de un evento.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class RecyclerImagenesAnterioresAdapter_DescripcionEvento extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected ArrayList<String> listaImagenes;

    /**
     *
     * @param listaImagenes lista de imágenes de ediciones anteriores
     */
    public RecyclerImagenesAnterioresAdapter_DescripcionEvento(ArrayList<String> listaImagenes) {

        this.listaImagenes = listaImagenes;
    }

    /**
     * Crea una nueva instancia de ImagenViewHolder asociando a la misma el layout
     * que especifica cómo van a mostrarse las imágenes.
     *
     * @param parent ViewGroup al que se añadirá la nueva vista
     * @param viewType tipo de vista
     * @return instancia de ImagenViewHolder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_imagen_anterior, parent, false);
        return ImagenViewHolder.newInstance(itemView);
    }

    /**
     * Es llamado por el RecyclerView para visualizar los datos en la posición especificada.
     *
     * @param viewHolder ViewHolder que debe ser actualizado
     * @param position posición del elemento
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        ImagenViewHolder holder = (ImagenViewHolder) viewHolder;
        String imagenBase64 = listaImagenes.get(position);
        holder.bindImagen(imagenBase64);
    }

    /**
     *
     * @return número de imágenes
     */
    @Override
    public int getItemCount() {
        return listaImagenes == null ? 0 : listaImagenes.size();
    }


    /**
     * ViewHolder que describe la imagen que será mostrada en un
     * RecyclerView.
     *
     * @author Luis Iglesias Muñoz
     * @version 1.0
     */
    public static class ImagenViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imagen;

        /**
         * Instancia los atributos de ImagenViewHolder
         *
         * @param itemView
         * @param imagen
         */
        public ImagenViewHolder(View itemView, ImageView imagen) {
            super(itemView);

            this.imagen = imagen;
        }

        /**
         * Crea una nueva instancia.
         *
         * @param parent
         * @return ImagenViewHolder
         */
        public static ImagenViewHolder newInstance(View parent) {
            ImageView imagen = (ImageView) parent.findViewById(R.id.imagen);
            return new ImagenViewHolder(parent, imagen);
        }

        /**
         * Asocia la imagen
         *
         * @param imagenBase64 imagen en Base64
         */
        public void bindImagen(String imagenBase64) {
            byte[] byteimagen = Base64.decode(imagenBase64, Base64.DEFAULT);
            Bitmap imagenBitmap = BitmapFactory.decodeByteArray(byteimagen, 0, byteimagen.length);
            this.imagen.setImageBitmap(imagenBitmap);
        }

    }
}