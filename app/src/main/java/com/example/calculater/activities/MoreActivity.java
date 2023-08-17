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
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.calculater.R;
import com.example.calculater.adapters.MoreAdapter;
import com.example.calculater.databinding.ActivityMoreBinding;
import com.example.calculater.utils.CommonFunctions;
import com.example.calculater.utils.FileType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MoreActivity extends BaseActivity<ActivityMoreBinding> {


    private static final int FILE_REQUEST_CODE = 1;
    private List<Uri> selectedVideoUris = new ArrayList<>();
    private MoreAdapter fileAdapter;
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

    @Override
    ActivityMoreBinding getLayout() {
        return ActivityMoreBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fileAdapter = new MoreAdapter(selectedVideoUris);
        binding.moreRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.moreRecyclerView.setAdapter(fileAdapter);
        onclickButton();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        getSavedData();
        AdapterFunction();
    }

    private void onclickButton() {

        binding.arrow.setOnClickListener(v -> {onBackPressed();});

        binding.moreAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("*/*");
            String[] mimetypes = {"image/*", "video/*", "audio/*", "application/*"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            startActivityForResult(intent, FILE_REQUEST_CODE);
        });

    }

    private void getSavedData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            try {
                selectedVideoUris.addAll(CommonFunctions.getFiles(FileType.IMAGE));
                selectedVideoUris.addAll(CommonFunctions.getFiles(FileType.VIDEO));
                selectedVideoUris.addAll(CommonFunctions.getFiles(FileType.AUDIO));
                selectedVideoUris.addAll(CommonFunctions.getFiles(FileType.DOCUMENT));
                int count = selectedVideoUris.size();
                Log.d("getSavedData", "getSavedData: " + count);
                fileAdapter.notifyDataSetChanged();
//                fileAdapter.notifyItemRangeInserted(0, count);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        selectedPositions = new HashSet<>();
    }

    private void AdapterFunction() {
        try {
            fileAdapter.setOnItemClickListener(new MoreAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    if (actionMode != null) {
                        toggleSelection(position);
                    } else {
                        Uri videoUri = selectedVideoUris.get(position);
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
            Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
        }

    }

    // menu function
    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
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

    private void deleteSelectedVideos() {
        List<Uri> selectedVideos = new ArrayList<>();
        for (int position : selectedPositions) {
            selectedVideos.add(selectedVideoUris.get(position));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                CommonFunctions.moveToTrash(this, selectedVideoUris.get(position));
            }
        }
        selectedVideoUris.removeAll(selectedVideos);
        selectedPositions.clear();
        fileAdapter.setSelectedPositions(selectedPositions);
        fileAdapter.notifyDataSetChanged();
        updateActionModeTitle();
    }

    private void restoreSelectedVideos() {
        List<Uri> selectedVideos = new ArrayList<>();
        for (int position : selectedPositions) {
            selectedVideos.add(selectedVideoUris.get(position));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileType fileType = CommonFunctions.getFileType(this, selectedVideoUris.get(position));
                if (fileType != null) {
                    CommonFunctions.recoverFile(this, selectedVideoUris.get(position), fileType);
                    CommonFunctions.deleteFile(this, selectedVideoUris.get(position));
                }
            }
        }
        selectedVideoUris.removeAll(selectedVideos);
        selectedPositions.clear();
        fileAdapter.setSelectedPositions(selectedPositions);
        fileAdapter.notifyDataSetChanged();
        updateActionModeTitle();
    }

    private void updateActionModeTitle() {
        int selectedCount = selectedPositions.size();
        actionMode.setTitle(String.valueOf(selectedCount));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                FileType fileType = CommonFunctions.getFileType(this, selectedFileUri);
                if (fileType != null) CommonFunctions.saveFile(this, selectedFileUri, fileType);
            }
        }
    }

}