package com.lgvaioli.beefmoteclient;

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
import java.util.ArrayList;

public class BeefmoteServer implements TracklistEmitter, NowPlayingEmitter, ConnectionEmitter {
    private static final BeefmoteServer singleton = new BeefmoteServer();
    private Socket socket;
    private BufferedReader bufferedReader;
    private boolean connected;
//    private boolean stopAfterCurrent;
//    private boolean notifyNowPlaying;
    private Thread nowPlayingThread;
    private final int TRACKLIST_BATCH_SIZE = 100;
    private ArrayList<String> trackBuffer;
    private ArrayList<TracklistListener> tracklistListeners;
    private String nowPlayingStr;
    private ArrayList<NowPlayingListener> nowPlayingListeners;
    private ArrayList<ConnectionListener> connectionListeners;
    private String connectionStr;

    // Beefmote commands
    private static final String BEEFMOTE_TRACKLIST = "tla";
    private static final String BEEFMOTE_PLAY = "pp";
    private static final String BEEFMOTE_PLAYTRACK = "pa";
//    private static final String BEEFMOTE_RANDOM = "r";
    private static final String BEEFMOTE_PLAY_RESUME = "p";
//    private static final String BEEFMOTE_STOP_AFTER_CURRENT = "sac";
    private static final String BEEFMOTE_STOP = "s";
    private static final String BEEFMOTE_PREVIOUS = "pv";
    private static final String BEEFMOTE_NEXT = "nt";
    private static final String BEEFMOTE_VOLUME_UP = "vu";
    private static final String BEEFMOTE_VOLUME_DOWN = "vd";
//    private static final String BEEFMOTE_SEEK_FORWARD = "sf";
//    private static final String BEEFMOTE_SEEK_BACKWARD = "sb";
    private static final String BEEFMOTE_ADD_PLAYBACKQUEUE_ADDRESS = "apa";
    private static final String BEEFMOTE_NOTIFY_NOW_PLAYING = "ntfy-nowplaying";
//    private static final String BEEFMOTE_EXIT = "exit";

    // Beefmote command markers
    static final String BEEFMOTE_TRACKLIST_BEGIN = "[BEEFMOTE_TRACKLIST_BEGIN]";
    static final String BEEFMOTE_TRACKLIST_END = "[BEEFMOTE_TRACKLIST_END]";
    private static final String BEEFMOTE_TRACKLIST_TRACK = "[BEEFMOTE_TRACKLIST_TRACK]";
    private static final String BEEFMOTE_NOW_PLAYING = "[BEEFMOTE_NOW_PLAYING]";


    // Beefmote messages
    static final int MESSAGE_TRACKLIST_BATCH_READY = 0;
//    static final int MESSAGE_PLAYLISTS_READY = 1;
//    static final int MESSAGE_CURRENT_PLAYLIST_READY = 2;
//    static final int MESSAGE_SEARCH_READY = 3;
    static final int MESSAGE_NOW_PLAYING = 4;
    static final int MESSAGE_CONNECTION = 5;

    // Dummy strings for storing/extracting data from a Bundle
    static final String TRACKLIST_DATA = "TRACKLIST_DATA";
    static final String NOW_PLAYING_DATA = "NOW_PLAYING_DATA";
    static final String CONNECTION_DATA = "CONNECTION_DATA";

    // Strings for connection handling
    static final String CONNECTION_START = "CONNECTION_START";
    static final String CONNECTION_SUCCESS = "CONNECTION_SUCCESS";
    static final String CONNECTION_FAILURE = "CONNECTION_FAILURE";

    // Enforce singleton
    private BeefmoteServer() {
//        stopAfterCurrent = false;
//        notifyNowPlaying = false;
        connected = false;
        nowPlayingThread = null;
        trackBuffer = new ArrayList<>(TRACKLIST_BATCH_SIZE);
        tracklistListeners = new ArrayList<>();
        nowPlayingListeners = new ArrayList<>();
        connectionListeners = new ArrayList<>();
    }

