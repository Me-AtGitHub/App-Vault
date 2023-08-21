package com.example.calculater.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.calculater.databinding.FragmentImageBinding;


public class ImageFragment extends BaseDialogFragment<FragmentImageBinding> {
    @Override
    FragmentImageBinding getLayout() {
        return FragmentImageBinding.inflate(LayoutInflater.from(requireContext()));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            String imagePath = getArguments().getString("imagePath");
            Glide.with(this).load(imagePath).into(binding.image1);
        }
        binding.arrow.setOnClickListener(v -> {
            dismiss();
        });
    }

}