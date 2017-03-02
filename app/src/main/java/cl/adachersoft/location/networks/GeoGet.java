package cl.adachersoft.location.networks;

import com.google.gson.JsonObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Created by cristian on 13-12-2016.
 */

public interface GeoGet {


    @GET("api/geocode/json")
    Call<JsonObject> get(@QueryMap Map<String, String> map);


}
