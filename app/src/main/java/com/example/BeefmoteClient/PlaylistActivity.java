package com.example.BeefmoteClient;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

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

        // This should never happen
        if (serverIpStr == null || serverPortStr == null) {
            finish();
            return;
        }

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

        // Create server and get track list
        beefmoteServer = new BeefmoteServer(serverIpStr, serverPort);
        if (!beefmoteServer.connect()) {
            Toast.makeText(this, getResources().getString(R.string.serverCouldNotConnect), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //beefmoteServer.setNotifyNowPlaying(true, playlistUiHandler);
        beefmoteServer.getTracklist(playlistUiHandler);
    }

    BeefmoteServer getBeefmoteServer() {
        return beefmoteServer;
    }

    // Called when the user clicks a track
    @Override
    public void onItemClick(View view, int position) {
        Track track = playlistUiHandler.getPlaylistAdapter().getItem(position);
        beefmoteServer.playTrack(track);
    }

    // Setup SearchView
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                playlistUiHandler.getPlaylistAdapter().getFilter().filter(s);
                return false;
            }
        });

        return true;
    }

    // Handle volume keys
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    beefmoteServer.volumeUp();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    beefmoteServer.volumeDown();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
}
