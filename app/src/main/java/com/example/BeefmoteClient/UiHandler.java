package com.example.BeefmoteClient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UiHandler extends Handler {
    private MainActivity mainActivity;
    private ArrayList<Track> trackList;
    private PlaylistRecyclerViewAdapter playlistAdapter;

    UiHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
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

            // FIXME: when this works, move it somewhere else
            RecyclerView recyclerView = mainActivity.findViewById(R.id.rvPlaylist);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mainActivity);
            recyclerView.setLayoutManager(layoutManager);
            playlistAdapter = new PlaylistRecyclerViewAdapter(mainActivity, trackList);
            playlistAdapter.setClickListener(mainActivity);
            recyclerView.setAdapter(playlistAdapter);

            // add a divider between the rows
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    layoutManager.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);
        }
    }
}
