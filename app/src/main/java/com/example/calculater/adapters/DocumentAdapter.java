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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {
    private List<Uri> fileList;
    private Context context;
    public interface OnItemClickListener {
        void onItemClick(int position);
        boolean onItemLongClick(int position);
    }
    private DocumentAdapter.OnItemClickListener listener;
    private Set<Integer> selectedPositions;
    public DocumentAdapter(List<Uri> fileList, Context context) {
        this.fileList = fileList;
        this.context = context;
        this.selectedPositions = new HashSet<>();

    }
    public void setOnItemClickListener(DocumentAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedPositions(Set<Integer> selectedPositions) {
        this.selectedPositions = selectedPositions;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public DocumentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull DocumentAdapter.ViewHolder holder, int position) {
        Uri audioUri = fileList.get(position);
        String audioTitle = audioUri.getLastPathSegment();
        holder.audioNameTextView.setText(audioTitle);

        if (isSelected(position)) {
            holder.document.setVisibility(View.VISIBLE);
        } else {
            holder.document.setVisibility(View.GONE);
        }
        Glide.with(context)
                .load(audioUri)
                .placeholder(R.drawable.folder)
                .into(holder.thumbnailImageView);
    }

    private boolean isSelected(int position) {
        return selectedPositions.contains(position);
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
        ImageView thumbnailImageView,document;
        private TextView audioNameTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.DocumentFile);
            document=itemView.findViewById(R.id.DocumentImageView);
            audioNameTextView = itemView.findViewById(R.id.DocumentText);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClick(getAdapterPosition());
                document.setVisibility(View.GONE);
            }
        }
        @Override
        public boolean onLongClick(View v) {
            if (listener != null) {
                thumbnailImageView.setVisibility( View.VISIBLE);
                document.setVisibility(View.VISIBLE);
                return listener.onItemLongClick(getAdapterPosition());
            }else {
                thumbnailImageView.setVisibility( View.VISIBLE);
                document.setVisibility(View.GONE);
            }
            return false;
        }

    }

}
