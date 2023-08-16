package com.example.calculater.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.calculater.R;
import com.example.calculater.adapters.VideoAdapter;
import com.example.calculater.databinding.ActivityVideosBinding;
import com.example.calculater.utils.FileManagerUtil;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VideosActivity extends BaseActivity<ActivityVideosBinding> {

    private static final int REQUEST_PICK_VIDEO = 1;
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 41;
    private static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 42;

    boolean add = false;
    private FileManagerUtil filerManagerUtil;

    private List<Uri> selectedVideoUris;
    private VideoAdapter videoAdapter;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                try {
                    if (result.getData() != null) {
                        if (result.getData().getClipData() != null) {
                            // Multiple videos were selected
                            ClipData clipData = result.getData().getClipData();
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                Uri selectedVideoUri = clipData.getItemAt(i).getUri();
                                Uri fileUri = filerManagerUtil.saveDataToFile(selectedVideoUri, FileManagerUtil.STORAGE_PATH_VIDEO);
                                int position = selectedVideoUris.size();
                                selectedVideoUris.add(fileUri);
                                videoAdapter.notifyItemInserted(position);
                            }
                        } else if (result.getData().getData() != null) {
                            // Single video was selected
                            Uri selectedVideoUri = result.getData().getData();
                            Uri fileUri = filerManagerUtil.saveDataToFile(selectedVideoUri, FileManagerUtil.STORAGE_PATH_VIDEO);
                            int position = selectedVideoUris.size();
                            selectedVideoUris.add(fileUri);
                            videoAdapter.notifyItemInserted(position);
                        }
                    }
                } catch (Exception e) {

                }
            }
        }
    });
    private ActionMode actionMode;
    private Set<Integer> selectedPositions;
    private Uri newImageUri = null;
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
                // restoreVideo((newImageUri));
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
        filerManagerUtil = FileManagerUtil.getInstance(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        } else {
            /// getExternalStorageDirectory path
            try {
                selectedVideoUris = new ArrayList<>();
                // Add files to the selectedTrash list
                String path = Environment.getExternalStorageDirectory().toString() + "/Download/.Calculator/video";
                Log.d("Files", "Path: " + path);
                File directory = new File(path);
                File[] files = directory.listFiles();
                Log.d("Files", "Size: " + files.length);

                for (File file : files) {
                    Log.d("Files", "FileName: " + file.getName());
                    selectedVideoUris.add(Uri.fromFile(file));
                }
            } catch (Exception e) {
                //Toast.makeText(this, "No Videos Add Videos ", Toast.LENGTH_SHORT).show();
            }
            videoAdapter = new VideoAdapter(this, selectedVideoUris);
            binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            binding.recyclerView.setAdapter(videoAdapter);
        }

        binding.arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
