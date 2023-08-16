package com.example.calculater.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
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

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.FileViewHolder> {
    private List<Uri> fileList;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(int position);
        boolean onItemLongClick(int position);
    }

    private DownloadAdapter.OnItemClickListener listener;
    private Set<Integer> selectedPositions;

    public DownloadAdapter(List<Uri> fileList, Context context) {
        this.fileList = fileList;
        this.context = context;
        this.selectedPositions = new HashSet<>();

    }
    public void setOnItemClickListener(DownloadAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
    public void setSelectedPositions(Set<Integer> selectedPositions) {
        this.selectedPositions = selectedPositions;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public DownloadAdapter.FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new DownloadAdapter.FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        Uri videoUri = fileList.get(position);
       // Bitmap thumbnail = getVideoThumbnail(videoUri);
       // holder.thumbnailImageView.setImageBitmap(thumbnail);
        /*Glide.with(holder.thumbnailImageView.getContext())
                .load(videoUri)
                .into(holder.thumbnailImageView);*/
        //
        String fileType = getFileType(videoUri);
      // holder.audioNameTextView.setText(fileType);

        if (fileType.equals("image")) {
            Bitmap thumbnail = getVideoThumbnail(videoUri);
            holder.thumbnailImageView.setImageBitmap(thumbnail);
            Glide.with(holder.thumbnailImageView.getContext())
                    .load(videoUri)
                    .into(holder.thumbnailImageView);

        }
        else if (fileType.equals("video")){
            Bitmap thumbnail = getVideoThumbnail(videoUri);
            holder.thumbnailImageView.setImageBitmap(thumbnail);
            Glide.with(holder.thumbnailImageView.getContext())
                    .load(videoUri)
                    .into(holder.thumbnailImageView);
        }

        else if (fileType.equals("audio")) {
            String audioTitle = videoUri.getLastPathSegment();
            holder.audioNameTextView.setText(audioTitle);
            Glide.with(holder.audioNameTextView.getContext())
                    .load(videoUri)
                    .placeholder(R.drawable.music)
                    .into(holder.thumbnailImageView);

        }
        else if (fileType.equals("pdf") || fileType.equals("doc")) {
            String fileName = videoUri.getLastPathSegment();
            holder.audioNameTextView.setText(fileName);
            Glide.with(holder.audioNameTextView.getContext())
                    .load(videoUri)
                    .placeholder(R.drawable.folder)
                    .into(holder.thumbnailImageView);
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }
    private String getFileType(Uri fileUri) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return "image";
            }else if (mimeType.startsWith("audio/")) {
                return "audio";
            }
            else if (mimeType.startsWith("video/")) {
                return "video";
            } else if (mimeType.equals("application/pdf")) {
                return "pdf";
            } else if (mimeType.equals("application/msword") || mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                return "doc";
            }
        }
        return "unknown";
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


        TextView audioNameTextView;
        public FileViewHolder(View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.textFileName);
            audioNameTextView = itemView.findViewById(R.id.fileNameTextView);
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
