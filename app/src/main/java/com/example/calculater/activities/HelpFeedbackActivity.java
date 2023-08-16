package com.example.calculater.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.calculater.R;
import com.example.calculater.databinding.ActivityAboutBinding;

public class HelpFeedbackActivity extends BaseActivity<ActivityAboutBinding> {

    ImageView arow;

    @Override
    ActivityAboutBinding getLayout() {
        return ActivityAboutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arow = findViewById(R.id.arrow);


        arow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HelpFeedbackActivity.this, SettingActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}