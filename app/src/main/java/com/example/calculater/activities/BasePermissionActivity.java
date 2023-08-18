package com.example.calculater.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Window;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;

import com.example.calculater.R;
import com.example.calculater.databinding.DialogPermissionInfoBinding;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

abstract public class BasePermissionActivity<viewBinding extends ViewBinding> extends BaseActivity<viewBinding> {


    private Dialog infoDialog = null;

    private boolean checkShouldShowRational() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean audioRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_VIDEO);
            boolean videoRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_AUDIO);
            boolean imageRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES);
            boolean writeRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            boolean manageRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE);
            return audioRationale || videoRationale || imageRationale || writeRationale || manageRationale;
        } else {
            boolean readRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            boolean writeRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return readRationale || writeRationale;
        }
    }

    public void onMediaPermissionGranted() {
    }

    private void requestMediaPermissions() {
        if (!checkShouldShowRational()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(new String[]{Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE});
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                launcher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE});
            } else {
                launcher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
            }
        } else showPermissionInfoDialog();
    }

    protected boolean checkMediaPermissions() {
        checkShouldShowRational();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean videoGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
            boolean audioGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
            boolean imageGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
            boolean writeGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (videoGranted && audioGranted && imageGranted && writeGranted) {
                onMediaPermissionGranted();
                return true;
            } else requestMediaPermissions();
        } else {
            boolean readGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            boolean writeGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (readGranted && writeGranted) {
                onMediaPermissionGranted();
                return true;
            } else requestMediaPermissions();
        }
        return false;
    }

    protected void showPermissionInfoDialog() {
        DialogPermissionInfoBinding dialogBinding = DialogPermissionInfoBinding.inflate(getLayoutInflater());
        infoDialog = new Dialog(this, R.style.DialogStyle);
        infoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        infoDialog.setCancelable(false);
        infoDialog.setCanceledOnTouchOutside(false);
        infoDialog.setContentView(dialogBinding.getRoot());
        Objects.requireNonNull(infoDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialogBinding.btnGrantPermission.setOnClickListener((view) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            navigateToPermissions.launch(intent);
        });
        infoDialog.show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkMediaPermissions();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void checkManagePermissions() {
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).setData(Uri.parse("package:" + getPackageName()));
            navigateToPermissions.launch(intent);
        } else {
            if (infoDialog != null) infoDialog.dismiss();
        }
    }

    private final ActivityResultLauncher<Intent> navigateToPermissions = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (checkMediaPermissions()) checkManagePermissions();
        } else {
            if (checkMediaPermissions()) if (infoDialog != null) infoDialog.dismiss();
        }

    });


    private final ActivityResultLauncher<String[]> launcher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (result) -> {
        AtomicBoolean allPermissionGranted = new AtomicBoolean(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            result.forEach((s, aBoolean) -> {
                allPermissionGranted.set(aBoolean);
            });
        } else {
            result.forEach((s, aBoolean) -> {
                allPermissionGranted.set(aBoolean);
            });
        }
        if (allPermissionGranted.get()) {
            onMediaPermissionGranted();
        } else requestMediaPermissions();

    });


}
