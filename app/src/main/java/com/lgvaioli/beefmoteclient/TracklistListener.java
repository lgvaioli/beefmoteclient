package com.lgvaioli.beefmoteclient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public abstract class TracklistListener extends Handler {

    // Convenience message handling. The messy BeefmoteServer details are here.
    // This method is *highly* coupled to BeefmoteServer's internal details.
    @Override
    final public void handleMessage(@NonNull Message msg) {
        if (msg.what == BeefmoteServer.MESSAGE_TRACKLIST_BATCH_READY) {
            Bundle bundle = msg.getData();
            ArrayList<String> buffer = bundle.getStringArrayList(BeefmoteServer.TRACKLIST_DATA);

            if (buffer == null) {
                return;
            }

            if (buffer.get(0).startsWith(BeefmoteServer.BEEFMOTE_TRACKLIST_BEGIN)) {
                // Beginning of tracklist. It has the format
                // "[BEEFMOTE_TRACKLIST_BEGIN] trackNum". We'll get trackNum here.
                String trackNumStr = buffer.get(0).split(" ", 0)[1];
                int trackNum = Integer.parseInt(trackNumStr);

                // We're done processing BeefmoteServer's stuff. Execute callback
                // with received info.
                onPartialTracklist(null, trackNum, false);
                return;
            }

            if (buffer.get(0).startsWith(BeefmoteServer.BEEFMOTE_TRACKLIST_END)) {
                // End of tracklist. Nothing to process. Execute callback.
                onPartialTracklist(null, -1, true);
                return;
            }

            // Construct tracklist from String array.
            ArrayList<Track> tracklist = new ArrayList<>();
            for (String str : buffer) {
                tracklist.add(new Track(str));
            }

            // Processing done. Execute callback.
            onPartialTracklist(tracklist, -1, false);
        }
    }

    // Called whenever we get a partial track list from the server.
    // First call: partialTracklist is set to null, trackNum is set to the number of tracks
    // being received, end is set to false.
    // Subsequent calls: partialTracklist is set to the received Tracks, trackNum is set to -1,
    // end is set to false.
    // Final call: partialTracklist is set to null, trackNum is set to -1, end is set to true.
    abstract void onPartialTracklist(ArrayList<Track> partialTracklist, int trackNum, boolean end);
}
