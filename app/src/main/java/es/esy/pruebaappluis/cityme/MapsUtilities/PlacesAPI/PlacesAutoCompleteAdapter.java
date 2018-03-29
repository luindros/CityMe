package es.esy.pruebaappluis.cityme.MapsUtilities.PlacesAPI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import es.esy.pruebaappluis.cityme.R;
import es.esy.pruebaappluis.cityme.Volley.PeticionesServidorVolley;

/**
 * Adaptador que gestiona los resultados obtenidos al realizar el autocompletado de sitio.
 *
 * @author Luis Iglesias Muñoz
 * @version 1.0
 */
public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    ArrayList<String> resultList;

    Context mContext;
    int mResource;

    PlaceAPI mPlaceAPI = new PlaceAPI();

    public PlacesAutoCompleteAdapter(Context context, int resource) {
        super(context, resource);

        mContext = context;
        mResource = resource;
    }

    /**
     *
     * @return restultados obtenidos
     */
    @Override
    public int getCount() {
        // Last item will be the footer
        return resultList.size();
    }

    /**
     *
     * @param position
     * @return resultado de la posición indicada
     */
    @Override
    public String getItem(int position) {
        return resultList.get(position);
    }

    /**
     * Devuelve un filtro que se utilizará como patrón de filtrado al realizar el autocompletado.
     *
     * @return filtro
     */
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    if (!PeticionesServidorVolley.compruebaConexion(mContext)) {
                        Toast.makeText(mContext, R.string.necesaria_conexion, Toast.LENGTH_SHORT).show();
                    } else {
                        mPlaceAPI.autocomplete(constraint.toString(),mContext);
                        resultList = new ArrayList<>(mPlaceAPI.resultList);
                        if (resultList.size() != 0) {
                            // Footer
                            resultList.add("");
                        }


                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return filter;
    }

    /**
     * Obtiene una vista que muestra el resultado de la posición indicada
     *
     * @param position
     * @param convertView
     * @param parent ViewGroup padre al se adjuntará la vista
     * @return vista que mostrará el resultado
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (position != (resultList.size() - 1))
            view = inflater.inflate(R.layout.list_item_autocomplete, null);
        else
            view = inflater.inflate(R.layout.autocomplete_google_logo, null);

        if (position != (resultList.size() - 1)) {
            TextView autocompleteTextView = (TextView) view.findViewById(R.id.autocompleteText);
            autocompleteTextView.setText(resultList.get(position));
        } else {

            view.setClickable(false);

        }

        return view;
    }

}
