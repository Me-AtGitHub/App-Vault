package com.example.calculater.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.calculater.databinding.ActivitySettingBinding;

public class SettingActivity extends BaseActivity<ActivitySettingBinding> {

    @Override
    ActivitySettingBinding getLayout() {
        return ActivitySettingBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //// click button
        binding.arrow.setOnClickListener(v -> {
            onBackPressed();
        });
        binding.help.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, HelpFeedbackActivity.class);
            startActivity(intent);
        });
        binding.privacy.setOnClickListener(v -> {
            try {
                Uri number = Uri.parse("https://airai.notion.site/Privacy-Policy-Calculator-Locker-8d5c7670979d491ab460c647136e36d1?pvs=4");
                Intent callIntent = new Intent(Intent.ACTION_VIEW, number);
                startActivity(callIntent);
            } catch (Exception e) {
            }
        });
        binding.setPassword.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, RecoveryPasswordActivity.class);
            startActivity(intent);
        });
        binding.about.setOnClickListener(v -> {
            try {
                Uri number = Uri.parse("https://www.airai.games/");
                Intent callIntent = new Intent(Intent.ACTION_VIEW, number);
                startActivity(callIntent);
            } catch (Exception e) {

            }
        });
        binding.restassword.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });

    }

}