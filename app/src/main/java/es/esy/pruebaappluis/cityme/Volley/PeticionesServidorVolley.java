package es.esy.pruebaappluis.cityme.Volley;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Gestiona la cola de peticiones al servicio REST.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class PeticionesServidorVolley extends Application {

    /* Atributos: */
    public static final String TAG = PeticionesServidorVolley.class.getSimpleName();

    private RequestQueue mRequestQueue;

    private static PeticionesServidorVolley objPeticionesServidorVolley;


    /* Métodos */

    @Override
    public void onCreate() {
        super.onCreate();
        objPeticionesServidorVolley = this;
    }

    /**
     * Obtiene una instacia de la clase
     *
     * @return objeto PeticionesServidorVolley
     */
    public static synchronized PeticionesServidorVolley getInstance() {
        return objPeticionesServidorVolley;
    }

    /**
     * Obtiene la cola de peticiones si existe y si no obtiene una nueva instancia.
     *
     * @return cola de peticiones
     */
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    /**
     * Añade una petición a la cola
     *
     * @param req
     * @param tag
     * @param <T>
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    /**
     * Añade una petición a la cola
     *
     * @param req
     * @param <T>
     */
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    /**
     * Cancela todas las peticiones pendientes
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    /**
     * Comprueba si hay conexión a Internet
     *
     * @param context
     * @return indica si hay o no conexión
     */

    public static boolean compruebaConexion(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null)
            return false;
        boolean isConnected = activeNetwork.isConnectedOrConnecting();

        return isConnected;

    }

}
