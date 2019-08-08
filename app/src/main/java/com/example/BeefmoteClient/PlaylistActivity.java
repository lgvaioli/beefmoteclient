package com.example.BeefmoteClient;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PlaylistActivity extends AppCompatActivity implements PlaylistRecyclerViewAdapter.ItemClickListener {
    private BeefmoteServer beefmoteServer;
    private static final String SERVER_DEFAULT_IP = "192.168.0.2";
    private static final int SERVER_DEFAULT_PORT = 49160;
    private PlaylistUiHandler playlistUiHandler;
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch(menuItem.getItemId()) {
                        case R.id.nav_stop:
                            beefmoteServer.stop();
                            break;

                        case R.id.nav_play:
                            beefmoteServer.play();
                            break;

                        case R.id.nav_pause:
                            beefmoteServer.playResume();
                            break;

                        case R.id.nav_previous:
                            beefmoteServer.previous();
                            break;

                        case R.id.nav_next:
                            beefmoteServer.next();
                            break;
                    }

                    return false; // all menu items unchecked
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        // Make all navigation buttons uncheckable
        BottomNavigationView nav = findViewById(R.id.navigation_view);
        nav.getMenu().setGroupCheckable(0, false, true);
        nav.setOnNavigationItemSelectedListener(navListener);

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