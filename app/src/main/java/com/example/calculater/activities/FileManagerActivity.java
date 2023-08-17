package com.example.calculater.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.calculater.databinding.ActivityFileManagerBinding;

public class FileManagerActivity extends BaseActivity<ActivityFileManagerBinding> {

    @Override
    ActivityFileManagerBinding getLayout() {
        return ActivityFileManagerBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding.photos.setOnClickListener(v -> {
            Intent intent = new Intent(FileManagerActivity.this, GalleryActivity.class);
            startActivity(intent);
        });

        binding.videos.setOnClickListener(v -> {
            Intent intent = new Intent(FileManagerActivity.this, VideosActivity.class);
            startActivity(intent);
        });

        binding.documents.setOnClickListener(v -> {
            Intent intent = new Intent(FileManagerActivity.this, DocumentActivity.class);
            startActivity(intent);
        });

        binding.music.setOnClickListener(v -> {
            Intent intent = new Intent(FileManagerActivity.this, MusicActivity.class);
            startActivity(intent);
        });


        binding.more.setOnClickListener(v -> {
            Intent intent = new Intent(FileManagerActivity.this, MoreActivity.class);
            startActivity(intent);
        });

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // back to home button
        binding.arrow.setOnClickListener(v -> {
            onBackPressed();
        });

    }

}





