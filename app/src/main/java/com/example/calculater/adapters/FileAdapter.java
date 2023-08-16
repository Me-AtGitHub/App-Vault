package com.example.calculater.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
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

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<Uri> fileList;
    private Context context;
    public interface OnItemClickListener {
        void onItemClick(int position);
        boolean onItemLongClick(int position);
    }

    private OnItemClickListener listener;
    private Set<Integer> selectedPositions;

    public FileAdapter(List<Uri> fileList, Context context) {
        this.fileList = fileList;
        this.context = context;
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
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        Uri videoUri = fileList.get(position);
        Bitmap thumbnail = getVideoThumbnail(videoUri);
        holder.thumbnailImageView.setImageBitmap(thumbnail);
        Glide.with(holder.thumbnailImageView.getContext())
                .load(videoUri)
                .into(holder.thumbnailImageView);
    }

    @Override
    public int getItemCount() {
        return fileList.size();
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

    public class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
        ImageView thumbnailImageView;
        public FileViewHolder(View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.textFileName);
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
