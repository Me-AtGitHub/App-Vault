package com.example.calculater.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.calculater.R;
import com.example.calculater.adapters.FileAdapter;
import com.example.calculater.adapters.MusicAdapter;
import com.example.calculater.databinding.ActivityMusicBinding;
import com.example.calculater.utils.CommonFunctions;
import com.example.calculater.utils.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MusicActivity extends BaseActivity<ActivityMusicBinding> {

    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 41;
    private static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 42;
    private final int FILE_REQUEST_CODE = 1;
    boolean add = false;
    private List<Uri> selectedVideoUris;
    private MusicAdapter fileAdapter;
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
                deleteSelectedVideos();
                mode.finish();
                return true;
            } else if (item.getItemId() == R.id.restore) {
                //restoreVideo((newImageUri));
                restoreSelectedVideos();
                mode.finish();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            selectedPositions.clear();
            fileAdapter.setSelectedPositions(selectedPositions);
        }
    };

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
            Log.e("permission ", "requested");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void requestPermissionForWriteExtertalStorage() {
        try {
            Log.e("permission ", "requested");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    ActivityMusicBinding getLayout() {
        return ActivityMusicBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedVideoUris = new ArrayList<>();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, FILE_REQUEST_CODE);
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            selectedVideoUris.addAll(CommonFunctions.getFiles(FileType.AUDIO));
            fileAdapter = new MusicAdapter(selectedVideoUris, this, binding.recyclerMusic);
            binding.recyclerMusic.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerMusic.setAdapter(fileAdapter);

            if (selectedVideoUris.isEmpty()) binding.tvNoFilesYet.setVisibility(View.VISIBLE);
            else binding.tvNoFilesYet.setVisibility(View.GONE);

        }

        selectedPositions = new HashSet<>();
        try {
            fileAdapter.setOnItemClickListener(new FileAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    if (actionMode != null) {
                        toggleSelection(position);
                    } /*else {
//                        selectedVideoUris.get(position);
                    }*/
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

        binding.arrow.setOnClickListener(v -> onBackPressed());

        binding.musicAdd.setOnClickListener(v -> {
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
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                // intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Audio"), FILE_REQUEST_CODE);
            }
        });

    }

    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            // Do something here
        } else {
            selectedPositions.add(position);

        }
        fileAdapter.setSelectedPositions(selectedPositions);

        if (selectedPositions.isEmpty()) {
            actionMode.finish();
        } else {
            actionMode.invalidate();
            updateActionModeTitle();
        }

    }

    public void deleteItem(int position) {

        Uri fileUri = selectedVideoUris.get(position);

        String filePath = fileUri.getPath();

        File fileToDelete = new File(filePath);
        if (fileToDelete.exists()) {
            boolean isDeleted = fileToDelete.delete();
        }

    }

    private void restoreSelectedVideos() {
        List<Uri> selectedVideos = new ArrayList<>();
        for (int position : selectedPositions) {
            if (selectedVideoUris.size() >= position && position != -1) {
                selectedVideos.add(selectedVideoUris.get(position));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    CommonFunctions.recoverFile(this, selectedVideoUris.get(position), FileType.AUDIO);
                    deleteItem(position);
                }
            }
        }
        selectedVideoUris.removeAll(selectedVideos);
        selectedPositions.clear();
        fileAdapter.setSelectedPositions(selectedPositions);
        fileAdapter.notifyDataSetChanged();
        updateActionModeTitle();

        if (selectedVideoUris.isEmpty()) binding.tvNoFilesYet.setVisibility(View.VISIBLE);
        else binding.tvNoFilesYet.setVisibility(View.GONE);

    }

    private void deleteSelectedVideos() {
        List<Uri> selectedVideos = new ArrayList<>();
        for (int position : selectedPositions) {
            selectedVideos.add(selectedVideoUris.get(position));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                CommonFunctions.moveToTrash(this, selectedVideoUris.get(position));
                deleteItem(position);
            }
        }
        selectedVideoUris.removeAll(selectedVideos);
        selectedPositions.clear();
        fileAdapter.setSelectedPositions(selectedPositions);
        fileAdapter.notifyDataSetChanged();
        updateActionModeTitle();

        if (selectedVideoUris.isEmpty()) binding.tvNoFilesYet.setVisibility(View.VISIBLE);
        else binding.tvNoFilesYet.setVisibility(View.GONE);

    }

    private void updateActionModeTitle() {
        int selectedCount = selectedPositions.size();
        actionMode.setTitle(String.valueOf(selectedCount));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                Uri selectedFileUri = data.getData();
                Uri newImageUri = CommonFunctions.saveFile(this, selectedFileUri, FileType.AUDIO);
                int position = selectedVideoUris.size();
                selectedVideoUris.add(newImageUri);
                fileAdapter.notifyItemInserted(position);
                if (selectedVideoUris.isEmpty()) binding.tvNoFilesYet.setVisibility(View.VISIBLE);
                else binding.tvNoFilesYet.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
