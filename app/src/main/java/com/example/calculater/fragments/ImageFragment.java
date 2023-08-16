package com.example.calculater.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.calculater.R;
import com.example.calculater.activities.GalleryActivity;


public class ImageFragment extends Fragment {
    private ImageView imageView,back;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView =inflater.inflate(R.layout.fragment_image, container, false);
        imageView = rootView.findViewById(R.id.image1);
        back=rootView.findViewById(R.id.arrow);
        if (getArguments() != null){
            String imagePath = getArguments().getString("imagePath");
            Glide.with(this).load(imagePath).into(imageView);
        }
        back.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GalleryActivity.class);
            startActivity(intent);
        });
        return rootView;
    }
}