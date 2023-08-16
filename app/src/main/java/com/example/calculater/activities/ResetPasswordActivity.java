package com.example.calculater.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.calculater.R;
import com.example.calculater.SharedPreferencesHelper;
import com.example.calculater.databinding.ActivityResetPasswordBinding;

import java.util.List;

public class ResetPasswordActivity extends BaseActivity<ActivityResetPasswordBinding> {
    SharedPreferencesHelper sharedPreferencesHelper;
    PatternLockView mPatternLockView;
    String patternMatch;
    TextView forgetPass;
    private String newPattern = "";
    private boolean isSettingNewPattern = false;

    @Override
    ActivityResetPasswordBinding getLayout() {
        return ActivityResetPasswordBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        forgetPass = findViewById(R.id.forget);
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        mPatternLockView = findViewById(R.id.pattern_lock_view);

        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResetPasswordActivity.this, RecoveryQuestionActivity.class);
                startActivity(intent);
            }
        });
        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }

            public void onComplete(List<PatternLockView.Dot> pattern) {
                String storedPattern = sharedPreferencesHelper.getString("pattern", "");

                if (storedPattern.isEmpty()) {
                    // No stored pattern found, creating a new one
                    String newPattern = PatternLockUtils.patternToString(mPatternLockView, pattern);
                    // Save the new pattern
                    sharedPreferencesHelper.saveString("pattern", newPattern);
                    // Clear the pattern view
                    mPatternLockView.clearPattern();
                    // Display a message or proceed to the next screen
                    Toast.makeText(ResetPasswordActivity.this, "New pattern set successfully", Toast.LENGTH_SHORT).show();
                    // Start the next activity
                    Intent intent = new Intent(ResetPasswordActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // A stored pattern exists, compare with the user's input
                    if (storedPattern.equals(PatternLockUtils.patternToString(mPatternLockView, pattern))) {
                        // Pattern matches, ask the user to set a new pattern
                        Toast.makeText(ResetPasswordActivity.this, "Pattern matched. Please set a new pattern.", Toast.LENGTH_SHORT).show();
                        // Clear the pattern view
                        mPatternLockView.clearPattern();
                        // Update the stored pattern to an empty string
                        sharedPreferencesHelper.saveString("pattern", "");
                    } else {
                        // Pattern doesn't match, display an error message
                        Toast.makeText(ResetPasswordActivity.this, "Wrong Pattern", Toast.LENGTH_SHORT).show();
                        // Clear the pattern view for re-entry
                        mPatternLockView.clearPattern();
                    }
                }
            }

            @Override
            public void onCleared() {

            }
        });

    }


}