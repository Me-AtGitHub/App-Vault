package com.example.calculater.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.calculater.R;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.VideoViewHolder> {
    private Context context;
    private List<Uri> imageUris;

    public interface OnItemClickListener {
        void onItemClick(int position);

        boolean onItemLongClick(int position);
    }

    private OnItemClickListener listener;
    private Set<Integer> selectedPositions;

    public ImageAdapter(Context context, List<Uri> imageUris) {
        this.context = context;
        this.imageUris = imageUris;
        this.selectedPositions = new HashSet<>();
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
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        holder.thumbnailImageView.setImageURI(imageUri);
        Log.d("ImageAdapter", "onBindViewHolder: " + imageUri);
//        Bitmap thumbnail = getVideoThumbnail(imageUri);
//        holder.thumbnailImageView.setImageBitmap(thumbnail);
//        Glide.with(holder.thumbnailImageView.getContext())
//                .load(imageUri)
//                .into(holder.thumbnailImageView);
        //notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    private Bitmap getVideoThumbnail(Uri videoUri) {
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

    public class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ImageView thumbnailImageView;

        public VideoViewHolder(View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.galleryImage);
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
