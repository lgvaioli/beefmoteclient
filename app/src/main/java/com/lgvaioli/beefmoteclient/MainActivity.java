package com.lgvaioli.beefmoteclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    private static final String SERVER_DEFAULT_IP = "192.168.0.2";
    private static final int SERVER_DEFAULT_PORT = 49160;
    private static Resources resources;

      /////////////////////////////////////////////////////////////////////
     // NESTED CLASS. Implements the ConnectionListener abstract class. //
    /////////////////////////////////////////////////////////////////////
    static private class MyConnectionListener extends ConnectionListener {
        private final WeakReference<MainActivity> activityWeak;

        MyConnectionListener(MainActivity mainActivity) {
            activityWeak = new WeakReference<>(mainActivity);
        }

          @Override
          void onConnectionStart() {
            MainActivity activity = activityWeak.get();

            if (activity == null) {
                return;
            }

            ProgressBar progressBar = activity.findViewById(R.id.mainActivityProgressbar);
            progressBar.setVisibility(ProgressBar.VISIBLE);
          }

          @Override
          void onConnectionSuccess() {
            MainActivity activity = activityWeak.get();

            if (activity == null) {
                return;
            }

            Intent intent = new Intent(activity, PlaylistActivity.class);
            activity.startActivity(intent);

            ProgressBar progressBar = activity.findViewById(R.id.mainActivityProgressbar);
            progressBar.setVisibility(ProgressBar.INVISIBLE);
          }

          @Override
          void onConnectionFailure() {
            MainActivity activity = activityWeak.get();

            if (activity == null) {
                return;
            }

            Toast.makeText(activity, resources.getString(R.string.serverCouldNotConnect), Toast.LENGTH_LONG).show();

            ProgressBar progressBar = activity.findViewById(R.id.mainActivityProgressbar);
            progressBar.setVisibility(ProgressBar.INVISIBLE);
          }
      }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resources = getResources();
    }

    // Called when the user taps the Connect button
    public void connect(View view) {
        // Get server IP and port from the main layout TextViews
        EditText editText_serverIp = findViewById(R.id.serverIp);
        String serverIpStr = editText_serverIp.getText().toString();

        EditText editText_serverPort = findViewById(R.id.serverPort);
        String serverPortStr = editText_serverPort.getText().toString();

        if (serverIpStr.equals(getResources().getString(R.string.serverIP))) {
            serverIpStr = SERVER_DEFAULT_IP;
        }

        int serverPort;
        if (serverPortStr.equals(getResources().getString(R.string.serverPort))) {
            serverPort = SERVER_DEFAULT_PORT;
        }
        else {
            serverPort = Integer.parseInt(serverPortStr);
        }

        BeefmoteServer.get().addConnectionListener(new MyConnectionListener(this));
        BeefmoteServer.get().connect(serverIpStr, serverPort);
    }
}
