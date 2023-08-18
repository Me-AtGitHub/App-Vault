package com.example.calculater.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.example.calculater.R;
import com.example.calculater.adapters.TrashAdapter;
import com.example.calculater.databinding.ActivityTrashBinding;
import com.example.calculater.utils.CommonFunctions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrashActivity extends BaseActivity<ActivityTrashBinding> {
    TrashAdapter trashAdapter;
    List<Uri> selectedTrash;
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
                List<Uri> files = CommonFunctions.getFiles(null);
                selectedTrash.addAll(files);
                trashAdapter.notifyItemRangeInserted(0, files.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (selectedTrash.isEmpty())
                binding.tvNoFilesYet.setVisibility(View.VISIBLE);
            else binding.tvNoFilesYet.setVisibility(View.GONE);
        }

        selectedPositions = new HashSet<>();

        try {
            trashAdapter.setOnItemClickListener(new TrashAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    if (actionMode != null) {
                        toggleSelection(position);
                    } else {
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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void deleteItem(int position) {
        Uri fileUri = selectedTrash.get(position);
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
            selectedVideos.add(selectedTrash.get(position));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                deleteItem(position);
                //Toast.makeText(this, "File deleted successfully", Toast.LENGTH_SHORT).show();

            }
        }
        selectedTrash.removeAll(selectedVideos);
        selectedPositions.clear();
        trashAdapter.setSelectedPositions(selectedPositions);
        trashAdapter.notifyDataSetChanged();
        updateActionModeTitle();

        if (selectedTrash.isEmpty())
            binding.tvNoFilesYet.setVisibility(View.VISIBLE);
        else binding.tvNoFilesYet.setVisibility(View.GONE);

    }

    private void restoreSelectedVideos() {
        List<Uri> selectedVideos = new ArrayList<>();
        for (int position : selectedPositions) {
            selectedVideos.add(selectedTrash.get(position));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                restoreTrash(selectedTrash.get(position));
                deleteItem(position);
            }
        }
        selectedTrash.removeAll(selectedVideos);
        selectedPositions.clear();
        trashAdapter.setSelectedPositions(selectedPositions);
        trashAdapter.notifyDataSetChanged();
        updateActionModeTitle();

        if (selectedTrash.isEmpty())
            binding.tvNoFilesYet.setVisibility(View.VISIBLE);
        else binding.tvNoFilesYet.setVisibility(View.GONE);

    }

    private void updateActionModeTitle() {
        int selectedCount = selectedPositions.size();
        actionMode.setTitle(String.valueOf(selectedCount));
    }

    private void restoreTrash(Uri videoUri) {
        if (videoUri == null) {
            Toast.makeText(this, "Invalid video URI", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.e("Uri is ", videoUri.getPath());
        Log.e("Uri extension ", getFileExtension(videoUri.getPath()));

        String folderName = "RestoredTrash";
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folderName);
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                Toast.makeText(this, "Folder created successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to create the folder", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String sourceFilePath = getFileName(videoUri);

        if (sourceFilePath == null) {
            Toast.makeText(this, "Source file path is null", Toast.LENGTH_SHORT).show();
            return;
        }
        // Create a temporary file with the original file name
        File sourceFile = new File(sourceFilePath);
        File destinationFile = new File(folder, sourceFile.getName());

        try {
            // Open an InputStream for the selected video file
            InputStream inputStream = getContentResolver().openInputStream(videoUri);
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

            Toast.makeText(this, " restored and moved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to restore the video", Toast.LENGTH_SHORT).show();
        }
    }

    public String getFileExtension(String filePath) {
        String extension = "";
        if (filePath != null && !filePath.isEmpty()) {
            File file = new File(filePath);
            String fileName = file.getName();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
                extension = fileName.substring(dotIndex + 1);
            }
        }
        return extension;
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String fileName = null;
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
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }

}