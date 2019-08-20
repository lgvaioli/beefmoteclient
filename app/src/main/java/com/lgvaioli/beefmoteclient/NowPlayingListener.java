package com.lgvaioli.beefmoteclient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

public abstract class NowPlayingListener extends Handler {

    // Convenience message handling. The messy BeefmoteServer details are here.
    // This method is *highly* coupled to BeefmoteServer's internal details.
    @Override
    final public void handleMessage(@NonNull Message msg) {
        if (msg.what == BeefmoteServer.MESSAGE_NOW_PLAYING) {
            Bundle bundle = msg.getData();
            String nowPlayingStr = bundle.getString(BeefmoteServer.NOW_PLAYING_DATA);

            if (nowPlayingStr == null) {
                return;
            }

            onNowPlaying(new Track(nowPlayingStr));
        }
    }

    // Called whenever we get a new "now playing" Track from the server.
    abstract void onNowPlaying(Track nowPlaying);
}
