package cl.adachersoft.location.background;

import com.google.gson.JsonObject;

import java.util.Map;

import cl.adachersoft.location.networks.GeoGet;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Created by cristian on 13-12-2016.
 */

public interface LocationCallback {


    void noAddress();
    void fail();
    void success(String lat, String lng);
}
