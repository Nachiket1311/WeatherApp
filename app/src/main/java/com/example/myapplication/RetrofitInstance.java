package com.example.myapplication;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {

    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Create a logging interceptor to log HTTP details
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Build OkHttpClient with logging interceptor
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            // Build Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.windy.com/") // Ensure this is correct
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static void fetchWindyData() {
        // Create API service instance
        WindyApi apiService = getRetrofitInstance().create(WindyApi.class);

        // Make an asynchronous API call
        Call<Object> call = apiService.getWindyData();
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    // Log success response
                    System.out.println("HTTP Code: " + response.code());
                    System.out.println("Response Body: " + response.body());
                } else {
                    // Log non-successful HTTP status code
                    System.out.println("HTTP Code: " + response.code());
                    System.out.println("Error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                t.printStackTrace(); // Log any failure
            }
        });
    }
}
