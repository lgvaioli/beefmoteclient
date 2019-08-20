package com.lgvaioli.beefmoteclient;

public interface NowPlayingEmitter {
    void addNowPlayingListener(NowPlayingListener listener);
    void removeNowPlayingListener(NowPlayingListener listener);
    void notifyNowPlayingListeners();
}
