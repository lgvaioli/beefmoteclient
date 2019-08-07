package com.example.BeefmoteClient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements PlaylistRecyclerViewAdapter.ItemClickListener{
    private BeefmoteServer beefmoteServer;
    private UiHandler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Called when the user taps the Connect button
    public void connect(View view) {
        EditText editText_serverIp = findViewById(R.id.editText_serverIP);
        String serverIpStr = editText_serverIp.getText().toString();
        if (serverIpStr.equals("Server IP")) {
            serverIpStr = "192.168.0.2";
        }

        EditText editText_serverPort = findViewById(R.id.editText_serverPort);
        String serverPortStr = editText_serverPort.getText().toString();
        int serverPort;
        if (serverPortStr.equals("Server Port")) {
            serverPort = 49160;
        }
        else {
            serverPort = Integer.parseInt(serverPortStr);
        }

        uiHandler = new UiHandler(this);
        beefmoteServer = new BeefmoteServer(serverIpStr, serverPort, uiHandler);
        beefmoteServer.connect();
    }

    // Called when the user taps the Disconnect button
    public void disconnect(View view) {
        beefmoteServer.disconnect();
    }

    // Called when the user taps the Send button
    public void send(View view) {
        EditText editText = findViewById(R.id.editText_userCommand);
        String message = editText.getText().toString();

        if (message.equals("tla")) {

            beefmoteServer.getTracklist();
        }
    }

    // Called when the user clicks a track
    @Override
    public void onItemClick(View view, int position) {
        /*Toast.makeText(this, "You clicked " +
                        uiHandler.getPlaylistAdapter().getItem(position) +
                        " on row number " +
                        position, Toast.LENGTH_SHORT).show();*/
        Track track = uiHandler.getPlaylistAdapter().getItem(position);
        beefmoteServer.playTrack(track);
        Toast.makeText(this, "Playing " + track.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
