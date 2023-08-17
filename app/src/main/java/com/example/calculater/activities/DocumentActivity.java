package com.example.calculater.activities;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculater.R;
import com.example.calculater.adapters.DocumentAdapter;
import com.example.calculater.databinding.ActivityDocumentBinding;
import com.example.calculater.utils.FileHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DocumentActivity extends BaseActivity<ActivityDocumentBinding> {
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 41;
    private static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 42;
    private final int FILE_REQUEST_CODE = 1;
    ImageView arow, addDocument;
    boolean add = false;
    private RecyclerView recyclerView;
    private List<Uri> selectedVideoUris;
    private Uri newDocUri = null;
    private DocumentAdapter fileAdapter;
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
    ActivityDocumentBinding getLayout() {
        return ActivityDocumentBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        arow = findViewById(R.id.arrow);
        addDocument = findViewById(R.id.documentAdd);
        recyclerView = findViewById(R.id.recyclerViewDocument);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        } else {
            /// getExternalStorageDirectory path
            try {
                selectedVideoUris = new ArrayList<>();
                // Add files to the selectedTrash list
                String path = Environment.getExternalStorageDirectory().toString() + "/Download/.Calculator/document";
                Log.d("Files", "Path: " + path);
                File directory = new File(path);
                File[] files = directory.listFiles();
                Log.d("Files", "Size: " + files.length);
                for (File file : files) {
                    Log.d("Files", "FileName: " + file.getName());
                    selectedVideoUris.add(Uri.fromFile(file));
                }
            } catch (Exception e) {
                // Toast.makeText(this, "No File Add File ", Toast.LENGTH_SHORT).show();
            }
            fileAdapter = new DocumentAdapter(selectedVideoUris, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(fileAdapter);

            if (selectedVideoUris.isEmpty()) binding.tvNoFilesYet.setVisibility(View.VISIBLE);
            else binding.tvNoFilesYet.setVisibility(View.GONE);
        }

        arow.setOnClickListener(v -> onBackPressed());

        addDocument.setOnClickListener(v -> {
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
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("application/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, FILE_REQUEST_CODE);
            }
        });

        // onCreate
        // menu button
        selectedPositions = new HashSet<>();
        try {
            fileAdapter.setOnItemClickListener(new DocumentAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    if (actionMode != null) {
                        toggleSelection(position);
                    } else {
                        Uri videoUri = selectedVideoUris.get(position);
                        //openVideoPlayer(videoUri);
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
                MoveFile(selectedVideoUris.get(position));
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

    @RequiresApi(api = Build.VERSION_CODES.Q)
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

    private void restoreSelectedVideos() {
        List<Uri> selectedVideos = new ArrayList<>();
        for (int position : selectedPositions) {
            selectedVideos.add(selectedVideoUris.get(position));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                restoreVideo(selectedVideoUris.get(position));
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

    // OnResult Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                Uri selectedFileUri = data.getData();


                if (selectedFileUri != null) {
                    saveDataToFile(selectedFileUri);
                    Log.d("saveDataToFile", "onActivityResult: uri" + newDocUri);
                    int position = selectedVideoUris.size();
                    Log.d("saveDataToFile", "onActivityResult: position" + position);
                    selectedVideoUris.add(newDocUri);
                    fileAdapter.notifyItemInserted(position);

                    if (selectedVideoUris.isEmpty())
                        binding.tvNoFilesYet.setVisibility(View.VISIBLE);
                    else binding.tvNoFilesYet.setVisibility(View.GONE);

                    Log.d("saveDataToFile", "onActivityResult: position" + selectedVideoUris.size());


                } else {
                    Toast.makeText(this, "Unable to get file path", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {

            }
        }

    }


    private void saveDataToFile(Uri selectedVideoUri) {
        String folderName = ".Calculator" + File.separator + "document";
        if (getFileExtension(selectedVideoUri.getPath()).equals("jpeg") || getFileExtension(selectedVideoUri.getPath()).equals("png")) {
            folderName = folderName + File.separator + "photos";
        } else if (getFileExtension(selectedVideoUri.getPath()).equals("mp4") || getFileExtension(selectedVideoUri.getPath()).equals("mkv")) {
            folderName = folderName + File.separator + "videos";
        } else if (getFileExtension(selectedVideoUri.getPath()).equals("DOC") || getFileExtension(selectedVideoUri.getPath()).equals("PDF")) {
            folderName = folderName + File.separator + "document";
        } else if (getFileExtension(selectedVideoUri.getPath()).equals("AUDIO") || getFileExtension(selectedVideoUri.getPath()).equals("mp4")) {
            folderName = folderName + File.separator + "document";
        }
        // Create the folder in the Downloads directory if it doesn't exist
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folderName);
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                Toast.makeText(this, "Folder created successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to create the folder", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Get the actual file path from the URI
        String sourceFilePath = FileHelper.getRealPathFromURI(this, selectedVideoUri);
        Log.d("actualFilePath", "saveDataToFile: " + sourceFilePath);
        if (sourceFilePath == null) {
            Toast.makeText(this, "Source path is null", Toast.LENGTH_SHORT).show();
            return;
        }

        File sourceFile = new File(sourceFilePath);
        Log.d("actualFilePath", "saveDataToFile: file exists " + sourceFile.exists());


        File tempFile = new File(folder.getPath() + File.separator + sourceFile.getName().replace(":", "_"));
        if (tempFile.exists()) {
            tempFile.delete();
        }

        try {

            // Copy the content of the selected image to the temporary file
            InputStream inputStream = getContentResolver().openInputStream(selectedVideoUri);
            OutputStream outputStream = Files.newOutputStream(tempFile.toPath());

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();

            // Delete the source file
            if (sourceFile.exists()) {
                newDocUri = getFileUri(tempFile);
                deleteFiles(getFileUri(sourceFile));
            } else {
                Toast.makeText(this, "Source file doesn't exist", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.d("saveDataToFile", "saveDataToFile: " + e.getMessage());
            Toast.makeText(this, "Failed to move the file", Toast.LENGTH_SHORT).show();
        }

        Log.d("saveDataToFile", "saveDataToFile: Done ");
    }

    private void restoreVideo(Uri videoUri) {
        if (videoUri == null) {
            Toast.makeText(this, "Invalid document URI", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.e("Uri is ", videoUri.getPath());
        Log.e("Uri extension ", getFileExtension(videoUri.getPath()));

        String folderName = "RestoredDocument";
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
        Log.d("sourceFilePath", "restoreVideo: " + sourceFilePath);
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

            Toast.makeText(this, "document restored and moved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to restore the document", Toast.LENGTH_SHORT).show();
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

    private void MoveFile(Uri fileUri) {
        if (fileUri == null) {
            Toast.makeText(this, "Invalid document URI", Toast.LENGTH_SHORT).show();
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
        String sourceFilePath = FileHelper.getRealPathFromURI(this, fileUri);

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
//            File deleted successfully
            Toast.makeText(this, "Document save successfully", Toast.LENGTH_SHORT).show();
        } else {
            // Failed to delete the file
            Toast.makeText(this, "Failed to save the document", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFilePathFromUri(Uri uri) {
        String filePath = null;
        if (uri != null) {
            if (uri.getScheme().equals("content")) {
                DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
                if (documentFile != null && documentFile.exists()) {
                    filePath = documentFile.getUri().getPath();
                }
            } else if (uri.getScheme().equals("file")) {
                filePath = uri.getPath();
            }
        }
        return filePath;
    }


    private String getDataColumn(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }


    public void getAllMusicFiles() {
        try {
            String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DURATION};

            // Display audios in alphabetical order based on their display name.
            Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));


                    Log.d("getAllMusicFiles", "getAllMusicFiles: " + "Id : => " + id + "Title : => " + title + "duration : => " + duration + "artist : => " + artist);
                } while (cursor.moveToNext());
                cursor.close();
            }

        } catch (Exception e) {
            Log.d("getAllMusicFiles", "getAllMusicFiles: " + e.getLocalizedMessage());
        }

    }

    protected void getAllDocuments() {
        try {

            String[] projection = {MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.MIME_TYPE, MediaStore.Files.FileColumns.DATE_ADDED, MediaStore.Files.FileColumns.DATE_MODIFIED, MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.TITLE, MediaStore.Files.FileColumns.SIZE,};

            String mime = "application/pdf";

            String whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mime + "')" + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + " LIKE 'application/vnd%'";
            String orderBy = MediaStore.Files.FileColumns.SIZE + " DESC";
            Cursor cursor = getContentResolver().query(MediaStore.Files.getContentUri("external"), projection, whereClause, null, orderBy);

            if (cursor != null) {
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                int mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);
                int addedCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED);
                int modifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);
                int nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
                int sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);

                if (cursor.moveToFirst()) {
                    do {
                        Uri fileUri = Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), cursor.getString(idCol));
                        String mimeType = cursor.getString(mimeCol);
                        long dateAdded = cursor.getLong(addedCol);
                        long dateModified = cursor.getLong(modifiedCol);

                        Log.d("getAllDocuments", "getAllDocuments: " + fileUri + " " + mimeType + " " + dateAdded + " " + dateModified);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
        }
    }


}