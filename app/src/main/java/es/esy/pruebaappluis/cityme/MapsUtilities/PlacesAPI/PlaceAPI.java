package es.esy.pruebaappluis.cityme.MapsUtilities.PlacesAPI;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.esy.pruebaappluis.cityme.R;
import es.esy.pruebaappluis.cityme.Volley.PeticionesServidorVolley;

/**
 * Permite realizar el autocompletado de sitios.
 * <p>
 * Permite dada una latitud y longitud obtener la dirección de la ubicacion.
 * <p>
 * Permite dada la dirección en lenguaje natural de una ubicación obtener la latitud
 * y longitud correspondiente.
 * <p>
 * Permite dada una latitud y longitud obtener la ciudad correspondiente a la ubicacion.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class PlaceAPI {
    private static final String TAG = PlaceAPI.class.getSimpleName();

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyAN_-OvQwPY9xJRYx3HzofNaUFHkcYjcyI";

    ArrayList<String> resultList = null;

    /**
     * Realiza el autocompletado de sitios
     *
     * @param input cadena introducida por el usuario de la cual se obtienen las predicciones
     * @param context
     */
    public void autocomplete (String input, final Context context) {

        String uri = null;
        try {
            uri = PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON + "?key=" + API_KEY + "&types=geocode" + "&input=" + URLEncoder.encode(input, "utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!PeticionesServidorVolley.compruebaConexion(context)) {
            Toast.makeText(context, R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        // PETICIÓN GET
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                uri, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {
                    // Parsing json object response

                    JSONArray predsJsonArray = response.getJSONArray("predictions");

                    // Extract the Place descriptions from the results
                    resultList = new ArrayList<String>(predsJsonArray.length());
                    for (int i = 0; i < predsJsonArray.length(); i++) {
                        resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(context, R.string.error_conexion, Toast.LENGTH_SHORT).show();

            }
        });

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);
    }

    /**
     * Dada una latitud y una longitud devuelve la dirección completa
     *
     * @param LATITUDE
     * @param LONGITUDE
     * @param context
     * @return dirección completa en lenguaje natural
     */
    public static String getCompleteAddressString(double LATITUDE, double LONGITUDE, Context context) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                int i=0;
                for (i = 0; i < returnedAddress.getMaxAddressLineIndex()-1; i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(", ");
                }
                strReturnedAddress.append(returnedAddress.getAddressLine(i));

                strAdd = strReturnedAddress.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }

    /**
     * Dada una latitud y una longitud devulve la ciudad correspondiente
     *
     * @param LATITUDE
     * @param LONGITUDE
     * @param context
     * @return ciudad
     */
    public static String getCityString(double LATITUDE, double LONGITUDE, Context context){
        String cityName = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                cityName = addresses.get(0).getLocality();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cityName;
    }

    /**
     * Dada la dirección te devuelve la latitud y la longitud correspondiente
     *
     * @param completeAddress
     * @param context
     * @return latitud y longitud
     */
    public static LatLng getCoordenadasFromAddress(String completeAddress, Context context) {
        LatLng coordenadas = null;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(completeAddress, 1);
            if (addresses != null) {
                coordenadas = new LatLng(addresses.get(0).getLatitude(),addresses.get(0).getLongitude());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return coordenadas;
    }
}
