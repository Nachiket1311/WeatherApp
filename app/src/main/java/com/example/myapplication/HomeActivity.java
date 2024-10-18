package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.Arrays;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private VideoView videoView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private double userLatitude = 50.4; // Default values
    private double userLongitude = 14.3; // Default values

    @SuppressLint({"ResourceType", "WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize VideoView
        videoView = findViewById(R.id.background_image);

        // Initialize the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Request location permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Fetch the location
            fetchLocation();
        }

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        // Drawer layout and toggle setup
        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation view setup
        navigationView = findViewById(R.id.nav_view);

        // Get the header view from the navigation view
        View headerView = navigationView.getHeaderView(0);
        TextView tvHeaderName = headerView.findViewById(R.id.tvHeaderName);
        TextView tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);

        // Get Firebase user info
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();
            String displayName = currentUser.getDisplayName();

            // Set the header text with user info
            tvHeaderName.setText(displayName != null ? displayName : "No Name");
            tvHeaderEmail.setText(email);
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    Toast.makeText(HomeActivity.this, "You are already on Home", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_maps) {
                    Intent i2 = new Intent(HomeActivity.this, Maps.class);
                    startActivity(i2);
                    Toast.makeText(HomeActivity.this, "Maps clicked", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_chats) {
                    Toast.makeText(HomeActivity.this, "Chats clicked", Toast.LENGTH_SHORT).show();
                    Intent i4 = new Intent(HomeActivity.this, Chats.class);
                    startActivity(i4);
                } else if (id == R.id.Logout) {
                    Intent i3 = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(i3);
                    Toast.makeText(HomeActivity.this, "Logout successfully", Toast.LENGTH_SHORT).show();
                }

                // Close drawer after item click
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    // Function to fetch user's current location
    @SuppressLint("MissingPermission")
    private void fetchLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    userLatitude = location.getLatitude();
                    userLongitude = location.getLongitude();

                    // After fetching the location, make the weather API call
                    fetchWeatherStatus();
                } else {
                    userLatitude = 50.4;  // Default values
                    userLongitude = 14.3; // Default values
                    Toast.makeText(HomeActivity.this, "Unable to fetch current location, using default coordinates", Toast.LENGTH_SHORT).show();
                    fetchWeatherStatus(); // Proceed with the default coordinates
                }
            }
        });
    }

    private void fetchWeatherStatus() {
        WindyApi apiService = RetrofitInstance.getRetrofitInstance().create(WindyApi.class);

        WeatherRequest requestBody = new WeatherRequest(
                userLatitude,  // Latitude value
                userLongitude, // Longitude value
                "gfs",  // Model (e.g., GFS global model)
                Arrays.asList("wind", "dewpoint", "rh", "pressure"),  // List of parameters
                Arrays.asList("surface", "800h", "300h"),  // Levels
                "r1SaZh5rymWwOLRlTdlprY0aX7H3eoZj"  // Your API key
        );
        Call<WeatherResponse> call = apiService.getWeather(requestBody);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Handle successful response
                    WeatherResponse weatherResponse = response.body();
                    Log.d("Weather API", "Raw Response: " + response.body().toString());

                    // Log the weather data to verify it is correct
                    Log.d("Weather API", "Weather Response: " + weatherResponse.toString());

                    float windSpeed = weatherResponse.getWindSpeedMagnitude();
                    float temperature = weatherResponse.getTemperature();
                    float precipitation = weatherResponse.getPrecipitation();
                    float cloudStat = weatherResponse.getHighCloudCover();

                    // Log values for debugging
                    Log.d("Weather API", "Wind Speed: " + windSpeed);
                    Log.d("Weather API", "Temperature: " + temperature);
                    Log.d("Weather API", "Precipitation: " + precipitation);
                    Log.d("Weather API", "Cloud Cover: " + cloudStat);

                    // Proceed with weather status determination
                    String weatherStatus;
                    if (precipitation > 0.0) {
                        weatherStatus = "rain";
                    } else if (windSpeed > 43.0) {
                        weatherStatus = "thunderstorms";
                    } else if (cloudStat >= 70.0) {
                        weatherStatus = "cloudy";
                    } else if (cloudStat >= 25.0 && cloudStat < 70.0) {
                        weatherStatus = "partly_cloudy";
                    } else {
                        weatherStatus = "default";
                    }

                    setVideoBasedOnWeather(weatherStatus);
                } else {
                    // Handle unsuccessful response
                    try {
                        String errorResponse = response.errorBody().string();
                        Log.e("Weather API", "Failed response: " + errorResponse);
                        Toast.makeText(HomeActivity.this, "Failed to fetch weather: " + errorResponse, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e("Weather API", "Error reading error response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                // Handle failure
                Log.e("Weather API", "API call failed: " + t.getMessage());
                Toast.makeText(HomeActivity.this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setVideoBasedOnWeather(String weatherStatus) {
        int videoResourceId;
        switch (weatherStatus) {
            case "rain":
                videoResourceId = R.raw.rain;
                break;
            case "thunderstorms":
                videoResourceId = R.raw.rain_with_thunder;
                break;
            case "cloudy":
                videoResourceId = R.raw.cloudy;
                break;
            case "partly_cloudy":
                videoResourceId = R.raw.partly_cloudy;
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
        // Resume video playback
        if (videoView != null) {
            videoView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause video playback
        if (videoView != null) {
            videoView.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation(); // Fetch location if permission granted
            } else {
                Toast.makeText(this, "Location permission is required to fetch weather data.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Handle back press
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
