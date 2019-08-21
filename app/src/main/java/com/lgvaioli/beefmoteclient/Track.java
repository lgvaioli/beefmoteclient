package com.lgvaioli.beefmoteclient;

import androidx.annotation.NonNull;

import java.util.Objects;
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
        String patternStr = "\\((.*?)\\) (.*?) \\[(.*?) - (.*?)] (.*?) - (.*?) \\((\\d+:\\d+)\\)";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(beefmoteTrack);

        if (matcher.find()) {
            String matched = matcher.group(1);

            if (matched != null) {
                playlistIndex = Integer.parseInt(matched);
                address = matcher.group(2);
                artist = matcher.group(3);
                album = matcher.group(4);
                number = matcher.group(5);
                title = matcher.group(6);
                duration = matcher.group(7);
            }
        }
    }

//    public int getPlaylistIndex() {
//        return playlistIndex;
//    }

    String getAddress() {
        return address;
    }

    String getArtist() {
        return artist;
    }

    String getAlbum() {
        return album;
    }

//    String getNumber() {
//        return number;
//    }

    String getTitle() {
        return title;
    }

//    String getDuration() {
//        return duration;
//    }

    @Override
    @NonNull public String toString() {
        return artist + " - " + title;
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }

        if (otherObject == null) {
            return false;
        }

        if (!(otherObject instanceof Track)) {
            return false;
        }

        Track other = (Track) otherObject;

        return playlistIndex == other.playlistIndex &&
                address.equals(other.address) &&
                artist.equals(other.artist) &&
                album.equals(other.album) &&
                number.equals(other.number) &&
                title.equals(other.title) &&
                duration.equals(other.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playlistIndex, address, artist, album, number, title, duration);
    }
}
