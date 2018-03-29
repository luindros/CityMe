package es.esy.pruebaappluis.cityme.Modelo;

import android.os.Parcel;
import android.os.Parcelable;

import com.facebook.AccessToken;

import es.esy.pruebaappluis.cityme.R;

/**
 * Contiene toda la información de un usuario.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class Usuario implements Parcelable{

    private String id = null;
    private String nombre = null;
    private String email = null;
    private String link = null;
    private AccessToken token = AccessToken.getCurrentAccessToken();
    private String imagenUrl = null;

    /**
     *
     * @param id_usuario identificador del usuario (identificador de su perfil de Facebook)
     * @param nombre_usuario nombre del usuario (nombre del usuario en Facebook)
     * @param email_usuario email del usuario
     * @param link_usuario link de Facebook del usuario
     * @param token_usuario token de acceso del usuario
     * @param imagenUrl_usuario url de la imagen de perfil de Facebook del usuario
     */
    public Usuario (String id_usuario, String nombre_usuario, String email_usuario, String link_usuario, String token_usuario, String imagenUrl_usuario){
        this.id=id_usuario;
        this.nombre=nombre_usuario;
        this.email=email_usuario;
        this.link=link_usuario;
        this.token= new AccessToken(token_usuario, Integer.toString(R.string.facebook_app_id), id_usuario, null, null, null, null, null);
        this.imagenUrl=imagenUrl_usuario;

    }

    /**
     * Constructor que permite crear un usuario mediante un objeto Parcel
     *
     * @param in objeto Parcel que contiene los datos del usuario
     */
    protected Usuario(Parcel in) {
        id = in.readString();
        nombre = in.readString();
        email = in.readString();
        link = in.readString();
        token = in.readParcelable(AccessToken.class.getClassLoader());
        imagenUrl = in.readString();
    }

    /**
     * Interfaz que permite crear instrancias Parcelable de la clase Usuario
     */
    public static final Creator<Usuario> CREATOR = new Creator<Usuario>() {
        @Override
        public Usuario createFromParcel(Parcel in) {
            return new Usuario(in);
        }

        @Override
        public Usuario[] newArray(int size) {
            return new Usuario[size];
        }
    };

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AccessToken getToken() {
        return token;
    }

    public void setToken(AccessToken token) {
        this.token = token;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Permite acoplar este objeto en un objeto Parcel
     *
     * @param parcel objeto Parcel donde el usuario va a ser acoplado
     * @param i flags
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.id);
        parcel.writeString(this.nombre);
        parcel.writeString(this.email);
        parcel.writeString(this.link);
        parcel.writeParcelable(token,0);
        parcel.writeString(this.imagenUrl);

    }
}
