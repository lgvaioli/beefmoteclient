package com.example.BeefmoteClient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlaylistUiHandler extends Handler {
    private PlaylistActivity playlistActivity;
    private ArrayList<Track> trackList;
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

            RecyclerView recyclerView = playlistActivity.findViewById(R.id.rvPlaylist);
            LinearLayoutManager layoutManager = new LinearLayoutManager(playlistActivity);
            recyclerView.setLayoutManager(layoutManager);
            playlistAdapter = new PlaylistRecyclerViewAdapter(playlistActivity, trackList);
            playlistAdapter.setClickListener(playlistActivity);
            recyclerView.setAdapter(playlistAdapter);

            // add a divider between the rows
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    layoutManager.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);
        }

        if(msg.what == BeefmoteServer.MESSAGE_NOW_PLAYING) {
            Bundle bundle = msg.getData();
            String nowPlayingStr = bundle.getString(BeefmoteServer.SERVER_DATA);

            int idx = BeefmoteServer.nowPlayingStrToInt(nowPlayingStr);
            Track currTrack = playlistAdapter.getItem(idx);

            Toast.makeText(playlistActivity, "Playing " + currTrack.getTitle(), Toast.LENGTH_LONG).show();
        }
    }
}
