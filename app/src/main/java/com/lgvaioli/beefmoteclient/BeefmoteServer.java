package com.lgvaioli.beefmoteclient;

import android.os.Bundle;
import android.os.Handler;
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

// FIXME REIMPLEMENT THIS USING THE ACTOR PATTERN
public class BeefmoteServer {
    private Socket socket;
    private BufferedReader bufferedReader;
    private String serverIp;
    private int serverPort;
    private boolean stopAfterCurrent;
    private boolean notifyNowPlaying;
    private Thread nowPlayingThread;
    private final int TRACKLIST_BATCH_SIZE = 100;

    // Beefmote commands
    private static final String BEEFMOTE_TRACKLIST = "tla";
    private static final String BEEFMOTE_PLAY = "pp";
    private static final String BEEFMOTE_PLAYTRACK = "pa";
    private static final String BEEFMOTE_RANDOM = "r";
    private static final String BEEFMOTE_PLAY_RESUME = "p";
    private static final String BEEFMOTE_STOP_AFTER_CURRENT = "sac";
    private static final String BEEFMOTE_STOP = "s";
    private static final String BEEFMOTE_PREVIOUS = "pv";
    private static final String BEEFMOTE_NEXT = "nt";
    private static final String BEEFMOTE_VOLUME_UP = "vu";
    private static final String BEEFMOTE_VOLUME_DOWN = "vd";
    private static final String BEEFMOTE_SEEK_FORWARD = "sf";
    private static final String BEEFMOTE_SEEK_BACKWARD = "sb";
    private static final String BEEFMOTE_NOTIFY_NOW_PLAYING = "ntfy-nowplaying";
    private static final String BEEFMOTE_EXIT = "exit";

    // Beefmote messages (for communication with the PlaylistUiHandler)
    static final int MESSAGE_TRACKLIST_BATCH_READY = 0;
    static final int MESSAGE_PLAYLISTS_READY = 1;
    static final int MESSAGE_CURRENT_PLAYLIST_READY = 2;
    static final int MESSAGE_SEARCH_READY = 3;
    static final int MESSAGE_NOW_PLAYING = 4;

    // Dummy strings for storing/extracting data from a Bundle (PlaylistUiHandler communication stuff)
    static final String TRACKLIST_DATA = "TRACKLIST_DATA";
    static final String NOW_PLAYING_DATA = "NOW_PLAYING_DATA";

    BeefmoteServer(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        stopAfterCurrent = false;
        notifyNowPlaying = false;
        nowPlayingThread = null;
    }

    static public int nowPlayingStrToInt(String nowPlaying) {
        return Integer.parseInt(nowPlaying.split(" ")[1]);
    }

