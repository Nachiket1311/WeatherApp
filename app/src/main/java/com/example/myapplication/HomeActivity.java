package com.example.myapplication;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.MediaController;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private VideoView videoView;

    @SuppressLint({"ResourceType", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize VideoView AFTER setContentView
        videoView = findViewById(R.id.background_image);

        // Check if the VideoView exists in the layout
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.clear_night);
        videoView.setVideoURI(videoUri);

        // Add an error listener to catch any issues with playback
        videoView.setOnErrorListener((mediaPlayer, what, extra) -> {
            Toast.makeText(HomeActivity.this, "Error playing video!", Toast.LENGTH_SHORT).show();
            return true; // Return true if error is handled
        });

        // Start the video automatically when it's ready
        videoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true); // Optional: loop the video if needed
            videoView.start();
        });



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
                    Toast.makeText(HomeActivity.this, "Home clicked", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_profile) {
                    Toast.makeText(HomeActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_settings) {
                    Toast.makeText(HomeActivity.this, "Settings clicked", Toast.LENGTH_SHORT).show();
                }

                // Close drawer after item click
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
