package es.esy.pruebaappluis.cityme.Modelo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

/**
 * Contiene toda la información de un evento.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class Evento implements Parcelable {

    protected String id;
    protected String nombre;
    protected String ciudad;
    protected String descripcion;
    protected String imagen;
    protected Bitmap imagenBitmap; // Para mostrar una imagen en la vista a de estar en bitmap (por eso el Base64 se transforma a Bitmap)
    protected double latitud;
    protected double longitud;
    protected String lugar;
    protected Usuario usuarioCreador;
    protected String fecha_inicio;
    protected String fecha_fin;

    /**
     *
     * @param id identificador del evento
     * @param nombre nombre del evento
     * @param descripcion descripción del evento
     * @param ciudad ciudad del evento
     * @param latitud latitud de la ubicación del evento
     * @param longitud longitud de la ubicación del evento
     * @param lugar establecimiento o lugar del evento
     * @param usuario usuario creador del evento
     * @param fecha_inicio fecha de inicio del evento
     * @param fecha_fin fecha de finalización del evento
     */
    public Evento (String id, String nombre, String descripcion, String ciudad, double latitud, double longitud, String lugar, Usuario usuario, String fecha_inicio, String fecha_fin){
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ciudad = ciudad;
        this.latitud=latitud;
        this.longitud=longitud;
        this.lugar=lugar;
        this.usuarioCreador = usuario;
        this.fecha_inicio=fecha_inicio;
        this.fecha_fin=fecha_fin;
    }

    /**
     * Constructor que permite crear un evento mediante un objeto Parcel
     *
     * @param in objeto Parcel que contiene los datos del evento
     */
    protected Evento(Parcel in) {
        id = in.readString();
        nombre = in.readString();
        ciudad = in.readString();
        descripcion = in.readString();
        imagen = in.readString();
        imagenBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        latitud=in.readDouble();
        longitud=in.readDouble();
        lugar= in.readString();
        usuarioCreador=in.readParcelable(Usuario.class.getClassLoader());
        fecha_inicio=in.readString();
        fecha_fin=in.readString();
    }

    /**
     * Interfaz que permite crear instrancias Parcelable de la clase Evento
     */
    public static final Creator<Evento> CREATOR = new Creator<Evento>() {
        @Override
        public Evento createFromParcel(Parcel in) {
            return new Evento(in);
        }

        @Override
        public Evento[] newArray(int size) {
            return new Evento[size];
        }
    };

    public void setId(String id) {
        this.id = id;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
        byte[] byteimagen = Base64.decode(imagen, Base64.DEFAULT);

        this.imagenBitmap = BitmapFactory.decodeByteArray(byteimagen, 0, byteimagen.length);
    }

    public void setLatitud(double latitud) {this.latitud = latitud;}

    public void setLongitud(double longitud) {this.longitud = longitud;}

    public void setLugar(String lugar) {this.lugar = lugar;}

    public void setUsuarioCreador(Usuario usuarioCreador) {
        this.usuarioCreador = usuarioCreador;
    }

    public void setFecha_inicio(String fecha_inicio) {this.fecha_inicio = fecha_inicio;}

    public void setFecha_fin(String fecha_fin) {this.fecha_fin = fecha_fin;}

    public String getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getImagen() {
        return imagen;
    }

    public double getLatitud() {return latitud;}

    public double getLongitud() {return longitud;}

    public String getLugar() {return lugar;}

    public Bitmap getImagenBitmap() {
        return imagenBitmap;
    }

    public Usuario getUsuarioCreador() {
        return usuarioCreador;
    }

    public String getFecha_inicio() {return fecha_inicio;}

    public String getFecha_fin() {return fecha_fin;}

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Permite acoplar este objeto en un objeto Parcel
     *
     * @param parcel objeto Parcel donde el evento va a ser acoplado
     * @param i flags
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.id);
        parcel.writeString(this.nombre);
        parcel.writeString(this.ciudad);
        parcel.writeString(this.descripcion);
        parcel.writeString(this.imagen);
        parcel.writeDouble(this.latitud);
        parcel.writeDouble(this.longitud);
        parcel.writeString(this.lugar);
        parcel.writeString(this.fecha_inicio);
        parcel.writeString(this.fecha_fin);
    }

}
