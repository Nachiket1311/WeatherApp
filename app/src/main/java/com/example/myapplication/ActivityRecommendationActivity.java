package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ActivityRecommendationActivity extends AppCompatActivity {

    private ActivityAdapter activityAdapter;
    private final List<ActivityRecommendation> recommendedActivities = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activityy);

        // Setup RecyclerView
        RecyclerView activityRecyclerView = findViewById(R.id.activity_recycler_view);
        activityRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        activityAdapter = new ActivityAdapter(recommendedActivities);
        activityRecyclerView.setAdapter(activityAdapter);

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup Navigation and Toolbar
        setupNavigationAndToolbar();

        // Check location permissions and fetch location
        checkLocationPermissionsAndFetchLocation();
    }

    private void setupNavigationAndToolbar() {
        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar); // Make sure you have a toolbar in your layout
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        // Drawer layout and toggle setup
        drawerLayout = findViewById(R.id.drawer_layout); // Ensure this layout exists
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation view setup
        NavigationView navigationView = findViewById(R.id.nav_view); // Ensure this layout exists
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.nav_home) {
                intent = new Intent(ActivityRecommendationActivity.this,HomeActivity.class);
            } else if (id == R.id.Recommendations) {
                // No action needed, you are already in this activity
                Toast.makeText(ActivityRecommendationActivity.this, "You are already on Recommmendations", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_maps) {
                intent = new Intent(ActivityRecommendationActivity.this, Maps.class);
            } else if (id == R.id.nav_chats) {
                intent = new Intent(ActivityRecommendationActivity.this, Chats.class);
            } else if (id == R.id.Logout) {
                intent = new Intent(ActivityRecommendationActivity.this, MainActivity.class);
                Toast.makeText(ActivityRecommendationActivity.this, "Logout successful", Toast.LENGTH_SHORT).show();
            }

            // Close drawer and start activity if intent is set
            drawerLayout.closeDrawer(GravityCompat.START);
            if (intent != null) {
                startActivity(intent);
            }
            return true;
        });
    }

    private void checkLocationPermissionsAndFetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Fetch weather data using the location
                        fetchWeatherData(location.getLatitude (), location.getLongitude());
                    } else {
                        Toast.makeText(ActivityRecommendationActivity.this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ActivityRecommendationActivity.this, "Failed to fetch location", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchWeatherData(double latitude, double longitude) {
        String API_KEY = "e454a2b324237efbd47e83a16f3c4eae";
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY;

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
        } else if (weather.equals("Thunderstorm")) {
            recommendedActivities.add(new ActivityRecommendation("Stay Indoors", "It's best to stay safe indoors during a thunderstorm"));
            recommendedActivities.add(new ActivityRecommendation("Read a Book", "Catch up on your reading while it's stormy outside"));
        } else if (weather.equals("Clouds")) {
            recommendedActivities.add(new ActivityRecommendation("Go for a Walk", "A perfect day for a leisurely walk"));
            recommendedActivities.add(new ActivityRecommendation("Visit a Café", "Enjoy a warm drink at your favorite café"));
        } else if (weather.equals("Drizzle")) {
            recommendedActivities.add(new ActivityRecommendation("Take an Umbrella Walk", "Enjoy the gentle rain with an umbrella"));
            recommendedActivities.add(new ActivityRecommendation("Watch a Movie", "Perfect for a cozy movie day at home"));
        } else if (weather.equals("Snow")) {
            recommendedActivities.add(new ActivityRecommendation("Build a Snowman", "Enjoy the snow with friends"));
            recommendedActivities.add(new ActivityRecommendation("Skiing", "Great day to hit the slopes"));
        } else if (weather.equals("Mist")) {
            recommendedActivities.add(new ActivityRecommendation("Photography Day", "Capture the beauty of misty landscapes"));
            recommendedActivities.add(new ActivityRecommendation("Go for a Nature Walk", "Enjoy a serene walk in the mist"));
        } else if (weather.equals("Fog")) {
            recommendedActivities.add(new ActivityRecommendation("Stay Indoors", "It's best to stay safe indoors when it's foggy"));
            recommendedActivities.add(new ActivityRecommendation("Enjoy a Hot Drink", "Perfect time for a warm cup of tea or coffee"));
        } else if (weather.equals("Smoke")) {
            recommendedActivities.add(new ActivityRecommendation("Stay Indoors", "Avoid outdoor activities due to smoke"));
            recommendedActivities.add(new ActivityRecommendation("Watch a Movie", "Cozy up indoors with a good film"));
        } else if (weather.equals("Haze")) {
            recommendedActivities.add(new ActivityRecommendation("Stay Indoors", "Limit outdoor activities due to haze"));
            recommendedActivities.add(new ActivityRecommendation("Read a Book", "Perfect time to catch up on your reading indoors"));
        } else if (weather.equals("Dust")) {
            recommendedActivities.add(new ActivityRecommendation("Stay Indoors", "Avoid outdoor activities due to dust"));
            recommendedActivities.add(new ActivityRecommendation("Clean the House", "A good time to tidy up indoors"));
        } else if (weather.equals("Sand")) {
            recommendedActivities.add(new ActivityRecommendation("Stay Indoors", "Avoid outdoor activities due to sand storms"));
            recommendedActivities.add(new ActivityRecommendation("Watch a Movie", "Cozy up indoors with a good film"));
        } else if (weather.equals("Ash")) {
            recommendedActivities.add(new ActivityRecommendation("Stay Indoors", "Avoid outdoor activities due to ash"));
            recommendedActivities.add(new ActivityRecommendation("Indoor Exercise", "Try a workout at home"));
        } else if (weather.equals("Squall")) {
            recommendedActivities.add(new ActivityRecommendation("Stay Indoors", "It's best to stay safe indoors during a squall"));
            recommendedActivities.add(new ActivityRecommendation("Catch Up on Work", "Use this time to get some work done indoors"));
        } else if (weather.equals("Tornado")) {
            recommendedActivities.add(new ActivityRecommendation("Seek Shelter", "Find a safe place until the tornado passes"));
            recommendedActivities.add(new ActivityRecommendation("Stay Informed", "Keep track of weather updates and alerts"));
        } else {
            Toast.makeText(this, "Some other weather status", Toast.LENGTH_SHORT).show();
        }

        activityAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}