    // Get singleton
    static BeefmoteServer get() {
        return singleton;
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

//    public boolean getStopAfterCurrent() {
//        return stopAfterCurrent;
//    }
//
//    public void setStopAfterCurrent(boolean stopAfterCurrent) {
//        this.stopAfterCurrent = stopAfterCurrent;
//        sendCommand(BEEFMOTE_STOP_AFTER_CURRENT);
//    }

    boolean isConnected() {
        return connected;
    }

    // Connects to the Beefmote server.
    void connect(final String serverIp, final int serverPort) {
        Thread thread = new Thread() {
            public void run() {
                connectionStr = CONNECTION_START;
                notifyConnectionListeners();

                try {
                    InetAddress serverAddress = InetAddress.getByName(serverIp);
                    socket = new Socket(serverAddress, serverPort);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                if (socket != null) {
                    connected = true;

                    try {
                        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    connectionStr = CONNECTION_SUCCESS;
                    notifyConnectionListeners();
                }
                else {
                    connectionStr = CONNECTION_FAILURE;
                    notifyConnectionListeners();
                }
            }
        };

        thread.start();
    }

//    void disconnect() {
//        new Thread() {
//            public void run() {
//                try {
//                    socket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//    }

    // Gets tracklist.
    void getTracklist() {
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

                while(true) {
                    try {
                        if ((inputLine = bufferedReader.readLine()) == null) {
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (inputLine == null) {
                        continue;
                    }

                    if (inputLine.startsWith(BEEFMOTE_TRACKLIST_BEGIN)) {
                        trackBuffer.add(inputLine);
                        notifyTracklistListeners();
                        trackBuffer = new ArrayList<>();

                        continue;
                    }

                    if (inputLine.equals(BEEFMOTE_TRACKLIST_END)) {
                        if (trackBuffer.size() > 0) {
                            // Send last batch
                            notifyTracklistListeners();

                            // Send the end message itself
                            trackBuffer = new ArrayList<>();
                            trackBuffer.add(inputLine);
                            notifyTracklistListeners();

                            break;
                        }
                        else {
                            // Send the end message itself
                            trackBuffer = new ArrayList<>();
                            trackBuffer.add(inputLine);

                            notifyTracklistListeners();
                        }
                    }

                    if (inputLine.startsWith(BEEFMOTE_TRACKLIST_TRACK)) {
                        trackBuffer.add(inputLine.replace(BEEFMOTE_TRACKLIST_TRACK + " ", ""));

                        if (trackBuffer.size() == TRACKLIST_BATCH_SIZE) {
                            // Send buffer and reset it
                            notifyTracklistListeners();
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

//    void playRandom() {
//        sendCommand(BEEFMOTE_RANDOM);
//    }

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

//    void seekForward() {
//        sendCommand(BEEFMOTE_SEEK_FORWARD);
//    }
//
//    void seekBackward() {
//        sendCommand(BEEFMOTE_SEEK_BACKWARD);
//    }

    void setNotifyNowPlaying(final boolean notifyNowPlaying) {
//        this.notifyNowPlaying = notifyNowPlaying;

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

                    if (inputLine == null) {
                        continue;
                    }

                    if (inputLine.startsWith(BEEFMOTE_NOW_PLAYING)) {
                        nowPlayingStr = inputLine.replace(BEEFMOTE_NOW_PLAYING + " ", "");
                        notifyNowPlayingListeners();
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

//    boolean getNotifyNowPlaying() {
//        return notifyNowPlaying;
//    }

    void addToPlaybackQueue(Track track) {
        sendCommand(BEEFMOTE_ADD_PLAYBACKQUEUE_ADDRESS + " " + track.getAddress());
    }

//    void exit() {
//        sendCommand(BEEFMOTE_EXIT);
//    }

    // TracklistEmitter interface
    @Override
    public void addTracklistListener(TracklistListener listener) {
        tracklistListeners.add(listener);
    }

    @Override
    public void removeTracklistListener(TracklistListener listener) {
        tracklistListeners.remove(listener);
    }

    @Override
    public void notifyTracklistListeners() {
        for (TracklistListener listener : tracklistListeners) {
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(TRACKLIST_DATA, trackBuffer);
            msg.setData(bundle);
            msg.what = MESSAGE_TRACKLIST_BATCH_READY;
            listener.sendMessage(msg);
        }
    }

    // NowPlayingEmitter interface
    @Override
    public void addNowPlayingListener(NowPlayingListener listener) {
        nowPlayingListeners.add(listener);
    }

    @Override
    public void removeNowPlayingListener(NowPlayingListener listener) {
        nowPlayingListeners.remove(listener);
    }

    @Override
    public void notifyNowPlayingListeners() {
        for (NowPlayingListener listener : nowPlayingListeners) {
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putString(NOW_PLAYING_DATA, nowPlayingStr);
            msg.setData(bundle);
            msg.what = MESSAGE_NOW_PLAYING;
            listener.sendMessage(msg);
        }
    }

    // ConnectionEmitter interface
    @Override
    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    @Override
    public void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    @Override
    public void notifyConnectionListeners() {
        for (ConnectionListener listener : connectionListeners) {
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putString(CONNECTION_DATA, connectionStr);
            msg.setData(bundle);
            msg.what = MESSAGE_CONNECTION;
            listener.sendMessage(msg);
        }
    }
}
