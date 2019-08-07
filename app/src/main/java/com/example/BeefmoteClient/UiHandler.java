package com.example.BeefmoteClient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;

public class UiHandler extends Handler {
    private MainActivity mainActivity;
    private ArrayList<Track> trackList;

    UiHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        trackList = new ArrayList<>();
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == BeefmoteServer.MESSAGE_TRACKLIST_READY) {
            Bundle bundle = msg.getData();
            ArrayList<String> buffer = bundle.getStringArrayList(BeefmoteServer.SERVER_DATA);

            System.out.println("[HANDLER] BUNDLE_BEGIN");
            for (String str : buffer) {
                Track track = new Track(str);
                trackList.add(track);
                System.out.println("[HANDLER] " + track.toString());
                mainActivity.appendTextToServerOutput(track.toString());
            }
            System.out.println("[HANDLER] BUNDLE_END");
        }
    }
}
