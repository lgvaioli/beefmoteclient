package com.lgvaioli.beefmoteclient;

public interface TracklistEmitter {
    void addTracklistListener(TracklistListener listener);
    void removeTracklistListener(TracklistListener listener);
    void notifyTracklistListeners();
}
