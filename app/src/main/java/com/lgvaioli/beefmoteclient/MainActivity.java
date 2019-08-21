package com.lgvaioli.beefmoteclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String SERVER_DEFAULT_IP = "192.168.0.2";
    private static final int SERVER_DEFAULT_PORT = 49160;

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

        if (serverIpStr.equals(getResources().getString(R.string.serverIP))) {
            serverIpStr = SERVER_DEFAULT_IP;
        }

        int serverPort;
        if (serverPortStr.equals(getResources().getString(R.string.serverPort))) {
            serverPort = SERVER_DEFAULT_PORT;
        }
        else {
            serverPort = Integer.parseInt(serverPortStr);
        }

        if (!BeefmoteServer.get().connect(serverIpStr, serverPort)) {
            Toast.makeText(this, getResources().getString(R.string.serverCouldNotConnect),
                    Toast.LENGTH_LONG).show();
            return;
        }

        startActivity(intent);
    }
}