    // Helper function for sending Beefmote commands (cuts down the Thread'ing boilerplate)
    private void sendCommand(final String beefmoteCommand) {
        new Thread() {
            public void run() {
                // Send playTrack command to Beefmote
                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    out.println(beefmoteCommand);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }.start();
    }

    public boolean getStopAfterCurrent() {
        return stopAfterCurrent;
    }

    public void setStopAfterCurrent(boolean stopAfterCurrent) {
        this.stopAfterCurrent = stopAfterCurrent;
        sendCommand(BEEFMOTE_STOP_AFTER_CURRENT);
    }

    // Connects to the Beefmote server. This function is blocking, i.e. it won't return
    // until the connection is established. Returns true if connection was established,
    // false otherwise.
    boolean connect() {
        Thread thread = new Thread() {
            public void run() {
                try {
                    InetAddress serverAddress = InetAddress.getByName(serverIp);
                    socket = new Socket(serverAddress, serverPort);
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                    if (socket != null) {
                        try {
                            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            }
        };

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return socket != null;
    }

    void disconnect() {
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

    // Gets tracklist.
    void getTracklist(final Handler uiHandler) {
        Thread thread = new Thread() {
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
                ArrayList<String> trackBuffer = new ArrayList<>(TRACKLIST_BATCH_SIZE);

                while(true) {
                    try {
                        if ((inputLine = bufferedReader.readLine()) == null) {
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (inputLine.startsWith("[BEEFMOTE_TRACKLIST_BEGIN]")) {
                        // Send buffer and reset it
                        trackBuffer.add(inputLine);
                        Message msg = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList(TRACKLIST_DATA, trackBuffer);
                        msg.setData(bundle);
                        msg.what = MESSAGE_TRACKLIST_BATCH_READY;
                        uiHandler.sendMessage(msg);
                        trackBuffer = new ArrayList<>();

                        continue;
                    }

                    if (inputLine.equals("[BEEFMOTE_TRACKLIST_END]")) {
                        if (trackBuffer.size() > 0) {
                            // Send buffer
                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putStringArrayList(TRACKLIST_DATA, trackBuffer);
                            msg.setData(bundle);
                            msg.what = MESSAGE_TRACKLIST_BATCH_READY;
                            uiHandler.sendMessage(msg);

                            // Send the end message itself
                            trackBuffer = new ArrayList<>();
                            trackBuffer.add(inputLine);
                            msg = new Message();
                            bundle = new Bundle();
                            bundle.putStringArrayList(TRACKLIST_DATA, trackBuffer);
                            msg.setData(bundle);
                            msg.what = MESSAGE_TRACKLIST_BATCH_READY;
                            uiHandler.sendMessage(msg);

                            break;
                        }
                        else {
                            // Send the end message itself
                            trackBuffer = new ArrayList<>();
                            trackBuffer.add(inputLine);
                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putStringArrayList(TRACKLIST_DATA, trackBuffer);
                            msg.setData(bundle);
                            msg.what = MESSAGE_TRACKLIST_BATCH_READY;
                            uiHandler.sendMessage(msg);
                        }
                    }

                    if (inputLine.startsWith("[BEEFMOTE_TRACKLIST_TRACK]")) {
                        trackBuffer.add(inputLine.replace("[BEEFMOTE_TRACKLIST_TRACK] ", ""));

                        if (trackBuffer.size() == TRACKLIST_BATCH_SIZE) {
                            // Send buffer and reset it
                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putStringArrayList(TRACKLIST_DATA, trackBuffer);
                            msg.setData(bundle);
                            msg.what = MESSAGE_TRACKLIST_BATCH_READY;
                            uiHandler.sendMessage(msg);
                            trackBuffer = new ArrayList<>();
                        }
                    }
                }
            }
        };

        thread.start();
    }

    void play() {
        sendCommand(BEEFMOTE_PLAY);
    }

    void playTrack(final Track track) {
        sendCommand(BEEFMOTE_PLAYTRACK + " " + track.getAddress());
    }

    void playRandom() {
        sendCommand(BEEFMOTE_RANDOM);
    }

    void playResume() {
        sendCommand(BEEFMOTE_PLAY_RESUME);
    }

    void stop() {
        sendCommand(BEEFMOTE_STOP);
    }

    void previous() {
        sendCommand(BEEFMOTE_PREVIOUS);
    }

    void next() {
        sendCommand(BEEFMOTE_NEXT);
    }

    void volumeUp() {
        sendCommand(BEEFMOTE_VOLUME_UP);
    }

    void volumeDown() {
        sendCommand(BEEFMOTE_VOLUME_DOWN);
    }

    void seekForward() {
        sendCommand(BEEFMOTE_SEEK_FORWARD);
    }

    void seekBackward() {
        sendCommand(BEEFMOTE_SEEK_BACKWARD);
    }

    void setNotifyNowPlaying(final boolean notifyNowPlaying, final Handler uiHandler) {
        this.notifyNowPlaying = notifyNowPlaying;

        if (notifyNowPlaying && nowPlayingThread != null) {
            System.out.println("Notification true and thread running, returning");
            return;
        }

        nowPlayingThread = new Thread() {
            public void run() {
                String boolStr;

                if (notifyNowPlaying) {
                    boolStr = "true";
                }
                else {
                    boolStr = "false";
                }

                // Send notifyNowPlaying command to Beefmote
                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    out.println(BEEFMOTE_NOTIFY_NOW_PLAYING + " " + boolStr);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                if (!notifyNowPlaying) {
                    System.out.println("Returning from Thread's run()");
                    return;
                }

                // ... and get results
                String inputLine = null;

                while(true) {
                    try {
                        if ((inputLine = bufferedReader.readLine()) == null) {
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (inputLine.startsWith("[BEEFMOTE_NOW_PLAYING]")) {
                        // Send buffer and clear it
                        Message msg = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString(NOW_PLAYING_DATA, inputLine.replace("[BEEFMOTE_NOW_PLAYING] ", ""));
                        msg.setData(bundle);
                        msg.what = MESSAGE_NOW_PLAYING;
                        uiHandler.sendMessage(msg);
                    }
                }
            }
        };
        nowPlayingThread.start();

        if (!notifyNowPlaying && nowPlayingThread != null) {
            System.out.println("Notification false and thread running, interrupting thread");
            nowPlayingThread.interrupt();
            nowPlayingThread = null;
        }
    }

    boolean getNotifyNowPlaying() {
        return notifyNowPlaying;
    }

    void exit() {
        sendCommand(BEEFMOTE_EXIT);
    }
}