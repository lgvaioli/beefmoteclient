package com.example.BeefmoteClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Track {
    private String trackAddr;
    private String artist;
    private String album;
    private String trackNum;
    private String title;
    private String duration;
    private String originalString;

    // Creates a Track from a String with Beefmote track format:
    // trackAddr [artist - album] trackNum - title (duration)
    Track(String beefmoteTrack) {
        originalString = beefmoteTrack;

        // Is there anything on this Earth more unreadable than a regex?
        String patternStr = "\\((.*?)\\) (.*?) \\[(.*?) - (.*?)\\] (.*?) - (.*?) \\((\\d+:\\d+)\\)";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(beefmoteTrack);

        if (matcher.find()) {
            trackAddr = matcher.group(2);
            artist = matcher.group(3);
            album = matcher.group(4);
            trackNum = matcher.group(5);
            title = matcher.group(6);
            duration = matcher.group(7);
        }
    }

    public String getTrackAddr() {
        return trackAddr;
    }

    public void setTrackAddr(String trackAddr) {
        this.trackAddr = trackAddr;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getTrackNum() {
        return trackNum;
    }

    public void setTrackNum(String trackNum) {
        this.trackNum = trackNum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String toString() {
        return artist + " - " + title;
    }
}
