package com.lgvaioli.beefmoteclient;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlaylistRecyclerViewAdapter
        extends RecyclerView.Adapter<PlaylistRecyclerViewAdapter.ViewHolder>
        implements Filterable {
    private RecyclerView playlistRecyclerView;
    private Track currentTrack;
    private final ArrayList<Integer> currentTrackPosition = new ArrayList<>();
    private ArrayList<Track> trackList;
    private ArrayList<Track> trackListFull;
    private LayoutInflater inflater;
    private ItemClickListener clickListener;
    static final int CONTEXT_MENU_ADD_TO_PLAYBACKQUEUE = 100;
    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            // This is necessary because given that the track loading is not blocking anymore,
            // trackListFull is initially null and we have to initialize it somewhere. If the
            // user searches something *before* all the tracks are loaded we're in trouble though,
            // so we should either a) make sure that the user doesn't search anything before
            // all the tracks are loaded, or b) change this to account for that situation.
            if (trackListFull == null) {
                trackListFull = new ArrayList<>(trackList);
            }

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

        @Override @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            trackList.clear();
            trackList.addAll((ArrayList<Track>) filterResults.values);
            notifyDataSetChanged();

            // Refresh current track position
            int currentPosition = getTrackPosition(currentTrack);
            setCurrentTrackPosition(currentPosition);

            // Scroll RecyclerView
            LinearLayoutManager layoutManager = (LinearLayoutManager) playlistRecyclerView.getLayoutManager();

            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(currentPosition, 0);
            }
        }
    };

      /////////////////////////////////////////////////////////////////////////
     // SUBCLASS. Stores and recycles views as they are scrolled off screen //
    /////////////////////////////////////////////////////////////////////////
    public class ViewHolder
              extends RecyclerView.ViewHolder
              implements View.OnClickListener, View.OnCreateContextMenuListener {
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
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onItemClick(view, getAdapterPosition());
            }
        }

          @Override
          public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                          ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.add(this.getAdapterPosition(), CONTEXT_MENU_ADD_TO_PLAYBACKQUEUE,
                    Menu.NONE, R.string.addPlaybackQueue);

            // Using this you can create menus dynamically depending on the clicked track's
            // properties. This is useful to create, for example, a "Remove from playback queue"
            // menu (which shouldn't be shown unless the track is already in the playback queue).
            //Track track = getItem(this.getAdapterPosition());
          }
      }

    // Constructor
    PlaylistRecyclerViewAdapter(Context context, RecyclerView playlistRecyclerView, ArrayList<Track> trackList) {
        this.inflater = LayoutInflater.from(context);
        this.playlistRecyclerView = playlistRecyclerView;
        this.trackList = trackList;
        this.trackListFull = null;
    }

    // Sets current track
    void setCurrentTrack(Track t) {
        currentTrack = t;
    }

    // Inflates the row layout from xml when needed
    @Override
    @NonNull public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.playlist_row, parent, false);
        return new ViewHolder(view);
    }

    // Highlights holder.
    void highlightHolder(ViewHolder holder) {
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Track track = trackList.get(position);
        holder.playlistRowTitle.setText(track.getTitle());
        holder.playlistRowArtist.setText(track.getArtist());
        SpannableString albumSpanString = new SpannableString(track.getAlbum());
        albumSpanString.setSpan(new StyleSpan(Typeface.ITALIC), 0, albumSpanString.length(), 0);
        holder.playlistRowAlbum.setText(albumSpanString);

        if (!currentTrackPosition.contains(position)) {
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

    // Set currentTrackPosition
    void setCurrentTrackPosition (int newPosition) {
        if (currentTrackPosition.isEmpty()) {
            currentTrackPosition.add(newPosition);
        } else {
            int old = currentTrackPosition.get(0);
            currentTrackPosition.clear();
            currentTrackPosition.add(newPosition);
            notifyItemChanged(old);
        }
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
