package com.example.BeefmoteClient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private BeefmoteServer beefmoteServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Make text view scrollable.
        TextView serverOutput = findViewById(R.id.textView_serverOutput);
        serverOutput.setMovementMethod(new ScrollingMovementMethod());
    }

    public void appendTextToServerOutput(String text) {
        TextView serverOutput = findViewById(R.id.textView_serverOutput);
        serverOutput.append("\n" + text);
    }

    // Called when the user taps the Connect button
    public void connect(View view) {
        EditText editText_serverIp = findViewById(R.id.editText_serverIP);
        String serverIpStr = editText_serverIp.getText().toString();
        if (serverIpStr.equals("Server IP")) {
            serverIpStr = "192.168.0.2";
        }

        EditText editText_serverPort = findViewById(R.id.editText_serverPort);
        String serverPortStr = editText_serverPort.getText().toString();
        int serverPort;
        if (serverPortStr.equals("Server Port")) {
            serverPort = 49160;
        }
        else {
            serverPort = Integer.parseInt(serverPortStr);
        }

        beefmoteServer = new BeefmoteServer(serverIpStr, serverPort, new UiHandler(this));
        beefmoteServer.connect();
    }

    // Called when the user taps the Disconnect button
    public void disconnect(View view) {
        beefmoteServer.disconnect();
    }

    // Called when the user taps the Send button
    public void send(View view) {
        EditText editText = findViewById(R.id.editText_userCommand);
        String message = editText.getText().toString();

        if (message.equals("tla")) {
            beefmoteServer.getTracklist();
        }
    }
}
