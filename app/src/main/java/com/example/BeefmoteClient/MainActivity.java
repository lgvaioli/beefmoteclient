package com.example.BeefmoteClient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public static final String SERVER_IP = "SERVER_IP";
    public static final String SERVER_PORT = "SERVER_PORT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Called when the user taps the Connect button
    public void connect(View view) {
        Intent intent = new Intent(this, PlaylistActivity.class);

        // Get server IP and port from the main layout TextViews
        EditText editText_serverIp = findViewById(R.id.serverIp);
        String serverIpStr = editText_serverIp.getText().toString();

        EditText editText_serverPort = findViewById(R.id.serverPort);
        String serverPortStr = editText_serverPort.getText().toString();

        // Pack server IP and port strings into the Intent
        intent.putExtra(SERVER_IP, serverIpStr);
        intent.putExtra(SERVER_PORT, serverPortStr);

        startActivity(intent);
    }
}
