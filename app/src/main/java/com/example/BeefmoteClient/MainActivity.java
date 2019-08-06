package com.example.BeefmoteClient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Socket socket = null;
    private static final int NEW_DATA_FROM_SERVER = 100;
    private static final String NEW_DATA_FROM_SERVER_STR = "NEW_DATA";
    private static final int TRACKS_FROM_SERVER = 101;
    private static final String TRACKS_FROM_SERVER_STR = "TRACKS_FROM_SERVER";
    private static UiHandler uiHandler = null;

      ////////////////////////////////////////////////////////////
     // NESTED CLASSES FOR HANDLING UI UPDATING AND NETWORKING //
    ////////////////////////////////////////////////////////////

      //////////////////////////////////
     // Handler for updating the UI. //
    //////////////////////////////////
    static class UiHandler extends Handler {
        private final WeakReference<MainActivity> mainActivity;

        UiHandler(MainActivity mainAct) {
            this.mainActivity = new WeakReference<>(mainAct);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainAct = mainActivity.get();

            if(mainAct != null) {
                // Got data from server; append to server output TextView
                if(msg.what == NEW_DATA_FROM_SERVER) {
                    Bundle bundle = msg.getData();
                    String str = bundle.getString(NEW_DATA_FROM_SERVER_STR);
                    mainAct.appendTextToServerOutput(str);
                }

                if (msg.what == TRACKS_FROM_SERVER) {
                    Bundle bundle = msg.getData();
                    ArrayList<String> buffer = bundle.getStringArrayList(TRACKS_FROM_SERVER_STR);

                    System.out.println("[HANDLER] BUNDLE_BEGIN");
                    for (String str : buffer) {
                        System.out.println("[HANDLER] " + str);
                        mainAct.appendTextToServerOutput(str);
                    }
                    System.out.println("[HANDLER] BUNDLE_END");
                }
            }
        }
    }

      ////////////////////////////////////////
     // Thread class for managing network. //
    ////////////////////////////////////////
    class NetworkThread implements Runnable {
        private String serverIp = "192.168.0.2";
        private int serverPort = 49160;
        private final int BUFSIZE = 100;
        private ArrayList<String> buffer = new ArrayList<>(BUFSIZE);

        NetworkThread(String serverIp, String serverPort) {
            // FIXME: don't hardcode the default values
            if(!serverIp.equals("Server IP")) {
                this.serverIp = serverIp;
            }

            if(!serverPort.equals("Server Port")) {
                this.serverPort = Integer.parseInt(serverPort);
            }
        }

        @Override
        public void run() {
            // Connect socket to server
            try {
                InetAddress serverAddress = InetAddress.getByName(serverIp);
                socket = new Socket(serverAddress, serverPort);
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // Blocking listen()
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String inputLine = null;

            boolean receivingTracklist = false;

            while(true) {
                try {
                    if ((inputLine = bufferedReader.readLine()) == null) {
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Send message to the UI handler.
                if (uiHandler != null) {
                    if (inputLine.equals("TRACKLIST_BEGIN")) {
                        receivingTracklist = true;
                        continue;
                    }

                    if (inputLine.equals("TRACKLIST_END")) {
                        receivingTracklist = false;

                        // Send buffer and clear it
                        if (buffer.size() > 0) {
                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putStringArrayList(TRACKS_FROM_SERVER_STR, buffer);
                            msg.setData(bundle);
                            msg.what = TRACKS_FROM_SERVER;
                            uiHandler.sendMessage(msg);

                            // Reset buffer
                            buffer = new ArrayList<>(BUFSIZE);
                        }

                        continue;
                    }

                    if (receivingTracklist) {
                        buffer.add(inputLine);

                        if (buffer.size() == BUFSIZE) {
                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putStringArrayList(TRACKS_FROM_SERVER_STR, buffer);
                            msg.setData(bundle);
                            msg.what = TRACKS_FROM_SERVER;
                            uiHandler.sendMessage(msg);

                            // Reset buffer
                            buffer = new ArrayList<>(BUFSIZE);
                        }
                    }
                    else {

                        Message msg = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString(NEW_DATA_FROM_SERVER_STR, inputLine);
                        msg.setData(bundle);
                        msg.what = NEW_DATA_FROM_SERVER;
                        uiHandler.sendMessage(msg);
                    }
                }
            }
        }
    }

      ///////////////////////////
     // END OF NESTED CLASSES //
    ///////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Make text view scrollable.
        TextView serverOutput = findViewById(R.id.textView_serverOutput);
        serverOutput.setMovementMethod(new ScrollingMovementMethod());
    }

    private void appendTextToServerOutput(String text) {
        TextView serverOutput = findViewById(R.id.textView_serverOutput);
        serverOutput.append("\n" + text);
    }

    // Called when the user taps the Connect button
    public void connect(View view) {
        EditText editText_serverIp = findViewById(R.id.editText_serverIP);
        String serverIpStr = editText_serverIp.getText().toString();

        EditText editText_serverPort = findViewById(R.id.editText_serverPort);
        String serverPortStr = editText_serverPort.getText().toString();

        uiHandler = new UiHandler(this);
        new Thread(new NetworkThread(serverIpStr, serverPortStr)).start();
    }

    // Called when the user taps the Disconnect button
    public void disconnect(View view) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Called when the user taps the Send button
    public void send(View view) {
        if (socket == null) {
            return;
        }

        // We have to send the message in another thread. Remember, *every* networking operation
        // in Android *must* be implemented in another thread different from the main one.
        new Thread() {
            public void run() {
                EditText editText = findViewById(R.id.editText_userCommand);
                String message = editText.getText().toString();

                PrintWriter out = null;
                try {
                    out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                if(out != null) {
                    out.println(message);
                }
            }
        }.start();
    }
}
