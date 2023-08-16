package com.example.calculater.activities;

import android.content.Intent;
import android.os.Bundle;

import com.example.calculater.databinding.ActivityHomeBinding;

public class HomeActivity extends BaseActivity<ActivityHomeBinding> {

    @Override
    ActivityHomeBinding getLayout() {
        return ActivityHomeBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding.fileManager.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FileManagerActivity.class);
            startActivity(intent);
        });
        binding.setting.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
            startActivity(intent);
        });
        binding.photo.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, GalleryActivity.class);
            startActivity(intent);
        });
        binding.video.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, VideosActivity.class);
            startActivity(intent);
        });
        binding.trash.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TrashActivity.class);
            startActivity(intent);
        });
    }


}