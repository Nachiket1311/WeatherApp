package com.example.myapplication;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

// Define the structure of the request body
class WeatherRequest {
    double lat;
    double lon;
    String model;
    List<String> parameters;
    List<String> levels;
    String key;

    // Constructor
    public WeatherRequest(double lat, double lon, String model, List<String> parameters, List<String> levels, String key) {
        this.lat = lat;
        this.lon = lon;
        this.model = model;
        this.parameters = parameters;
        this.levels = levels;
        this.key = key;
    }
}

// Retrofit interface
public interface WindyApi {
    @POST("api/point-forecast/v2")
    Call<WeatherResponse> getWeather(@Body WeatherRequest requestBody);

    @GET("/path_to_endpoint?parameters") // Specify the API path here
    Call<Object> getWindyData();
}
