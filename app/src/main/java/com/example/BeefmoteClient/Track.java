package com.example.BeefmoteClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Track {
    private int playlistIndex;
    private String address;
    private String artist;
    private String album;
    private String number;
    private String title;
    private String duration;

    // Creates a Track from a String with Beefmote track format:
    // (trackIdx) address [artist - album] number - title (duration)
    Track(String beefmoteTrack) {
        // Is there anything on this Earth more unreadable than a regex?
        String patternStr = "\\((.*?)\\) (.*?) \\[(.*?) - (.*?)\\] (.*?) - (.*?) \\((\\d+:\\d+)\\)";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(beefmoteTrack);

        if (matcher.find()) {
            playlistIndex = Integer.parseInt(matcher.group(1));
            address = matcher.group(2);
            artist = matcher.group(3);
            album = matcher.group(4);
            number = matcher.group(5);
            title = matcher.group(6);
            duration = matcher.group(7);
        }
    }

    public int getPlaylistIndex() {
        return playlistIndex;
    }

    public String getAddress() {
        return address;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return artist + " - " + title;
    }
}
