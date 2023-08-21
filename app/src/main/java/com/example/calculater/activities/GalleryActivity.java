package com.example.calculater.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.calculater.R;
import com.example.calculater.adapters.ImageAdapter;
import com.example.calculater.databinding.ActivityGalleryBinding;
import com.example.calculater.fragments.ImageFragment;
import com.example.calculater.utils.CommonFunctions;
import com.example.calculater.utils.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GalleryActivity extends BaseActivity<ActivityGalleryBinding> implements View.OnClickListener {

    private static final int GALLERY_REQUEST_CODE = 100;
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 41;
    private static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 42;
    private final List<File> selectedVideoUris = new ArrayList<File>();
    boolean add = false;
    private ImageAdapter imageAdapter;
    private ActionMode actionMode;
    private Set<Integer> selectedPositions;
    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.actionDelete) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    deleteSelectedVideos();
                }
                mode.finish();
                return true;
            } else if (item.getItemId() == R.id.restore) {
                restoreSelectedVideos();
                mode.finish();
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            selectedPositions.clear();
            imageAdapter.setSelectedPositions(selectedPositions);
        }
    };

    private Uri newImageUri = null;

    public boolean checkPermissionForReadExtertalStorage() {
        int result = getApplicationContext().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkPermissionForWriteExternalStorage() {
        int result = getApplicationContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissionForReadExtertalStorage() {
        try {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void requestPermissionForWriteExtertalStorage() {
        try {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    ActivityGalleryBinding getLayout() {
        return ActivityGalleryBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        selectedVideoUris.addAll(CommonFunctions.getFilesOne(FileType.IMAGE));
        imageAdapter = new ImageAdapter(this, selectedVideoUris);
        binding.recyclerViewGallery.setLayoutManager(new GridLayoutManager(this, 3));
        binding.recyclerViewGallery.setAdapter(imageAdapter);

        binding.arrow.setOnClickListener(this);
        binding.galleryAdd.setOnClickListener(this);
        selectedPositions = new HashSet<>();

        try {
            imageAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    if (actionMode != null) {
                        toggleSelection(position);
                    } else {
                        Bundle bundle = new Bundle();
                        if (selectedVideoUris.get(position) != null) {
                            bundle.putString("imagePath", String.valueOf(selectedVideoUris.get(position)));
                        }
                        DialogFragment fragment = new ImageFragment();
                        fragment.setArguments(bundle);
                        fragment.show(getSupportFragmentManager(), "");
                    }
                }

                @Override
                public boolean onItemLongClick(int position) {
                    if (actionMode == null) {
                        actionMode = startActionMode(actionModeCallback);
                    }
                    toggleSelection(position);
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (selectedVideoUris.isEmpty())
            binding.tvNoFilesYet.setVisibility(View.VISIBLE);
        else
            binding.tvNoFilesYet.setVisibility(View.GONE);
    }

    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        imageAdapter.setSelectedPositions(selectedPositions);

        if (selectedPositions.isEmpty()) {
            actionMode.finish();
        } else {
            actionMode.invalidate();
            updateActionModeTitle();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void deleteSelectedVideos() {
        List<File> selectedVideos = new ArrayList<>();
        for (int position : selectedPositions) {
            if (((selectedVideoUris.size() - 1) >= position) && position != -1) {
                selectedVideos.add(selectedVideoUris.get(position));
                CommonFunctions.moveToTrash(this, Uri.fromFile(selectedVideoUris.get(position)));
            }
        }
        selectedVideoUris.removeAll(selectedVideos);
        selectedPositions.clear();
        imageAdapter.setSelectedPositions(selectedPositions);
        imageAdapter.notifyDataSetChanged();
        updateActionModeTitle();

        if (selectedVideoUris.isEmpty()) {
            binding.tvNoFilesYet.setVisibility(View.VISIBLE);
        } else binding.tvNoFilesYet.setVisibility(View.GONE);

    }

    private void restoreSelectedVideos() {
        List<File> selectedVideos = new ArrayList<>();
        for (int position : selectedPositions) {
            if (selectedVideoUris.size() >= position && position != -1) {
                selectedVideos.add(selectedVideoUris.get(position));
                CommonFunctions.recoverFile(this, Uri.fromFile(selectedVideoUris.get(position)), FileType.IMAGE);
                selectedVideoUris.get(position).delete();
//                CommonFunctions.deleteFile(this, selectedVideoUris.get(position));
            }
        }
        selectedVideoUris.removeAll(selectedVideos);
        selectedPositions.clear();
        imageAdapter.setSelectedPositions(selectedPositions);
        updateActionModeTitle();
        imageAdapter.notifyDataSetChanged();
        if (selectedVideoUris.isEmpty())
            binding.tvNoFilesYet.setVisibility(View.VISIBLE);
        else
            binding.tvNoFilesYet.setVisibility(View.GONE);
    }

    private void updateActionModeTitle() {
        int selectedCount = selectedPositions.size();
        actionMode.setTitle(String.valueOf(selectedCount));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = null;
                if (selectedImageUri != null) {
                    cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);
                }
                if (cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    cursor.close();
                    File newFile = CommonFunctions.saveFile(this, selectedImageUri, FileType.IMAGE);
                    int position = selectedVideoUris.size();
                    selectedVideoUris.add(newFile);
                    imageAdapter.notifyItemInserted(position);
                    binding.tvNoFilesYet.setVisibility(View.GONE);
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.arrow) {
            onBackPressed();
        } else if (view.getId() == R.id.galleryAdd) {
            if (checkPermissionForReadExtertalStorage()) {
                if (checkPermissionForWriteExternalStorage()) {
                    add = true;
                } else {
                    add = false;
                    try {
                        requestPermissionForWriteExtertalStorage();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                add = false;
                try {
                    requestPermissionForReadExtertalStorage();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if (add) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        }
    }

}


