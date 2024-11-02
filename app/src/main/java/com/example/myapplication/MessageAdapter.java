package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends ArrayAdapter<Message> {
    private final Context context;
    private final List<Message> messages;

    // Date format for displaying message time
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MessageAdapter(Context context, ArrayList<Message> messages) {
        super(context, 0, messages);
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // ViewHolder pattern for better performance
        ViewHolder holder;

        // Inflate the layout if necessary
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.message_item, parent, false);
            holder = new ViewHolder();
            holder.usernameText = convertView.findViewById(R.id.username_display);
            holder.messageText = convertView.findViewById(R.id.message_text);
            holder.messageTime = convertView.findViewById(R.id.message_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the message item
        Message message = messages.get(position);

        // Set the message data
        if (message != null) {
            holder.usernameText.setText(message.getUsername());
            holder.messageText.setText(message.getMessageText());

            // Format the timestamp
            long timestamp = message.getTimestamp();
            if (timestamp > 0) {
                Date date = new Date(timestamp);
                holder.messageTime.setText(DATE_FORMAT.format(date));
            } else {
                holder.messageTime.setText("");
            }
        } else {
            holder.usernameText.setText("");
            holder.messageText.setText("");
            holder.messageTime.setText("");
        }

        return convertView;
    }

    // ViewHolder class to hold views for recycling
    static class ViewHolder {
        TextView usernameText;
        TextView messageText;
        TextView messageTime;
    }
}