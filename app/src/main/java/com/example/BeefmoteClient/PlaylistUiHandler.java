package com.example.BeefmoteClient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlaylistUiHandler extends Handler {
    private PlaylistActivity playlistActivity;
    private ArrayList<Track> trackList;
    private RecyclerView playlistRecycler;
    private PlaylistRecyclerViewAdapter playlistAdapter;

    PlaylistUiHandler(PlaylistActivity playlistActivity) {
        this.playlistActivity = playlistActivity;
        trackList = new ArrayList<>();
    }

    public PlaylistRecyclerViewAdapter getPlaylistAdapter() {
        return playlistAdapter;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == BeefmoteServer.MESSAGE_TRACKLIST_READY) {
            Bundle bundle = msg.getData();
            ArrayList<String> buffer = bundle.getStringArrayList(BeefmoteServer.SERVER_DATA);

            for (String str : buffer) {
                Track track = new Track(str);
                trackList.add(track);
            }

            playlistRecycler = playlistActivity.findViewById(R.id.rvPlaylist);
            LinearLayoutManager layoutManager = new LinearLayoutManager(playlistActivity);
            playlistRecycler.setLayoutManager(layoutManager);
            playlistAdapter = new PlaylistRecyclerViewAdapter(playlistActivity, trackList);
            playlistAdapter.setClickListener(playlistActivity);
            playlistRecycler.setAdapter(playlistAdapter);
        }

        if(msg.what == BeefmoteServer.MESSAGE_NOW_PLAYING) {
            Bundle bundle = msg.getData();
            String nowPlayingStr = bundle.getString(BeefmoteServer.SERVER_DATA);

            if (nowPlayingStr == null) {
                return;
            }

            Track nowPlaying = new Track(nowPlayingStr);

            //Toast.makeText(playlistActivity, playlistActivity.getResources().getString(R.string.playing) + " " + nowPlaying.getTitle(), Toast.LENGTH_LONG).show();

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

            ArrayList<Integer> selectedTracks = playlistAdapter.getSelectedTracks();

            if (selectedTracks.isEmpty()) {
                selectedTracks.add(adapterPosition);
            } else {
                int old = selectedTracks.get(0);
                selectedTracks.clear();
                selectedTracks.add(adapterPosition);
                playlistAdapter.notifyItemChanged(old);
            }
        }
    }
}
