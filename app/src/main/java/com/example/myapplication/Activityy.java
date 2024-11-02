package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private final String API_KEY = "YOUR_OPENWEATHERMAP_API_KEY";
    private RecyclerView activityRecyclerView;
    private ActivityAdapter activityAdapter;
    private List<ActivityRecommendation> recommendedActivities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activityRecyclerView = findViewById(R.id.activity_recycler_view);
        activityRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        activityAdapter = new ActivityAdapter(recommendedActivities);
        activityRecyclerView.setAdapter(activityAdapter);

        fetchWeatherData();
    }

    private void fetchWeatherData() {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=YOUR_CITY_NAME&appid=" + API_KEY;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        JSONObject main = json.getJSONObject("main");
                        double temp = main.getDouble("temp") - 273.15; // Convert from Kelvin to Celsius
                        String weather = json.getJSONArray("weather").getJSONObject(0).getString("main");

                        runOnUiThread(() -> updateActivitiesBasedOnWeather(temp, weather));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void updateActivitiesBasedOnWeather(double temp, String weather) {
        recommendedActivities.clear();

        if (weather.equals("Clear")) {
            recommendedActivities.add(new ActivityRecommendation("Go for a Hike", "Perfect day for hiking"));
            recommendedActivities.add(new ActivityRecommendation("Picnic at the Park", "Enjoy sunny weather at the park"));
        } else if (weather.equals("Rain")) {
            recommendedActivities.add(new ActivityRecommendation("Visit a Museum", "Stay dry indoors and explore art"));
            recommendedActivities.add(new ActivityRecommendation("Watch a Movie", "Perfect for a cozy movie day at home"));
        } else if (weather.equals("Snow")) {
            recommendedActivities.add(new ActivityRecommendation("Build a Snowman", "Enjoy the snow with friends"));
            recommendedActivities.add(new ActivityRecommendation("Skiing", "Great day to hit the slopes"));
        }

        activityAdapter.notifyDataSetChanged();
    }
}
