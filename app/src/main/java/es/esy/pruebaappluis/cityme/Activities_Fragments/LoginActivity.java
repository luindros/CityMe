package es.esy.pruebaappluis.cityme.Activities_Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

import es.esy.pruebaappluis.cityme.Modelo.Usuario;
import es.esy.pruebaappluis.cityme.R;
import es.esy.pruebaappluis.cityme.Volley.PeticionesServidorVolley;

/**
 * Gestiona el acceso a la aplicación.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class LoginActivity extends AppCompatActivity {

    private Button mLoginButton;
    private CallbackManager mCallbackManager;
    private String uri = "http://pruebaappluis.esy.es/usuarios";
    private static String TAG = LoginActivity.class.getSimpleName();

    boolean loggedIn = false;
    private TextView indicadorCargando;

    private static final int CODIGO_MAIN_ACTIVITY = 1;

    SharedPreferences preferences;

    Usuario usuario = null;

    /**
     * Es llamado cuando la actividad se está iniciando.
     * Contiene la inicialización de todos los elementos de la Actividad.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_login);

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.activity_login, null);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        Intent intentHome = new Intent(Intent.ACTION_MAIN);
                        intentHome.addCategory(Intent.CATEGORY_HOME);
                        intentHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentHome);
                        //finish();
                        return true;
                    }
                }
                return false;
            }
        });

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                mLoginButton.setVisibility(View.INVISIBLE);
                indicadorCargando.setVisibility(View.VISIBLE);

                // Se almacena el las preferencias compartidas que el usuario está logueado
                loggedIn = AccessToken.getCurrentAccessToken() != null;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("loggedIn", loggedIn);
                editor.commit();

                login_app();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {

                if (error instanceof FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut();
                        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "user_friends", "email"));

                    }
                }

            }
        });

        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!PeticionesServidorVolley.compruebaConexion(LoginActivity.this)) {
                    Toast.makeText(LoginActivity.this, R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
                    return;
                }

                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "user_friends", "email"));
            }
        });

        indicadorCargando = (TextView) findViewById(R.id.indicadorCargando);
        indicadorCargando.setVisibility(View.INVISIBLE);

        loggedIn = preferences.getBoolean("loggedIn",false);

        if (loggedIn == true) {
            boolean token = AccessToken.getCurrentAccessToken() != null;
            if(token == true) {
                mLoginButton.setVisibility(View.INVISIBLE);
                indicadorCargando.setVisibility(View.VISIBLE);
                login_app();
            }
            else{
                LoginManager.getInstance().logOut();
                mLoginButton.setVisibility(View.VISIBLE);
                indicadorCargando.setVisibility(View.INVISIBLE);
                loggedIn = false;

                // Se almacena en las preferencias compartidas que el usuairo no está logueado
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("loggedIn", loggedIn);
                editor.commit();
            }

        }

    }

    /**
     * Procesa los resultados obtenidos tras acceder a otras Actividades,
     * como son el MainActivity y Facebook.
     *
     * @param requestCode permite identificar de qué Actividad procede el resultado
     * @param resultCode código de resultado devuelto por la otra Actividad
     * @param data puede contener datos devueltos por la Actividad
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODIGO_MAIN_ACTIVITY) {
            loggedIn = false;
            usuario = null;
            mLoginButton.setVisibility(View.VISIBLE);
            indicadorCargando.setVisibility(View.INVISIBLE);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("loggedIn", loggedIn); // value to store
            editor.commit();

        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    /**
     * Realiza la petición a la API GRAPH de Facebook mediante la cual obtiene los datos del usuario.
     * Realiza una petición POST al servicio REST mediante la cual se realiza el login o registro
     * del usuario en la aplicación.
     */
    public void login_app() {

        if (!PeticionesServidorVolley.compruebaConexion(LoginActivity.this)) {
            Toast.makeText(LoginActivity.this, R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        // PETICIÓN API GRAPH
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.v("Main", response.toString());

                        if(object == null) {
                            Toast.makeText(getApplicationContext(),
                                    R.string.error_conexion, Toast.LENGTH_SHORT).show();

                            LoginManager.getInstance().logOut();
                            mLoginButton.setVisibility(View.VISIBLE);
                            indicadorCargando.setVisibility(View.INVISIBLE);
                            return;
                        }

                        String nombre = null;
                        String email = null;
                        String id = null;
                        String link = null;
                        AccessToken token = AccessToken.getCurrentAccessToken();
                        String imagenUrl = null;


                        try {
                            id = object.getString("id");
                            Log.v("token", token.getToken());
                            nombre = object.getString("name");
                            email = object.getString("email");
                            link = object.getString("link");
                            imagenUrl = "https://graph.facebook.com/" + id + "/picture?type=large";

                            usuario = new Usuario(id, nombre, email, link, token.getToken(), imagenUrl);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Se establecen los parametros que van asociados a la petición
                        HashMap<String, String> parametros = new HashMap<>();
                        parametros.put("id_usuario", id.toString());
                        parametros.put("nombre_usuario", nombre.toString());
                        parametros.put("email_usuario", email.toString());
                        parametros.put("link_usuario", link.toString());
                        parametros.put("token_usuario", token.getToken().toString());
                        parametros.put("imagenUrl_usuario", imagenUrl.toString());

                        // Se obtiene el objeto JSON que irá asociado a la petición
                        JSONObject parametrosJson = new JSONObject(parametros);

                        // PETICIÓN POST
                        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                                uri, parametrosJson, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());

                                try {
                                    if (response.getInt("estado") == 1) { //EXITO

                                        Intent facebookIntent = new Intent(LoginActivity.this, MainActivity.class);
                                        facebookIntent.putExtra("usuario", usuario);

                                        startActivityForResult(facebookIntent, CODIGO_MAIN_ACTIVITY);

                                    } else { // ERROR API
                                        Toast.makeText(getApplicationContext(),
                                                response.getString("datos"), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                VolleyLog.d(TAG, "Error: " + error.getMessage());
                                Toast.makeText(getApplicationContext(),
                                        R.string.error_conexion, Toast.LENGTH_SHORT).show();

                                LoginManager.getInstance().logOut();
                                mLoginButton.setVisibility(View.VISIBLE);
                                indicadorCargando.setVisibility(View.INVISIBLE);
                                loggedIn = false;

                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putBoolean("loggedIn", loggedIn); // value to store
                                editor.commit();
                            }
                        });

                        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                        // Se añade la petición a la Cola de Peticiones de Volley
                        PeticionesServidorVolley.getInstance().addToRequestQueue(jsonObjReq);

                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,link");
        request.setParameters(parameters);
        request.executeAsync();
    }

}

