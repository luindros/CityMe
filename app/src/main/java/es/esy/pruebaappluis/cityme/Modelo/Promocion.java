package es.esy.pruebaappluis.cityme.Modelo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

/**
 * Contiene toda la información de una promoción.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class Promocion implements Parcelable {

    protected String id;
    protected String nombre;
    protected String descripcion;
    protected String imagen;
    protected Bitmap imagenBitmap; // Para mostrar una imagen en la vista a de estar en bitmap (por eso el Base64 se transforma a Bitmap)
    protected Usuario usuarioCreador;
    protected boolean checkBox;

    /**
     *
     * @param id identificador de la promoción
     * @param nombre nombre de la promoción
     * @param descripcion descripción de la promoción
     * @param usuarioCreador usuario creador de la promoción
     */
    public Promocion(String id, String nombre, String descripcion, Usuario usuarioCreador){
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.usuarioCreador = usuarioCreador;
        this.checkBox = false;
    }

    /**
     * Constructor que permite crear una promoción mediante un objeto Parcel
     *
     * @param in objeto Parcel que contiene los datos de la promoción
     */
    protected Promocion(Parcel in) {
        id = in.readString();
        nombre = in.readString();
        descripcion = in.readString();
        imagen = in.readString();
        imagenBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        usuarioCreador = in.readParcelable(Usuario.class.getClassLoader());
    }

    /**
     * Interfaz que permite crear instrancias Parcelable de la clase Promoción
     */
    public static final Creator<Promocion> CREATOR = new Creator<Promocion>() {
        @Override
        public Promocion createFromParcel(Parcel in) {
            return new Promocion(in);
        }

        @Override
        public Promocion[] newArray(int size) {
            return new Promocion[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getImagen() {
        return imagen;
    }

    public Bitmap getImagenBitmap() {
        return imagenBitmap;
    }

    public Usuario getUsuarioCreador() {
        return usuarioCreador;
    }

    public boolean isCheckBox() {return checkBox;}

    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
        byte[] byteimagen = Base64.decode(imagen, Base64.DEFAULT);

        this.imagenBitmap = BitmapFactory.decodeByteArray(byteimagen, 0, byteimagen.length);
    }
    public void setUsuarioCreador(Usuario usuarioCreador) {
        this.usuarioCreador = usuarioCreador;
    }

    public void setCheckBox(boolean checkBox) {this.checkBox = checkBox;}

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Permite acoplar este objeto en un objeto Parcel
     *
     * @param parcel objeto Parcel donde la promoción va a ser acoplada
     * @param i flags
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.id);
        parcel.writeString(this.nombre);
        parcel.writeString(this.descripcion);
        parcel.writeString(this.imagen);
    }
}
