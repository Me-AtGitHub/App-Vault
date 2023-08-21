package com.example.calculater.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewbinding.ViewBinding;

import com.example.calculater.R;

abstract public class BaseDialogFragment<viewBinding extends ViewBinding> extends DialogFragment {
    protected viewBinding binding;

    abstract viewBinding getLayout();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(STYLE_NORMAL, R.style.DialogFragment);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            if (getDialog().getWindow() != null) {
                getDialog().getWindow().setLayout(width, height);
                getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                getDialog().getWindow().setDimAmount(0.8F);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = getLayout();
        return binding.getRoot();
    }
}
