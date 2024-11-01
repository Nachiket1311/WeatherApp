package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class Chats extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private ListView messageList;
    private EditText messageInput;
    private Button sendButton;
    private DatabaseReference databaseReference;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messages;
    private FirebaseAuth auth;
    private String username;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        // Initialize Firebase Auth and Database reference
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("messages");

        // Get current user and set username
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            username = user.getDisplayName();
            if (username == null) {
                username = "Anonymous";  // Default name if display name is not set
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();  // Close activity if user is not logged in
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
        navigationView.setNavigationItemSelectedListener(item -> {
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

        // UI elements
        messageList = findViewById(R.id.message_list);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages);
        messageList.setAdapter(messageAdapter);

        // Load existing messages
        loadMessages();

        // Send message on button click
        sendButton.setOnClickListener(view -> sendMessage());

        // Set long-click listener for deleting messages
        messageList.setOnItemLongClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            Message selectedMessage = messages.get(position);
            showDeleteConfirmationDialog(selectedMessage);
            return true;
        });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            String messageId = databaseReference.push().getKey();
            Message message = new Message(username, messageText, messageId); // Include messageId in Message constructor

            databaseReference.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        messageInput.setText(""); // Clear input field
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ChatsActivity", "Failed to send message", e);
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
        new AlertDialog.Builder(this)
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Yes", (DialogInterface dialog, int which) -> deleteMessage(message))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteMessage(Message message) {
        databaseReference.child(message.getMessageId()).removeValue()
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
}
