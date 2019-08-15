package com.lgvaioli.beefmoteclient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlaylistUiHandler extends Handler {
    private PlaylistActivity playlistActivity;
    private ArrayList<Track> trackList;
    private RecyclerView playlistRecycler;
    private PlaylistRecyclerViewAdapter playlistAdapter;
    private int tracklistNum = -1;

    PlaylistUiHandler(PlaylistActivity playlistActivity) {
        this.playlistActivity = playlistActivity;
        trackList = new ArrayList<>();
        playlistRecycler = playlistActivity.findViewById(R.id.rvPlaylist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(playlistActivity);
        playlistRecycler.setLayoutManager(layoutManager);
        playlistAdapter = new PlaylistRecyclerViewAdapter(playlistActivity, playlistRecycler, trackList);
        playlistAdapter.setClickListener(playlistActivity);
        playlistRecycler.setAdapter(playlistAdapter);
    }

    PlaylistRecyclerViewAdapter getPlaylistAdapter() {
        return playlistAdapter;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        if (msg.what == BeefmoteServer.MESSAGE_TRACKLIST_BATCH_READY) {
            Bundle bundle = msg.getData();
            ArrayList<String> buffer = bundle.getStringArrayList(BeefmoteServer.TRACKLIST_DATA);

            if (buffer == null) {
                return;
            }

            // FIXME hardcoded string; this method shouldn't know about these details
            if (buffer.get(0).startsWith("[BEEFMOTE_TRACKLIST_BEGIN]")) {
                String trackNumStr = buffer.get(0).split(" ", 0)[1];
                tracklistNum = Integer.parseInt(trackNumStr);

                //System.out.println("Receiving " + trackNum + " tracks");
                //Toast.makeText(playlistActivity, "Receiving " + trackNumStr + " tracks", Toast.LENGTH_LONG).show();

                return;
            }

            // FIXME ugly hack; the BeefmoteServer class should be reimplemented using the Actor pattern
            if (buffer.get(0).startsWith("[BEEFMOTE_TRACKLIST_END]")) {
                playlistActivity.getBeefmoteServer().setNotifyNowPlaying(true, this);
                //Toast.makeText(playlistActivity, "Now Playing = TRUE", Toast.LENGTH_LONG).show();
                tracklistNum = -1;

                // Hide progress bar
                ProgressBar progressBar = playlistActivity.findViewById(R.id.playlistProgressBar);
                if (progressBar != null) {
                    progressBar.setVisibility(View.INVISIBLE);
                }

                return;
            }

            for (String str : buffer) {
                Track track = new Track(str);
                trackList.add(track);
            }

            playlistAdapter.notifyDataSetChanged();

            // Update progress bar
            ProgressBar progressBar = playlistActivity.findViewById(R.id.playlistProgressBar);
            if (progressBar != null) {
                double percent = ((double) trackList.size() / (double) tracklistNum) * 100;
                progressBar.setProgress((int) percent);
            }

            //Toast.makeText(playlistActivity, "%" + (int) percent, Toast.LENGTH_SHORT).show();
        }

        if(msg.what == BeefmoteServer.MESSAGE_NOW_PLAYING) {
            Bundle bundle = msg.getData();
            String nowPlayingStr = bundle.getString(BeefmoteServer.NOW_PLAYING_DATA);

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
}
