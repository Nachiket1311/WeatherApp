package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class Chats extends AppCompatActivity {

    private ListView messageList;
    private EditText messageInput;
    private Button sendButton;
    private DatabaseReference databaseReference;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        messageList = findViewById(R.id.message_list);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages);
        messageList.setAdapter(messageAdapter);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("messages");

        // Load existing messages
        loadMessages();

        // Send message on button click
        sendButton.setOnClickListener(view -> sendMessage());
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            String username = "Your Username"; // Replace this with actual username logic
            Message message = new Message(username, messageText);
            databaseReference.push().setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        messageInput.setText(""); // Clear input field
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ChatActivity", "Failed to send message", e);
                        Toast.makeText(Chats.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadMessages() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    messages.add(message);
                }
                Collections.reverse(messages); // To show the latest messages at the bottom
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatActivity", "Failed to load messages", databaseError.toException());
            }
        });
    }
}
