package com.lgvaioli.beefmoteclient;

public interface ConnectionEmitter {
    void addConnectionListener(ConnectionListener listener);
    void removeConnectionListener(ConnectionListener listener);
    void notifyConnectionListeners();
}
