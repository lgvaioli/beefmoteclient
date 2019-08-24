package com.lgvaioli.beefmoteclient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

public abstract class ConnectionListener extends Handler {
    // Convenience message handling. The messy BeefmoteServer details are here.
    // This method is *highly* coupled to BeefmoteServer's internal details.
    @Override
    final public void handleMessage(@NonNull Message msg) {
        if (msg.what == BeefmoteServer.MESSAGE_CONNECTION) {
            Bundle bundle = msg.getData();
            String connectionStr = bundle.getString(BeefmoteServer.CONNECTION_DATA);

            if (connectionStr == null) {
                return;
            }

            if (connectionStr.equals(BeefmoteServer.CONNECTION_START)) {
                onConnectionStart();
                return;
            }

            if (connectionStr.equals(BeefmoteServer.CONNECTION_SUCCESS)) {
                onConnectionSuccess();
                return;
            }

            if (connectionStr.equals(BeefmoteServer.CONNECTION_FAILURE)) {
                onConnectionFailure();
            }
        }
    }

    // Called whenever a connection is started
    abstract void onConnectionStart();

    // Called whenever a connection was successfully established
    abstract void onConnectionSuccess();

    // Called whenever a connection couldn't be established
    abstract void onConnectionFailure();
}
