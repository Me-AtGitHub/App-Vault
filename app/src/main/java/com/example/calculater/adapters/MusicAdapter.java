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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.calculater.R;
import com.example.calculater.utils.CommonFunctions;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.FileViewHolder> {
    private final List<File> fileList;
    private final Context context;
    private final RecyclerView recyclerView;
    private FileAdapter.OnItemClickListener listener;
    private Set<Integer> selectedPositions;

    public MusicAdapter(List<File> fileList, Context context, RecyclerView recyclerView) {
        this.fileList = fileList;
        this.context = context;
        this.selectedPositions = new HashSet<>();
        this.recyclerView = recyclerView;

    }

    public void setOnItemClickListener(FileAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedPositions(Set<Integer> selectedPositions) {
        this.selectedPositions = selectedPositions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MusicAdapter.FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio, parent, false);
        return new MusicAdapter.FileViewHolder(view);
    }

    public void onBindViewHolder(@NonNull MusicAdapter.FileViewHolder holder, int position) {
        Uri audioUri = Uri.fromFile(fileList.get(position));
        Log.d("onBindViewHolder", "onBindViewHolder: " + audioUri);
        String audioTitle = CommonFunctions.getFileName(audioUri, holder.audioNameTextView.getContext());
        holder.audioNameTextView.setText(audioTitle);
        Glide.with(context).load(audioUri).placeholder(R.drawable.music).into(holder.thumbnailImageView);
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

    public interface OnItemClickListener {
        void onItemClick(int position);

        boolean onItemLongClick(int position);
    }

    public class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ImageView thumbnailImageView, trick;
        private final TextView audioNameTextView;

        public FileViewHolder(View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.MusicFile);
            trick = itemView.findViewById(R.id.tickImageView);
            audioNameTextView = itemView.findViewById(R.id.musicText);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClick(getAdapterPosition());
                trick.setVisibility(View.GONE);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (listener != null) {
                thumbnailImageView.setVisibility(View.VISIBLE);
                trick.setVisibility(View.VISIBLE);
                return listener.onItemLongClick(getAdapterPosition());
            } else {
                thumbnailImageView.setVisibility(View.VISIBLE);
                trick.setVisibility(View.GONE);
            }
            return false;
        }
    }
}


