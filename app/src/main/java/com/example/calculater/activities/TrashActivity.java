package com.example.calculater.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.calculater.R;
import com.example.calculater.adapters.TrashAdapter;
import com.example.calculater.databinding.ActivityTrashBinding;
import com.example.calculater.utils.CommonFunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrashActivity extends BaseActivity<ActivityTrashBinding> {
    TrashAdapter trashAdapter;
    List<File> selectedTrash;
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
            trashAdapter.setSelectedPositions(selectedPositions);
        }
    };

    @Override
    ActivityTrashBinding getLayout() {
        return ActivityTrashBinding.inflate(getLayoutInflater());
    }
    //  select code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // id define
        binding.arrow.setOnClickListener(v -> onBackPressed());

        selectedTrash = new ArrayList<>();
        trashAdapter = new TrashAdapter(selectedTrash);
        binding.trashRecyclerView.setAdapter(trashAdapter);

        // check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            try {
                List<File> files = CommonFunctions.getFilesOne(null);
                selectedTrash.addAll(files);
                trashAdapter.notifyItemRangeInserted(0, files.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (selectedTrash.isEmpty()) binding.tvNoFilesYet.setVisibility(View.VISIBLE);
            else binding.tvNoFilesYet.setVisibility(View.GONE);
        }

        selectedPositions = new HashSet<>();

        try {
            trashAdapter.setOnItemClickListener(new TrashAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    if (actionMode != null) {
                        toggleSelection(position);
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
    }

    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        trashAdapter.setSelectedPositions(selectedPositions);

        if (selectedPositions.isEmpty()) {
            actionMode.finish();
        } else {
            actionMode.invalidate();
            updateActionModeTitle();
        }

    }

    private void deleteSelectedVideos() {
        List<File> selectedVideos = new ArrayList<>();
        for (int position : selectedPositions) {
            selectedVideos.add(selectedTrash.get(position));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                CommonFunctions.deleteFile(this, selectedTrash.get(position));
                selectedTrash.get(position).delete();
            }
        }
        selectedTrash.removeAll(selectedVideos);
        selectedPositions.clear();
        trashAdapter.setSelectedPositions(selectedPositions);
        trashAdapter.notifyDataSetChanged();
        updateActionModeTitle();

        if (selectedTrash.isEmpty()) binding.tvNoFilesYet.setVisibility(View.VISIBLE);
        else binding.tvNoFilesYet.setVisibility(View.GONE);

    }

    private void restoreSelectedVideos() {
        List<File> selectedVideos = new ArrayList<>();
        for (int position : selectedPositions) {
            selectedVideos.add(selectedTrash.get(position));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Uri fileUri = Uri.parse(selectedTrash.get(position).getAbsolutePath());
                CommonFunctions.recoverFile(this, fileUri, CommonFunctions.getFileType(this, fileUri));
                selectedTrash.get(position).delete();
            }
        }
        selectedTrash.removeAll(selectedVideos);
        selectedPositions.clear();
        trashAdapter.setSelectedPositions(selectedPositions);
        trashAdapter.notifyDataSetChanged();
        updateActionModeTitle();

        if (selectedTrash.isEmpty()) binding.tvNoFilesYet.setVisibility(View.VISIBLE);
        else binding.tvNoFilesYet.setVisibility(View.GONE);

    }

    private void updateActionModeTitle() {
        int selectedCount = selectedPositions.size();
        actionMode.setTitle(String.valueOf(selectedCount));
    }

}