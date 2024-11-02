package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Chats extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private ListView messageList;
    private EditText messageInput;
    private Button sendButton;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReference1;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messages;
    private FirebaseAuth auth;
    private String username;
    private TextView usernameDisplay; // TextView to display the username

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        createNotificationChannel();



        // Initialize Firebase Auth and Database reference
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("messages");

        // Initialize UI elements
        usernameDisplay = findViewById(R.id.username_display); // This must match the ID in your layout
        messageList = findViewById(R.id.message_list);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages);
        messageList.setAdapter(messageAdapter);

        String[] loginDetails = retrieveLoginDetails();
        if (loginDetails != null) {
            String email = loginDetails[0];

            // Use the email to find the username
            findUsername(email);
        } else {
            Toast.makeText(this, "Error retrieving login details", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if login details are not found
            return;
        }

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chats");
        }

        // Drawer layout and toggle setup
        drawerLayout = findViewById(R.id.drawer_layout1);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation view setup
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener (item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent i4 = new Intent(Chats.this, HomeActivity.class);
                startActivity(i4);
                finish();  // Close this activity
            } else if (id == R.id.nav_maps) {
                Intent i2 = new Intent(Chats.this, Maps.class);
                startActivity(i2);
                finish();
            } else if (id == R.id.nav_chats) {
                Toast.makeText(Chats.this, "You are already in Chats", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.Logout) {
                auth.signOut();
                Intent i3 = new Intent(Chats.this, MainActivity.class);
                i3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // Clear stack on logout
                startActivity(i3);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Load existing messages
        loadMessages();

        // Set long-click listener for deleting messages
        messageList.setOnItemLongClickListener((AdapterView<?> parent , View view, int position, long id) -> {
            Message selectedMessage = messages.get(position);
            showDeleteConfirmationDialog(selectedMessage);
            return true;
        });

        // Initially disable the send button
        sendButton.setEnabled(false);

        // Set click listener for send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }
    private void createNotificationChannel() {
        // Check if the device is running on Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Define the channel ID, name, and description
            String channelId = "chateaubriands";
            String channelName = "Chats";
            String channelDescription = "Channel for My Notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH; // Set the importance level

            // Create the NotificationChannel
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);

            // Optional: Set additional channel properties
            channel.enableLights(true); // Enable lights
            channel.enableVibration(true); // Enable vibration
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000}); // Vibration pattern
            channel.setShowBadge(true); // Show badge on app icon

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) { // Ensure message is not empty
            String messageId = databaseReference.push().getKey();
            long timestamp = System.currentTimeMillis(); // Get current timestamp
            Message message = new Message(username, messageText, messageId, timestamp); // Include timestamp in Message constructor

            // Log the message being sent for debugging
            Log.d(TAG, "Sending message: " + messageText);

            databaseReference.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        messageInput.setText(""); // Clear input field
                        // Send notification to all users
                        try {
                            sendNotification("New Message from " + username, messageText);
                        } catch (Exception e) {
                            Log.e(TAG, "Error sending notification", e);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to send message", e);
                        Toast.makeText(Chats.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(Chats.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMessages() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        messages.add(message);
                    }
                }
                Collections.reverse(messages); // Show latest messages at the bottom
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatsActivity", "Failed to load messages", databaseError.toException());
                Toast.makeText(Chats.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(Message message) {
        // Check if the current user is the sender
        if (!message.getUsername().equals(username)) {
            Toast.makeText(this, "You can only delete your own messages.", Toast.LENGTH_SHORT).show();
            return; // Exit if the user is not the sender
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Yes", (DialogInterface dialog, int which) -> deleteMessage(message))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteMessage(Message message) {
        databaseReference.child(message.getMessageid()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Chats.this, "Message deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatsActivity", "Failed to delete message", e);
                    Toast.makeText(Chats.this, "Failed to delete message", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Method to retrieve login details from file
    private String[] retrieveLoginDetails() {
        try {
            FileInputStream fis = openFileInput("login_details.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String loginDetails = br.readLine();
            String[] details = loginDetails.split(",");
            return details;
        } catch (IOException e) {
            return null;
        }
    }

    // Method to find the username using the email
    private void findUsername(String email) {
        Log.d("ChatsActivity", "Finding username for email: " + email); // Log the email being searched
        databaseReference1 = FirebaseDatabase.getInstance().getReference("user");
        databaseReference1.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String username = userSnapshot.child("name").getValue(String.class);
                        Chats.this.username = username; // Store the retrieved username
                        if (usernameDisplay != null) {
                            usernameDisplay.setText("Username : " + username); // Display the username
                        } else {
                            Log.e("ChatsActivity", "usernameDisplay is null");
                        }
                        Toast.makeText(Chats.this, "Username: " + username, Toast.LENGTH_SHORT).show();
                        sendButton.setEnabled(true); // Enable the send button after username is retrieved
                    }
                } else {
                    Log.d("ChatsActivity", "Username not found for email: " + email); // Log if not found
                    Toast.makeText(Chats.this, "Username not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatsActivity", "Failed to find username", databaseError.toException());
                Toast.makeText(Chats.this, "Error finding username", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void sendNotification(String title, String message) {
        // Create a notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "chatsandnews")
                .setSmallIcon(R.drawable.logo) // Ensure this resource exists
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo)) // Ensure this resource exists
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // Dismiss the notification when clicked

        // Create a pending intent to open the app when the notification is clicked
        Intent intent = new Intent(this, MainActivity.class);
        // Use FLAG_IMMUTABLE for security reasons
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        notificationBuilder.setContentIntent(pendingIntent);

        // Show the notification
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(12345, notificationBuilder.build());
        } else {
            Log.e(TAG, "NotificationManager is null");
        }
    }
}