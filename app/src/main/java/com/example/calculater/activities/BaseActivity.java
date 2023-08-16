package com.example.calculater.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

public abstract class BaseActivity<viewBinding extends ViewBinding> extends AppCompatActivity {

    protected viewBinding binding = null;

    abstract viewBinding getLayout();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("currentActivity", getClass().getSimpleName());
        binding = getLayout();
        setContentView(binding.getRoot());
    }
}
