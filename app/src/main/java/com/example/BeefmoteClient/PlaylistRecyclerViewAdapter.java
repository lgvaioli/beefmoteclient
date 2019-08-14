package com.example.BeefmoteClient;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlaylistRecyclerViewAdapter
        extends RecyclerView.Adapter<PlaylistRecyclerViewAdapter.ViewHolder>
        implements Filterable {

    private ArrayList<Track> trackList;
    private ArrayList<Track> trackListFull;
    private LayoutInflater inflater;
    private ItemClickListener clickListener;
    private final ArrayList<Integer> selectedTracks = new ArrayList<>();
    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Track> filteredTrackList = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0) {
                filteredTrackList.addAll(trackListFull);
            }
            else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (Track track : trackListFull) {
                    if (track.getArtist().toLowerCase().contains(filterPattern) ||
                            track.getAlbum().toLowerCase().contains(filterPattern) ||
                            track.getTitle().toLowerCase().contains(filterPattern)) {
                        filteredTrackList.add(track);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredTrackList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            trackList.clear();
            trackList.addAll((ArrayList<Track>) filterResults.values);
            notifyDataSetChanged();
        }
    };

      /////////////////////////////////////////////////////////////////////////
     // SUBCLASS. Stores and recycles views as they are scrolled off screen //
    /////////////////////////////////////////////////////////////////////////
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout playlistRowLayout;
        TextView playlistRowTitle;
        TextView playlistRowArtist;
        TextView playlistRowAlbum;

        ViewHolder(View itemView) {
            super(itemView);
            playlistRowLayout = itemView.findViewById(R.id.playlistRowLayout);
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
        this.trackListFull = new ArrayList<>(trackList);
    }

    // Inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.playlist_row, parent, false);
        return new ViewHolder(view);
    }

    // Highlights holder.
    public void highlightHolder(ViewHolder holder) {
        // FIXME hardcoded colors
        holder.playlistRowLayout.setBackgroundColor(Color.parseColor("#008577"));
        holder.playlistRowTitle.setTextColor(Color.WHITE);
        holder.playlistRowArtist.setTextColor(Color.WHITE);
        holder.playlistRowAlbum.setTextColor(Color.WHITE);
    }

    // De-highlights holder.
    private void dehighlightHolder(ViewHolder holder) {
        holder.playlistRowLayout.setBackgroundColor(Color.TRANSPARENT);
        holder.playlistRowTitle.setTextColor(Color.BLACK);
        holder.playlistRowArtist.setTextColor(Color.BLACK);
        holder.playlistRowAlbum.setTextColor(Color.BLACK);
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

        if (!selectedTracks.contains(position)) {
            dehighlightHolder(holder);
        }
        else {
            highlightHolder(holder);
        }
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

    // Get selectedTracks
    ArrayList<Integer> getSelectedTracks() {
        return selectedTracks;
    }

    // Allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    // Parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // Get adapter position of track in current (i.e. filtered) tracklist
    int getTrackPosition(Track track) {
        int i = 0;
        for (Track t : trackList) {
            if (t.equals(track)) {
                return i;
            }
            i++;
        }

        return -1;
    }

    // Filterable methods
    @Override
    public Filter getFilter() {
        return filter;
    }
}