//                Intent intent = new Intent(VideosActivity.this, HomeActivity.class);
//                startActivity(intent);
//                finish();
            }
        });

        binding.videoAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkPermissionForReadExtertalStorage()) {
                    if (checkPermissionForWriteExternalStorage()) {
                        add = true;
                        Log.e("permission ", "given");
                    } else {
                        add = false;
                        Log.e("permission ", " write not given");
                        try {
                            requestPermissionForWriteExtertalStorage();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    add = false;
                    Log.e("permission ", "not given");
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
//                    startActivityForResult(intent, REQUEST_PICK_VIDEO);
                }
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

    public void deleteItem(int position) {
        Uri fileUri = selectedVideoUris.get(position);
        // Get the file path from the Uri
        String filePath = fileUri.getPath();
        // Create a File object using the file path
        File fileToDelete = new File(filePath);
        if (fileToDelete.exists()) {
            // Delete the file
            if (fileToDelete.delete()) {
                Log.d("Files", "File deleted successfully");
            } else {
                Log.d("Files", "Failed to delete file");
            }
        } else {
            Log.d("Files", "File does not exist");
        }
    }

    private void deleteSelectedVideos() {
        List<Uri> selectedVideos = new ArrayList<>();
        for (int position : selectedPositions) {
            if (((selectedVideoUris.size() - 1) >= position) && position != -1) {
                selectedVideos.add(selectedVideoUris.get(position));
                MoveFile(selectedVideoUris.get(position));
                deleteItem(position);
            }
        }
        selectedVideoUris.removeAll(selectedVideos);
        selectedPositions.clear();
        videoAdapter.setSelectedPositions(selectedPositions);
        videoAdapter.notifyDataSetChanged();
        updateActionModeTitle();
    }

    private void restoreSelectedVideos() {
        List<Uri> selectedVideos = new ArrayList<>();
        for (int position : selectedPositions) {
            if ((selectedVideoUris.size() - 1) >= position && position != -1) {
                selectedVideos.add(selectedVideoUris.get(position));
                filerManagerUtil.restoreVideo(selectedVideoUris.get(position));
                deleteItem(position);
            }
        }
        selectedVideoUris.removeAll(selectedVideos);
        selectedPositions.clear();
        videoAdapter.setSelectedPositions(selectedPositions);
        updateActionModeTitle();
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
                        // Multiple videos were selected
                        ClipData clipData = data.getClipData();
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri selectedVideoUri = clipData.getItemAt(i).getUri();
                            filerManagerUtil.saveDataToFile(selectedVideoUri, FileManagerUtil.STORAGE_PATH_VIDEO);
                            int position = selectedVideoUris.size();
                            selectedVideoUris.add(selectedVideoUri);
                            videoAdapter.notifyItemInserted(position);
                        }
                    } else if (data.getData() != null) {
                        // Single video was selected
                        Uri selectedVideoUri = data.getData();
                        int position = selectedVideoUris.size();
                        selectedVideoUris.add(selectedVideoUri);
                        videoAdapter.notifyItemInserted(position);
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    private void MoveFile(Uri fileUri) {
        if (fileUri == null) {
            Toast.makeText(this, "Invalid image URI", Toast.LENGTH_SHORT).show();
            return;
        }
        String folderName = "Trash";
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), ".Calculator" + File.separator + folderName);
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                Toast.makeText(this, "Folder created successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to create the folder", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String sourceFileName = getFileName(fileUri);
        String sourceFilePath = fileUri.getPath();
        if (sourceFilePath == null) {
            Toast.makeText(this, "Source file path is null", Toast.LENGTH_SHORT).show();
            return;
        }
        // Create a temporary file with the original file name
        File sourceFile = new File(sourceFilePath);
        File destinationFile = new File(folder, sourceFileName);

        try {
            // Open an InputStream for the selected file
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            // Create an OutputStream for the destination file
            OutputStream outputStream = new FileOutputStream(destinationFile);
            // Copy the contents from the InputStream to the OutputStream
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            // Close the streams
            inputStream.close();
            outputStream.close();

            // Delete the source file
            if (sourceFile.exists()) {
                deleteFiles(getFileUri(sourceFile));
            } else {
                Toast.makeText(this, "Source file doesn't exist", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to restore the file", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String fileName = null;
        try {
            if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                ContentResolver contentResolver = getContentResolver();
                Cursor cursor = contentResolver.query(uri, null, null, null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        } catch (Exception e) {

        }
        try {


            if (fileName == null) {
                fileName = uri.getLastPathSegment();
            }

        } catch (Exception e) {

        }
        return fileName;
    }

    private Uri getFileUri(File file) {
        Context context = getApplicationContext();
        String authority = context.getPackageName() + ".activities.TrashActivity";
        // Get the Uri for the file using FileProvider
        return FileProvider.getUriForFile(context, authority, file);
    }

    private void deleteFiles(Uri fileUri) {
        // Check if the app has permission to delete files
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            // Request the MANAGE_EXTERNAL_STORAGE permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }
        // Delete the file from media store
        ContentResolver contentResolver = getContentResolver();
        int rowsDeleted = contentResolver.delete(fileUri, null, null);
        if (rowsDeleted > 0) {
            // File deleted successfully
            //Toast.makeText(this, "Video save successfully", Toast.LENGTH_SHORT).show();
        } else {
            // Failed to delete the file
            Toast.makeText(this, "Failed to save the video", Toast.LENGTH_SHORT).show();
        }
    }

    private String getDataColumn(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }

}