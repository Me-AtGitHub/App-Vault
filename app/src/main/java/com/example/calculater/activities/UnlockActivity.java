package com.example.calculater.activities;

import static com.example.calculater.utils.CommonFunctions.clearLightStatusBar;
import static com.example.calculater.utils.CommonFunctions.setLightStatusBar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.calculater.R;
import com.example.calculater.SharedPreferencesHelper;
import com.example.calculater.databinding.ActivityunlockBinding;

import java.util.List;

public class UnlockActivity extends BaseActivity<ActivityunlockBinding> {
    SharedPreferencesHelper sharedPreferencesHelper;
    PatternLockView mPatternLockView;
    TextView textView, titleText;
    String patternMatch;


    @Override
    ActivityunlockBinding getLayout() {
        return ActivityunlockBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLightStatusBar(this);
        titleText = findViewById(R.id.textView);
        textView = findViewById(R.id.forget);
        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(UnlockActivity.this, RecoveryPasswordActivity.class);
            startActivity(intent);
        });

        if (sharedPreferencesHelper.getString("pattern", "").isEmpty()) {
            titleText.setText("Create password");
            Toast.makeText(UnlockActivity.this, "Enter Your Pattern", Toast.LENGTH_SHORT).show();
        } else titleText.setText("Enter password");

        mPatternLockView = findViewById(R.id.pattern_lock_view);
        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                //Log.e("db",dbHelper.getPattern().toString());
                if (sharedPreferencesHelper.getString("pattern", "").isEmpty()) {
                    Log.e("db1", sharedPreferencesHelper.getString("pattern", ""));
                    // gat pattern to dataBase

                    if (patternMatch != null && patternMatch.equals(PatternLockUtils.patternToString(mPatternLockView, pattern))) {
                        sharedPreferencesHelper.saveString("pattern", patternMatch);
                        mPatternLockView.clearPattern();
                        Intent intent = new Intent(UnlockActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        patternMatch = PatternLockUtils.patternToString(mPatternLockView, pattern);
                        mPatternLockView.clearPattern();
                        Toast.makeText(UnlockActivity.this, "Re-Enter Pattern", Toast.LENGTH_SHORT).show();
                    }

                } else {

                    if (sharedPreferencesHelper.getString("pattern", "").equals(PatternLockUtils.patternToString(mPatternLockView, pattern))) {
                        Intent intent = new Intent(UnlockActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(UnlockActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                        mPatternLockView.clearPattern();
                    }
                }

            }

            @Override
            public void onCleared() {

            }

        });
    }

    @Override
    protected void onDestroy() {
        clearLightStatusBar(this);
        super.onDestroy();
    }
}