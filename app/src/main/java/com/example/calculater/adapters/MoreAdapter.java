package com.example.calculater.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.calculater.R;
import com.example.calculater.utils.SavedData;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MoreAdapter extends RecyclerView.Adapter<MoreAdapter.ViewHolder> {
    private List<SavedData> trashList;
    private OnItemClickListener listener;
    private Set<Integer> selectedPositions;

    public MoreAdapter(List<SavedData> trashList) {
        this.trashList = trashList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedPositions(Set<Integer> selectedPositions) {
        this.selectedPositions = selectedPositions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trash, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedData savedData = trashList.get(position);
        if (savedData.fileType != null) {
            switch (savedData.fileType) {

                case IMAGE:
                case VIDEO:
                    holder.audioNameTextView.setText(savedData.fileUri.getLastPathSegment());
                    Bitmap thumbnail = getVideoThumbnail(savedData.fileUri, holder.itemView.getContext());
                    holder.thumbnailImageView.setImageBitmap(thumbnail);
                    Glide.with(holder.thumbnailImageView.getContext())
                            .load(savedData.fileUri)
                            .centerCrop()
                            .into(holder.thumbnailImageView);
                    break;

                case DOCUMENT:
                    String fileName = savedData.fileUri.getLastPathSegment();
                    holder.audioNameTextView.setText(fileName);
                    holder.thumbnailImageView.setImageResource(R.drawable.folder);
                    break;
                case AUDIO:
                    String audioTitle = savedData.fileUri.getLastPathSegment();
                    holder.audioNameTextView.setText(audioTitle);
                    holder.thumbnailImageView.setImageResource(R.drawable.music);
                    break;

            }
        }

        /*if (selectedPositions != null && selectedPositions.contains(position)) {
            holder.trick.setVisibility(View.VISIBLE);
        } else {
            holder.trick.setVisibility(View.GONE);
        }*/

    }

    @Override
    public int getItemCount() {
        return trashList.size();
    }


    private Bitmap getVideoThumbnail(Uri videoUri, Context context) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, videoUri);
            return retriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        boolean onItemLongClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        //  ImageView imageView;
        ImageView thumbnailImageView;
        ImageView trick;
        TextView audioNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // imageView = itemView.findViewById(R.id.trash);
            thumbnailImageView = itemView.findViewById(R.id.MusicFile);
            //trick = itemView.findViewById(R.id.tickImageView);
            audioNameTextView = itemView.findViewById(R.id.musicText);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClick(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (listener != null) {
                return listener.onItemLongClick(getAdapterPosition());
            }
            return false;
        }
    }
}
