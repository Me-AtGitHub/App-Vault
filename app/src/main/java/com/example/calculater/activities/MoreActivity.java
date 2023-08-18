package com.example.calculater.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.calculater.R;
import com.example.calculater.adapters.MoreAdapter;
import com.example.calculater.databinding.ActivityMoreBinding;
import com.example.calculater.utils.CommonFunctions;
import com.example.calculater.utils.FileType;
import com.example.calculater.utils.SavedData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MoreActivity extends BaseActivity<ActivityMoreBinding> {


    private final List<SavedData> selectedVideoUris = new ArrayList<>();
    private final List<Uri> imageUris = new ArrayList<>();
    private final List<Uri> videoUris = new ArrayList<>();
    private final List<Uri> audioUris = new ArrayList<>();
    private final List<Uri> documentUris = new ArrayList<>();
    private MoreAdapter fileAdapter;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri selectedFileUri = result.getData().getData();
            if (selectedFileUri != null) {
                FileType fileType = CommonFunctions.getFileType(this, selectedFileUri);
                if (fileType != null) {
                    Uri uriToAdd = CommonFunctions.saveFile(this, selectedFileUri, fileType);
                    if (uriToAdd != null) {
                        addToList(uriToAdd, fileType);
                    }
                }
            }
        }
    });
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

        binding.arrow.setOnClickListener(v -> {
            onBackPressed();
        });

        binding.moreAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("*/*");
            String[] mimetypes = {"image/*", "video/*", "audio/*", "application/*"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            launcher.launch(intent);
        });

    }

    private void getSavedData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            try {

                imageUris.addAll(CommonFunctions.getFiles(FileType.IMAGE));
                videoUris.addAll(CommonFunctions.getFiles(FileType.VIDEO));
                audioUris.addAll(CommonFunctions.getFiles(FileType.AUDIO));
                documentUris.addAll(CommonFunctions.getFiles(FileType.DOCUMENT));

                for (Uri uri : imageUris) {
                    selectedVideoUris.add(new SavedData(uri, FileType.IMAGE));
                }
                for (Uri uri : videoUris) {
                    selectedVideoUris.add(new SavedData(uri, FileType.VIDEO));
                }
                for (Uri uri : audioUris) {
                    selectedVideoUris.add(new SavedData(uri, FileType.AUDIO));
                }
                for (Uri uri : documentUris) {
                    selectedVideoUris.add(new SavedData(uri, FileType.DOCUMENT));
                }

                int count = selectedVideoUris.size();
                fileAdapter.notifyItemRangeInserted(0, count);

                if (count > 0) {
                    binding.tvNoFilesYet.setVisibility(View.GONE);
                } else binding.tvNoFilesYet.setVisibility(View.VISIBLE);

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
                        Uri videoUri = selectedVideoUris.get(position).fileUri;
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
            selectedVideos.add(selectedVideoUris.get(position).fileUri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                CommonFunctions.moveToTrash(this, selectedVideoUris.get(position).fileUri);
            }
        }
        selectedVideoUris.removeAll(selectedVideos);
        selectedPositions.clear();
        fileAdapter.setSelectedPositions(selectedPositions);
        fileAdapter.notifyDataSetChanged();
        updateActionModeTitle();

        if (selectedVideoUris.size() > 0) {
            binding.tvNoFilesYet.setVisibility(View.GONE);
        } else binding.tvNoFilesYet.setVisibility(View.VISIBLE);

    }

    private void restoreSelectedVideos() {
        List<Uri> selectedVideos = new ArrayList<>();
        for (int position : selectedPositions) {
            selectedVideos.add(selectedVideoUris.get(position).fileUri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileType fileType = CommonFunctions.getFileType(this, selectedVideoUris.get(position).fileUri);
                if (fileType != null) {
                    CommonFunctions.recoverFile(this, selectedVideoUris.get(position).fileUri, fileType);
                    CommonFunctions.deleteFile(this, selectedVideoUris.get(position).fileUri);
                }
            }
        }
        selectedVideoUris.removeAll(selectedVideos);
        selectedPositions.clear();
        fileAdapter.setSelectedPositions(selectedPositions);
        fileAdapter.notifyDataSetChanged();
        updateActionModeTitle();

        if (selectedVideoUris.size() > 0) {
            binding.tvNoFilesYet.setVisibility(View.GONE);
        } else binding.tvNoFilesYet.setVisibility(View.VISIBLE);

    }

    private void updateActionModeTitle() {
        int selectedCount = selectedPositions.size();
        actionMode.setTitle(String.valueOf(selectedCount));
    }

    private void addToList(Uri uri, FileType fileType) {
        int positionToAdd = -1;
        switch (fileType) {
            case AUDIO:
                positionToAdd = (imageUris.size() + videoUris.size() + audioUris.size());
                audioUris.add(uri);
                break;
            case VIDEO:
                positionToAdd = (imageUris.size() + videoUris.size());
                videoUris.add(uri);
                break;
            case IMAGE:
                positionToAdd = (imageUris.size());
                imageUris.add(uri);
                break;
            case DOCUMENT:
                positionToAdd = (imageUris.size() + videoUris.size() + audioUris.size() + documentUris.size());
                documentUris.add(uri);
                break;
            default:
                break;
        }
        if (positionToAdd == -1) {
            selectedVideoUris.add(0, new SavedData(uri, fileType));
            fileAdapter.notifyItemInserted(0);
        } else if (positionToAdd >= 0) {
            selectedVideoUris.add(positionToAdd, new SavedData(uri, fileType));
            fileAdapter.notifyItemInserted(positionToAdd);
        }
    }
}