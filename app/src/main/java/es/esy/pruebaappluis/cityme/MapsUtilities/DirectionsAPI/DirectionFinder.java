package es.esy.pruebaappluis.cityme.MapsUtilities.DirectionsAPI;

import android.content.Context;
import android.graphics.Color;
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

import es.esy.pruebaappluis.cityme.R;
import es.esy.pruebaappluis.cityme.Volley.PeticionesServidorVolley;

/**
 * Realizará la búsqueda de una determinada ruta entre dos ubicaciones.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class DirectionFinder {

    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyAN_-OvQwPY9xJRYx3HzofNaUFHkcYjcyI";
    private DirectionFinderListener listener;
    private String origin;
    private String destination;
    private static String TAG = DirectionFinder.class.getSimpleName();
    private Context contextoApp;

    /**
     *
     * @param listener interfaz DirectionFinderListener
     * @param origin dirección de origen
     * @param destination dirección de destino
     * @param contextoApp
     */
    public DirectionFinder(DirectionFinderListener listener, String origin, String destination, Context contextoApp) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
        this.contextoApp = contextoApp;
    }

    /**
     * Inicia la solicitud de búsqueda.
     *
     * @param modo indica si la ruta se solicitará en coche o andando
     * @throws UnsupportedEncodingException
     */
    public void execute(int modo) throws UnsupportedEncodingException {
        listener.onDirectionFinderStart();
        if(modo==0){
            solicitarRuta(modo,createUrlDriving(), Color.rgb(0,177,234));
        }else{
            solicitarRuta(modo,createUrlWalking(),Color.rgb(76,175,80));
        }

    }

    /**
     * Crea la url que permitirá buscar la ruta en coche
     *
     * @return url de la petición a la Directions API
     * @throws UnsupportedEncodingException
     */
    private String createUrlDriving() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");

        return DIRECTION_URL_API + "origin=" + urlOrigin + "&destination=" + urlDestination + "&key=" + GOOGLE_API_KEY + "&sensor=true";
    }

    /**
     * Crea la url que permitirá buscar la ruta en andando
     *
     * @return url de la petición a la Directions API
     * @throws UnsupportedEncodingException
     */
    private String createUrlWalking() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");

        return DIRECTION_URL_API + "origin=" + urlOrigin + "&destination=" + urlDestination + "&mode=walking&key=" + GOOGLE_API_KEY + "&sensor=true";
    }

    /**
     * Realiza un petición GET mediante la cual se obtendrá la ruta
     *
     * @param modo indica si es en coche o andando
     * @param uriMaps uri de petición a la Directions API
     * @param color color de la polilínea que representará la ruta
     */
    private void solicitarRuta(final int modo, String uriMaps, final int color) {

        if (!PeticionesServidorVolley.compruebaConexion(contextoApp)) {
            Toast.makeText(contextoApp, R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        // PETICIÓN GET
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                uriMaps, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {

                    List<Route> routes = new ArrayList<Route>();
                    JSONArray jsonRoutes = response.getJSONArray("routes");
                    for (int i = 0; i < jsonRoutes.length(); i++) {
                        JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                        Route route = new Route();

                        JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
                        JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
                        JSONObject jsonLeg = jsonLegs.getJSONObject(0);
                        JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
                        JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
                        JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
                        JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

                        route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
                        route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
                        route.endAddress = jsonLeg.getString("end_address");
                        route.startAddress = jsonLeg.getString("start_address");
                        route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
                        route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
                        route.points = decodePolyLine(overview_polylineJson.getString("points"));

                        routes.add(route);
                    }

                    listener.onDirectionFinderSuccess(modo,routes,color);


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(contextoApp,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(contextoApp,R.string.error_conexion, Toast.LENGTH_SHORT).show();


            }
        });

        // Se añade la petición a la Cola de Peticiones de Volley
        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

    }

    /**
     * Decodifica una polilínea en las correspondientes coordenadas que contiene la misma.
     *
     * @param poly
     * @return lista de coordenadas
     */
    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }

}

