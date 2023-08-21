package com.example.calculater.activities;

import android.content.Intent;
import android.os.Bundle;

import com.example.calculater.R;
import com.example.calculater.databinding.ActivityHomeBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HomeActivity extends BasePermissionActivity<ActivityHomeBinding> {


    private final List<Integer> wallpapers = new ArrayList<>();

    @Override
    ActivityHomeBinding getLayout() {
        return ActivityHomeBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wallpapers.add(R.drawable.wall_one);
        wallpapers.add(R.drawable.wall_two);
        wallpapers.add(R.drawable.wall_three);
        wallpapers.add(R.drawable.wall_four);
        wallpapers.add(R.drawable.wall_five);

        binding.llFileManager.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FileManagerActivity.class);
            startActivity(intent);
        });
        binding.llSettings.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
            startActivity(intent);
        });
        binding.llPhotos.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, GalleryActivity.class);
            startActivity(intent);
        });
        binding.llVideos.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, VideosActivity.class);
            startActivity(intent);
        });
        binding.llTrash.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TrashActivity.class);
            startActivity(intent);
        });

        binding.llWallPaper.setOnClickListener(v -> {
            setRandomWallpaper();
        });
        setRandomWallpaper();

    }

    private void setRandomWallpaper() {
        int position = new Random().nextInt(wallpapers.size() - 1);
        binding.getRoot().setBackgroundResource(wallpapers.get(position));
    }


}