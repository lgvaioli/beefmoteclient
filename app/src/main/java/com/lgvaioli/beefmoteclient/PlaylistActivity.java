package com.lgvaioli.beefmoteclient;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity implements PlaylistRecyclerViewAdapter.ItemClickListener {
    private ArrayList<Track> tracklist;
    private RecyclerView playlistRecycler;
    private PlaylistRecyclerViewAdapter playlistAdapter;
    private int tracklistNum = -1;
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch(menuItem.getItemId()) {
                        case R.id.nav_stop:
                            BeefmoteServer.get().stop();
                            break;

                        case R.id.nav_play:
                            BeefmoteServer.get().play();
                            break;

                        case R.id.nav_pause:
                            BeefmoteServer.get().playResume();
                            break;

                        case R.id.nav_previous:
                            BeefmoteServer.get().previous();
                            break;

                        case R.id.nav_next:
                            BeefmoteServer.get().next();
                            break;
                    }

                    return false; // all menu items unchecked
                }
            };

      ////////////////////////////////////////////////////////////////////
     // NESTED CLASS. Implements the TracklistListener abstract class. //
    ////////////////////////////////////////////////////////////////////
    static private class PlaylistTracklistListener extends TracklistListener {
        private final WeakReference<PlaylistActivity> playlistActivityWeakReference;

        PlaylistTracklistListener(PlaylistActivity playlistActivity) {
            playlistActivityWeakReference = new WeakReference<>(playlistActivity);
        }

        @Override
        void onPartialTracklist(ArrayList<Track> partialTracklist, int trackNum, boolean end) {
            PlaylistActivity activity = playlistActivityWeakReference.get();

            if (activity == null) {
                return;
            }

            // First call: trackNum != -1
            if (trackNum != -1) {
                activity.setTracklistNum(trackNum);
                return;
            }

            // Last call: end == true
            if (end) {
                // FIXME: hack. Fix BeefmoteServer's race conditions or whatever.
                BeefmoteServer.get().setNotifyNowPlaying(true);
                activity.setTracklistNum(-1);

                // Hide progress bar
                ProgressBar progressBar = activity.findViewById(R.id.playlistProgressBar);
                if (progressBar != null) {
                    progressBar.setVisibility(View.INVISIBLE);
                }

                return;
            }

            activity.getTracklist().addAll(partialTracklist);
            activity.getPlaylistAdapter().notifyDataSetChanged();

            // Update progress bar
            ProgressBar progressBar = activity.findViewById(R.id.playlistProgressBar);
            if (progressBar != null) {
                double percent = ((double) activity.getTracklist().size() / (double) activity.getTracklistNum()) * 100;
                progressBar.setProgress((int) percent);
            }
        }
      }

      /////////////////////////////////////////////////////////////////////
     // NESTED CLASS. Implements the NowPlayingListener abstract class. //
    /////////////////////////////////////////////////////////////////////
    static private class PlaylistNowPlayingListener extends NowPlayingListener {
        private final WeakReference<PlaylistActivity> playlistActivityWeakReference;

        PlaylistNowPlayingListener(PlaylistActivity playlistActivity) {
            playlistActivityWeakReference = new WeakReference<>(playlistActivity);
        }

        @Override
        void onNowPlaying(Track nowPlaying) {
            PlaylistActivity activity = playlistActivityWeakReference.get();

            if (activity == null) {
                return;
            }

            RecyclerView playlistRecycler = activity.getPlaylistRecycler();
            PlaylistRecyclerViewAdapter playlistAdapter = activity.getPlaylistAdapter();

            // We *have* to use the Adapter's getTrackPosition method instead of simply using
            // the Track's getPlaylistIndex method because in a filtered tracklist those two
            // indexes will *not* match.
            int adapterPosition = playlistAdapter.getTrackPosition(nowPlaying);
            PlaylistRecyclerViewAdapter.ViewHolder holder =
                    (PlaylistRecyclerViewAdapter.ViewHolder)
                            playlistRecycler.findViewHolderForAdapterPosition(adapterPosition);

            if (holder != null) {
                playlistAdapter.highlightHolder(holder);
            }
            else {
                LinearLayoutManager layoutManager = (LinearLayoutManager) playlistRecycler.getLayoutManager();

                if (layoutManager != null) {
                    layoutManager.scrollToPositionWithOffset(adapterPosition, 0);
                }
            }

            playlistAdapter.setCurrentTrack(nowPlaying);
            playlistAdapter.setCurrentTrackPosition(adapterPosition);
            playlistAdapter.notifyItemChanged(adapterPosition);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        // If server isn't connected, show toast message and return
        if (!BeefmoteServer.get().isConnected()) {
            Toast.makeText(this, getResources().getString(R.string.serverCouldNotConnect),
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Make all navigation buttons uncheckable
        BottomNavigationView nav = findViewById(R.id.navigation_view);
        nav.getMenu().setGroupCheckable(0, false, true);
        nav.setOnNavigationItemSelectedListener(navListener);

        // Create playlist RecyclerView et al
        tracklist = new ArrayList<>();
        playlistRecycler = findViewById(R.id.rvPlaylist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        playlistRecycler.setLayoutManager(layoutManager);
        playlistAdapter = new PlaylistRecyclerViewAdapter(this, playlistRecycler, tracklist);
        playlistAdapter.setClickListener(this);
        playlistRecycler.setAdapter(playlistAdapter);

        // Add BeefmoteServer listeners and get tracklist
        BeefmoteServer.get().addTracklistListener(new PlaylistTracklistListener(this));
        BeefmoteServer.get().addNowPlayingListener(new PlaylistNowPlayingListener(this));
        BeefmoteServer.get().getTracklist();
    }

    // Called when the user clicks a track
    @Override
    public void onItemClick(View view, int position) {
        Track track = playlistAdapter.getItem(position);
        BeefmoteServer.get().playTrack(track);
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
                playlistAdapter.getFilter().filter(s);
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
                    BeefmoteServer.get().volumeUp();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    BeefmoteServer.get().volumeDown();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    // Handle context menu selection
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == PlaylistRecyclerViewAdapter.CONTEXT_MENU_ADD_TO_PLAYBACKQUEUE) {
            Track track = playlistAdapter.getItem(item.getGroupId());
            BeefmoteServer.get().addToPlaybackQueue(track);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    // The following private methods are meant to be used by the private static listeners.
    private void setTracklistNum(int tracklistNum) {
        this.tracklistNum = tracklistNum;
    }

    private int getTracklistNum() {
        return tracklistNum;
    }

    private ArrayList<Track> getTracklist() {
        return tracklist;
    }

    private PlaylistRecyclerViewAdapter getPlaylistAdapter() {
        return playlistAdapter;
    }

    private RecyclerView getPlaylistRecycler() {
        return playlistRecycler;
    }
}
