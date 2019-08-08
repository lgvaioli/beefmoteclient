package com.example.BeefmoteClient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PlaylistActivity extends AppCompatActivity implements PlaylistRecyclerViewAdapter.ItemClickListener {
    private BeefmoteServer beefmoteServer;
    private static final String SERVER_DEFAULT_IP = "192.168.0.2";
    private static final int SERVER_DEFAULT_PORT = 49160;
    private PlaylistUiHandler playlistUiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        playlistUiHandler = new PlaylistUiHandler(this);

        Intent intent = getIntent();
        String serverIpStr = intent.getStringExtra(MainActivity.SERVER_IP);
        String serverPortStr = intent.getStringExtra(MainActivity.SERVER_PORT);

        if (serverIpStr.equals("Server IP")) {
            serverIpStr = SERVER_DEFAULT_IP;
        }

        int serverPort;
        if (serverPortStr.equals("Server Port")) {
            serverPort = SERVER_DEFAULT_PORT;
        }
        else {
            serverPort = Integer.parseInt(serverPortStr);
        }

        // Create server and get track list
        beefmoteServer = new BeefmoteServer(serverIpStr, serverPort);
        beefmoteServer.connect();
        beefmoteServer.getTracklist(playlistUiHandler);
    }

    // Called when the user clicks a track
    @Override
    public void onItemClick(View view, int position) {
        /*Toast.makeText(this, "You clicked " +
                        uiHandler.getPlaylistAdapter().getItem(position) +
                        " on row number " +
                        position, Toast.LENGTH_SHORT).show();*/
        Track track = playlistUiHandler.getPlaylistAdapter().getItem(position);
        beefmoteServer.playTrack(track);
    }
}
