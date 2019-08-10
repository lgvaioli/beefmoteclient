package com.example.BeefmoteClient;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlaylistRecyclerViewAdapter extends RecyclerView.Adapter<PlaylistRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Track> trackList;
    private LayoutInflater inflater;
    private ItemClickListener clickListener;

    // Stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView playlistRowTitle;
        TextView playlistRowArtist;
        TextView playlistRowAlbum;

        ViewHolder(View itemView) {
            super(itemView);
            playlistRowTitle = itemView.findViewById(R.id.playlistRowTitle);
            playlistRowArtist = itemView.findViewById(R.id.playlistRowArtist);
            playlistRowAlbum = itemView.findViewById(R.id.playlistRowAlbum);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    // Constructor
    PlaylistRecyclerViewAdapter(Context context, ArrayList<Track> trackList) {
        this.inflater = LayoutInflater.from(context);
        this.trackList = trackList;
    }

    // Inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.playlist_row, parent, false);
        return new ViewHolder(view);
    }

    // Binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Track track = trackList.get(position);
        holder.playlistRowTitle.setText(track.getTitle());
        holder.playlistRowArtist.setText(track.getArtist());
        SpannableString albumSpanString = new SpannableString(track.getAlbum());
        albumSpanString.setSpan(new StyleSpan(Typeface.ITALIC), 0, albumSpanString.length(), 0);
        holder.playlistRowAlbum.setText(albumSpanString);
    }

    // Total number of rows
    @Override
    public int getItemCount() {
        return trackList.size();
    }

    // Convenience method for getting data at click position
    Track getItem(int id) {
        return trackList.get(id);
    }

    // Allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    // Parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
