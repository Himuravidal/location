package cl.adachersoft.location.background;

import android.os.AsyncTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cl.adachersoft.location.networks.GeoGet;
import cl.adachersoft.location.networks.GeoInterceptor;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by cristian on 13-12-2016.
 */

public class LocationByAddress extends AsyncTask<String, Void, String[]>

    {

        private LocationCallback callback;
        public LocationByAddress(LocationCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String[] doInBackground(String... params) {
            //TODO this vallidation is pointless we have to make sure the texts are not empty, this texts come from and edit text, so worst case scenario is empty text ""
            if (params.length > 0) {
                String address = "";
                for (String param : params) {
                    address = param+", ";
                }
                GeoGet geoGet = new GeoInterceptor().getInterceptor();
                Map<String, String> queryMap = new HashMap<>();
                queryMap.put("address", address);
                queryMap.put("sensor", "true");
                Call<JsonObject> jsonObjectCall = geoGet.get(queryMap);
                try {
                    Response<JsonObject> response = jsonObjectCall.execute();
                    JsonObject mainObject = response.body();
                    if (response.isSuccessful() && mainObject.get("status").toString().equals("\"OK\"")) {
                        JsonArray results = mainObject.getAsJsonArray("results");
                        JsonObject innerResult = (JsonObject) results.get(0);
                        JsonObject geometry = (JsonObject) innerResult.get("geometry");
                        JsonObject location = (JsonObject) geometry.get("location");
                        String lat = location.get("lat").toString();
                        String lng = location.get("lng").toString();
                        String[] coordinates = {lat, lng};
                        return coordinates;
                    } else {
                        callback.noAddress();
                        return null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.fail();
                    return null;
                }

            } else {
                callback.fail();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null) {
                callback.success(strings[0], strings[1]);
            }
        }
    }

