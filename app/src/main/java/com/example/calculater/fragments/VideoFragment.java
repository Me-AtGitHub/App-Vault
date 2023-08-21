package com.example.calculater.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.calculater.databinding.FragmentVideosBinding;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;

public class VideoFragment extends BaseDialogFragment<FragmentVideosBinding> {


    private String uriToVideo = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            uriToVideo = getArguments().getString("videoPath");
    }

    @Override
    FragmentVideosBinding getLayout() {
        return FragmentVideosBinding.inflate(getLayoutInflater());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (uriToVideo != null) {
            openVideoPlayer(Uri.parse(uriToVideo));
        }
    }

    private void openVideoPlayer(Uri videoUri) {
        SimpleExoPlayer player = new SimpleExoPlayer.Builder(requireContext()).build();
        binding.playerView.setPlayer(player);
        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

}
