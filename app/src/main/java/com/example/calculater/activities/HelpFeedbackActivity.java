package com.example.calculater.activities;

import android.os.Bundle;

import com.example.calculater.databinding.ActivityAboutBinding;

public class HelpFeedbackActivity extends BaseActivity<ActivityAboutBinding> {
    @Override
    ActivityAboutBinding getLayout() {
        return ActivityAboutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.arrow.setOnClickListener(v -> {
            onBackPressed();
        });
    }
}