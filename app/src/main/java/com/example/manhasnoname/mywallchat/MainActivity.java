package com.example.manhasnoname.mywallchat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // User's Data
    String usernameDto, messageDto;

    // Random generated key for storing message to DB
    String randomKey;

    // Form's assets
    Button sendMessage;
    EditText userET;
    EditText messageET;
    TextView chatTV;

    // Database Variables
    FirebaseDatabase database;
    DatabaseReference root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Attach code to assets
        userET = (EditText) findViewById(R.id.userEditText);
        messageET = (EditText) findViewById(R.id.messageEditText);
        sendMessage = (Button) findViewById(R.id.sendMessageButton);
        chatTV = (TextView) findViewById(R.id.chatTextView);

        // Make chatroom scrollable
        chatTV.setMovementMethod(new ScrollingMovementMethod());

        // Clear chat
        chatTV.setText("");

        // Attach Database
        database = FirebaseDatabase.getInstance();
        root = database.getReference().child("UnipiChatRoom");

        // Handling logic for sending a message
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // grab current hh:mm when user presses the button
                String timeStamp = new SimpleDateFormat("HH:mm").format(new Date());

                // Get user's input
                usernameDto = userET.getText().toString();
                messageDto = messageET.getText().toString();

                // Reset Message Text
                messageET.setText("");

                Map<String, Object> map = new HashMap<String, Object>();

                // get random key
                randomKey = root.push().getKey();

                // Append DB with random key
                root.updateChildren(map);

                // pivot inside random key
                DatabaseReference newRow = root.child(randomKey);

                // start building new row
                Map<String, Object> mapData = new HashMap<>();
                mapData.put("Time", timeStamp);
                mapData.put("Name", usernameDto);
                mapData.put("Message", messageDto);

                // Save row to DB
                newRow.updateChildren(mapData);

            }
        });


        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                // Gets called when first launching chat
                updateChat(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                // Gets called for each new entry
                updateChat(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

            private void updateChat(DataSnapshot dataSnapshot) {

                // Stores values for each iteration
                String usernameTemp, msgTemp, timeTemp;
                Iterator iterator = dataSnapshot.getChildren().iterator();

                // Append Chat TextView while there are entries
                while(iterator.hasNext()) {

                    msgTemp = (String) ((DataSnapshot) iterator.next()).getValue();
                    usernameTemp = (String) ((DataSnapshot) iterator.next()).getValue();
                    timeTemp = (String) ((DataSnapshot) iterator.next()).getValue();

                    String entry = String.format("%s %s: %s\n", timeTemp, usernameTemp, msgTemp);
                    chatTV.append(entry);
                }
            }
        });
    }
}
