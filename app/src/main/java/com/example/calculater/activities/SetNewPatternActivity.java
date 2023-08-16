package com.example.calculater.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.calculater.R;
import com.example.calculater.SharedPreferencesHelper;
import com.example.calculater.databinding.ActivitySetNewPatternBinding;

import java.util.List;

public class SetNewPatternActivity extends BaseActivity<ActivitySetNewPatternBinding> {

    SharedPreferencesHelper sharedPreferencesHelper;
    PatternLockView mPatternLockView;
    String patternMatch;

    @Override
    ActivitySetNewPatternBinding getLayout() {
        return ActivitySetNewPatternBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPatternLockView = findViewById(R.id.pattern_lock_view);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }

            public void onComplete(List<PatternLockView.Dot> pattern) {

                if (patternMatch != null && patternMatch.equals(PatternLockUtils.patternToString(mPatternLockView, pattern))) {
                    sharedPreferencesHelper.saveString("pattern", patternMatch);
                    //next screen
                    mPatternLockView.clearPattern();

                    Intent intent = new Intent(SetNewPatternActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    //Toast.makeText(SetNewPatternActivity.this, "Home Successfully", Toast.LENGTH_SHORT).show();
                } else if (patternMatch != null && !patternMatch.equals(PatternLockUtils.patternToString(mPatternLockView, pattern))) {
                    patternMatch = null;
                    mPatternLockView.clearPattern();
                    Toast.makeText(SetNewPatternActivity.this, "Password Not Match", Toast.LENGTH_SHORT).show();
                } else {
                    patternMatch = PatternLockUtils.patternToString(mPatternLockView, pattern);
                    //Re enter password
                    mPatternLockView.clearPattern();
                    Toast.makeText(SetNewPatternActivity.this, "Re-Enter Pattern", Toast.LENGTH_SHORT).show();
                }
            }

            public void onCleared() {

            }
        });

    }


}