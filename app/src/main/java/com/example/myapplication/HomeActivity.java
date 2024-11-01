package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private VideoView videoView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    // Weather API Key (IMPORTANT: Replace with your actual API key)
    private static final String API_KEY = "e454a2b324237efbd47e83a16f3c4eae";

    // UI Components
    private TextView cityName;
    private EditText searchbar;
    private TextView weatherInfo;
    private ExecutorService executorService;
    private TextView feelsLikeText;
    private TextView windSpeedText;
    private TextView airPressureText;
    private TextView visibilityText;
    private TextView uvIndexText; // Assuming you have UV data from another API or source

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize ExecutorService
        executorService = Executors.newSingleThreadExecutor();

        // Initialize UI elements
        initializeUIComponents();

        // Setup Navigation and Toolbar
        setupNavigationAndToolbar();

        // Check and request location permissions
        checkLocationPermissions();
    }

    @SuppressLint("WrongViewCast")
    private void initializeUIComponents() {
        videoView = findViewById(R.id.background_image);
        cityName = findViewById(R.id.city_name);
        weatherInfo = findViewById(R.id.weather);
        searchbar = findViewById(R.id.search_bar);
        Button searchButton = findViewById(R.id.search_button);
        feelsLikeText = findViewById(R.id.feels_like);
        windSpeedText = findViewById(R.id.wind_speed);
        airPressureText = findViewById(R.id.air_pressure);
        visibilityText = findViewById(R.id.visibility);
        uvIndexText = findViewById(R.id.uv_index); // If you have UV data available
        // Initialize the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up search button listener

        searchButton.setOnClickListener(view -> {
            String city = searchbar.getText().toString();
            if (!city.isEmpty()) {
                String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=metric";
                cityName.setText(city);
                fetchWeather(url);
            } else {
                Toast.makeText(HomeActivity.this, "Enter a city name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigationAndToolbar() {
        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        // Drawer layout and toggle setup
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation view setup
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.nav_home) {
                Toast.makeText(HomeActivity.this, "You are already on Home", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_maps) {
                intent = new Intent(HomeActivity.this, Maps.class);
            } else if (id == R.id.nav_chats) {
                intent = new Intent(HomeActivity.this, Chats.class);
            } else if (id == R.id.Logout) {
                intent = new Intent(HomeActivity.this, MainActivity.class);
                Toast.makeText(HomeActivity.this, "Logout successful", Toast.LENGTH_SHORT).show();
            }

            // Close drawer and start activity if intent is set
            drawerLayout.closeDrawer(GravityCompat.START);
            if (intent != null) {
                startActivity(intent);
            }
            return true;
        });
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Fetch the location
            fetchLocation();
        }
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

    // Function to fetch weather data using ExecutorService
    private void fetchWeather(String urlString) {
        executorService.execute(() -> {
            String result = fetchWeatherData(urlString);
            runOnUiThread(() -> updateWeatherInfo(result));
        });
    }

    // Fetching weather data in a background thread
    private String fetchWeatherData(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            try (InputStream inputStream = urlConnection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                return result.toString();
            }
        } catch (IOException e) {
            Log.e("HomeActivity", "Error fetching weather data", e);
            return null;
        }
    }

    // Update UI after fetching weather data
    @SuppressLint({"SetTextI18n", "SetTextI18n"})
    private void updateWeatherInfo(String result) {
        try {
            if (result != null) {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject mainObj = jsonObject.getJSONObject("main");
                String weatherStatus = jsonObject.getJSONArray("weather").getJSONObject(0).getString("main");

                Log.d("WeatherStatus", "Current weather status: " + weatherStatus);


                // Extract specific weather details
                double temp = mainObj.getDouble("temp");
                double feelsLike = mainObj.getDouble("feels_like");
                int humidity = mainObj.getInt("humidity");
                double windSpeed = jsonObject.getJSONObject("wind").getDouble("speed");
                double airPressure = mainObj.getDouble("pressure");
                double visibility = jsonObject.getDouble("visibility") / 1000; // Convert to kilometers

                // Update UI elements
                cityName.setText(jsonObject.getString("name")); // Set city name
                weatherInfo.setText(String.format("Temperature: %.1f°C\nHumidity: %d%%", temp, humidity));
                feelsLikeText.setText(String.format("Feels Like: %.1f°C", feelsLike));
                windSpeedText.setText(String.format("Wind Speed: %.1f m/s", windSpeed));
                airPressureText.setText(String.format("Pressure: %.1f hPa", airPressure));
                visibilityText.setText(String.format("Visibility: %.1f km", visibility));

                // UV Index - Assuming you have a way to fetch this data
                // Uncomment and update if you have UV data available
                // double uvIndex = ...; // Fetch UV index from another API or source
                // uvIndexText.setText(String.format("UV Index: %.1f", uvIndex));

                setVideoBasedOnWeather(weatherStatus.toLowerCase());
            } else {
                weatherInfo.setText("Weather data unavailable");
            }
        } catch (JSONException e) {
            Log.e("HomeActivity", "Error parsing weather data", e);
            weatherInfo.setText("Error fetching weather");
        }
    }
    // Function to fetch user's current location
    @SuppressLint("MissingPermission")
    private void fetchLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Use the location to fetch weather data
                        @SuppressLint("DefaultLocale") String url = String.format(
                                "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric",
                                location.getLatitude(),
                                location.getLongitude(),
                                API_KEY
                        );
                        fetchWeather(url);
                    } else {
                        Toast.makeText(HomeActivity.this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeActivity", "Failed to fetch location", e);
                    Toast.makeText(HomeActivity.this, "Location retrieval failed", Toast.LENGTH_SHORT).show();
                });
    }

    // Set video background based on weather condition
    private void setVideoBasedOnWeather(String weatherStatus) {
        int videoResourceId;
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // Get current hour (0-23)

        switch (weatherStatus) {
            case "rain":
                videoResourceId = R.raw.rain;
                break;
            case "thunderstorm":
                videoResourceId = R.raw.rain_with_thunder;
                break;
            case "clouds":
                videoResourceId = R.raw.cloudy;
                break;
            case "clear":
                // Determine the time of day and set the appropriate video
                if (hour >= 5 && hour < 12) { // Morning: 5 AM to 11:59 AM
                    videoResourceId = R.raw.clear_morning; // Ensure this video exists
                } else if (hour >= 12 && hour < 17) { // Afternoon: 12 PM to 4:59 PM
                    videoResourceId = R.raw.clear_afternoon; // Ensure this video exists
                } else if (hour >= 17 && hour < 21) { // Evening: 5 PM to 8:59 PM
                    videoResourceId = R.raw.clear_evening; // Ensure this video exists
                } else { // Night: 9 PM to 4:59 AM
                    videoResourceId = R.raw.clear_night; // Ensure this video exists
                }
                break;
            default:
                videoResourceId = R.raw.default_video;
                break;
        }

        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + videoResourceId));
        videoView.setOnPreparedListener(mp -> mp.setLooping(true));
        videoView.start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null) {
            videoView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null) {
            videoView.pause();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown executor service to prevent memory leaks
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}