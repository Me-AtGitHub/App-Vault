package com.example.calculater.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.calculater.R;
import com.example.calculater.adapters.VideoAdapter;
import com.example.calculater.databinding.ActivityVideosBinding;
import com.example.calculater.utils.CommonFunctions;
import com.example.calculater.utils.FileType;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VideosActivity extends BaseActivity<ActivityVideosBinding> {

    private static final int REQUEST_PICK_VIDEO = 1;
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 41;
    private static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 42;

    boolean add = false;

    private List<Uri> selectedVideoUris = new ArrayList<>();
    private VideoAdapter videoAdapter;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                try {
                    if (result.getData() != null) {
                        if (result.getData().getClipData() != null) {
                            ClipData clipData = result.getData().getClipData();
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                Uri selectedVideoUri = clipData.getItemAt(i).getUri();
                                Uri fileUri = CommonFunctions.saveFile(getBaseContext(), selectedVideoUri, FileType.VIDEO);
                                int position = selectedVideoUris.size();
                                selectedVideoUris.add(fileUri);
                                videoAdapter.notifyItemInserted(position);
                            }
                        } else if (result.getData().getData() != null) {
                            Uri selectedVideoUri = result.getData().getData();
                            Uri fileUri = CommonFunctions.saveFile(getBaseContext(), selectedVideoUri, FileType.VIDEO);
                            int position = selectedVideoUris.size();
                            selectedVideoUris.add(fileUri);
                            videoAdapter.notifyItemInserted(position);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    });

    private ActionMode actionMode;
    private Set<Integer> selectedPositions;
    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
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
            videoAdapter.setSelectedPositions(selectedPositions);
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

    // open video player by intent
    private void openVideoPlayer(Uri videoUri) {
        PlayerView playerView = new PlayerView(this);
        setContentView(playerView);
        SimpleExoPlayer player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    @Override
    ActivityVideosBinding getLayout() {
        return ActivityVideosBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            selectedVideoUris.addAll(CommonFunctions.getFiles(FileType.VIDEO));
        }

        videoAdapter = new VideoAdapter(this, selectedVideoUris);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        binding.recyclerView.setAdapter(videoAdapter);

        if (selectedVideoUris.isEmpty())
            binding.tvNoFilesYet.setVisibility(View.VISIBLE);
        else
            binding.tvNoFilesYet.setVisibility(View.GONE);

        binding.arrow.setOnClickListener(v -> {
            onBackPressed();
        });

        binding.videoAdd.setOnClickListener(v -> {

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
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                launcher.launch(intent);
            }
        });

        selectedPositions = new HashSet<>();
        try {
            videoAdapter.setOnItemClickListener(new VideoAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    if (actionMode != null) {
                        toggleSelection(position);
                    } else {
                        Uri videoUri = selectedVideoUris.get(position);
                        openVideoPlayer(videoUri);
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
        videoAdapter.setSelectedPositions(selectedPositions);

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
            if (((selectedVideoUris.size() - 1) >= position) && position != -1) {
                selectedVideos.add(selectedVideoUris.get(position));
                CommonFunctions.moveToTrash(this, selectedVideoUris.get(position));
                CommonFunctions.deleteFile(this, selectedVideoUris.get(position));
            }
        }
        selectedVideoUris.removeAll(selectedVideos);
        selectedPositions.clear();
        videoAdapter.setSelectedPositions(selectedPositions);
        videoAdapter.notifyDataSetChanged();
        updateActionModeTitle();

        if (selectedVideoUris.isEmpty())
            binding.tvNoFilesYet.setVisibility(View.VISIBLE);
        else
            binding.tvNoFilesYet.setVisibility(View.GONE);

    }

    private void restoreSelectedVideos() {
        List<Uri> selectedVideos = new ArrayList<>();
        for (int position : selectedPositions) {
            if ((selectedVideoUris.size() - 1) >= position && position != -1) {
                selectedVideos.add(selectedVideoUris.get(position));
                CommonFunctions.recoverFile(this, selectedVideoUris.get(position), FileType.VIDEO);
                CommonFunctions.deleteFile(this, selectedVideoUris.get(position));
            }
        }
        selectedVideoUris.removeAll(selectedVideos);
        selectedPositions.clear();
        videoAdapter.setSelectedPositions(selectedPositions);
        updateActionModeTitle();

        if (selectedVideoUris.isEmpty())
            binding.tvNoFilesYet.setVisibility(View.VISIBLE);
        else
            binding.tvNoFilesYet.setVisibility(View.GONE);
    }

    private void updateActionModeTitle() {
        try {
            int selectedCount = selectedPositions.size();
            actionMode.setTitle(String.valueOf(selectedCount));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_VIDEO && resultCode == RESULT_OK) {
            try {
                if (data != null) {
                    if (data.getClipData() != null) {
                        ClipData clipData = data.getClipData();
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri selectedVideoUri = clipData.getItemAt(i).getUri();
                            CommonFunctions.saveFile(this, selectedVideoUri, FileType.VIDEO);
                            int position = selectedVideoUris.size();
                            selectedVideoUris.add(selectedVideoUri);
                            videoAdapter.notifyItemInserted(position);
                        }

                    } else if (data.getData() != null) {
                        Uri selectedVideoUri = data.getData();
                        int position = selectedVideoUris.size();
                        selectedVideoUris.add(selectedVideoUri);
                        videoAdapter.notifyItemInserted(position);
                    }

                    binding.tvNoFilesYet.setVisibility(View.GONE);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}