package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class Maps extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private WebView webView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double userLatitude = 50.4; // default values
    private double userLongitude = 14.3; // default values

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

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

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    Intent i1 = new Intent(Maps.this, HomeActivity.class);
                    startActivity(i1);
                    Toast.makeText(Maps.this, "Home clicked", Toast.LENGTH_SHORT).show();
                }else if (id == R.id.Recommendations) {
                    Intent i2 = new Intent(Maps.this, ActivityRecommendationActivity.class);
                    startActivity(i2);
                    Toast.makeText(Maps.this, "Recommendations clicked", Toast.LENGTH_SHORT).show();
                }
                else if (id == R.id.nav_maps) {
                    Intent i2 = new Intent(Maps.this, Maps.class);
                    startActivity(i2);
                    Toast.makeText(Maps.this, "Profile clicked", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_chats) {
                    Toast.makeText(Maps.this, "Settings clicked", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.Logout) {
                    Intent i3 = new Intent(Maps.this, MainActivity.class);
                    startActivity(i3);
                    Toast.makeText(Maps.this, "Logout successfully", Toast.LENGTH_SHORT).show();
                }

                // Close drawer after item click
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        webView = findViewById(R.id.webview);

        // Enable JavaScript for the WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // Prevent WebView from opening the default browser
        webView.setWebViewClient(new WebViewClient());

        // Initialize the FusedLocationProviderClient to get user's location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getUserLocationAndLoadMap();
    }

    // Method to get user location
    @SuppressLint("MissingPermission")
    private void getUserLocationAndLoadMap() {
        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    userLatitude = location.getLatitude();
                    userLongitude = location.getLongitude();

                    // Load the Windy map with user's location
                    loadWindyMap();
                } else {
                    Toast.makeText(Maps.this, "Unable to fetch location", Toast.LENGTH_SHORT).show();

                    // Load the default Windy map if location is not available
                    loadWindyMap();
                }
            }
        });
    }

    // Method to load the Windy Map into the WebView
    private void loadWindyMap() {
        String windyUrl = "https://embed.windy.com/embed2.html?key=Q6gJJDpkDS67Esx41VgCznDWG9IcSxdh"
                + "&lat=" + userLatitude
                + "&lon=" + userLongitude
                + "&zoom=8"
                + "&overlay=wind";

        // Load the Windy map URL into the WebView
        webView.loadUrl(windyUrl);
    }
}
