package com.example.BeefmoteClient;

import android.os.Bundle;
import android.os.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class BeefmoteServer {
    private Socket socket;
    private BufferedReader bufferedReader;
    private UiHandler uiHandler;
    private String serverIp;
    private int serverPort;
    private ArrayList<String> buffer;

    // Beefmote commands
    private static final String BEEFMOTE_TRACKLIST = "tla";
    private static final String BEEFMOTE_PLAYTRACK = "pa";

    // Beefmote messages (for communication with the UiHandler)
    public static final int MESSAGE_TRACKLIST_READY = 0;
    public static final int MESSAGE_PLAYLISTS_READY = 1;
    public static final int MESSAGE_CURRENT_PLAYLIST_READY = 2;
    public static final int MESSAGE_SEARCH_READY = 3;

    // Dummy string for storing/extracting data from a Bundle (UiHandler communication stuff)
    public static final String SERVER_DATA = "SERVER_DATA";

    BeefmoteServer(String serverIp, int serverPort, UiHandler uiHandler) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.uiHandler = uiHandler;
        buffer = new ArrayList<>();
    }

    public void connect() {
        new Thread() {
            public void run() {
                try {
                    InetAddress serverAddress = InetAddress.getByName(serverIp);
                    socket = new Socket(serverAddress, serverPort);
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    public void disconnect() {
        new Thread() {
            public void run() {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void getTracklist() {
        new Thread() {
            public void run() {
                // Send tracklist command to Beefmote
                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    out.println(BEEFMOTE_TRACKLIST);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                // ... and get results
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

                    if (inputLine.equals("TRACKLIST_BEGIN")) {
                        receivingTracklist = true;
                        continue;
                    }

                    if (inputLine.equals("TRACKLIST_END")) {
                        receivingTracklist = false;

                        // Send buffer and clear it
                        Message msg = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList(SERVER_DATA, buffer);
                        msg.setData(bundle);
                        msg.what = MESSAGE_TRACKLIST_READY;
                        uiHandler.sendMessage(msg);

                        // Reset buffer
                        buffer = new ArrayList<>();

                        break;
                    }

                    if (receivingTracklist) {
                        buffer.add(inputLine);
                    }
                }
            }
        }.start();
    }

    public void playTrack(final Track track) {
        new Thread() {
            public void run() {
                // Send playTrack command to Beefmote
                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    out.println(BEEFMOTE_PLAYTRACK + " " + track.getTrackAddr());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }.start();
    }
}
