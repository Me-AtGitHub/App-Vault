package com.example.calculater;

import android.Manifest;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculater.activities.DocumentActivity;
import com.example.calculater.activities.DownloadActivity;
import com.example.calculater.activities.GalleryActivity;
import com.example.calculater.activities.HomeActivity;
import com.example.calculater.activities.MoreActivity;
import com.example.calculater.activities.MusicActivity;
import com.example.calculater.activities.VideosActivity;
import com.example.calculater.adapters.FileAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileManager extends AppCompatActivity {
    // Destination folder path
    private static final int FILE_REQUEST_CODE = 1;
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 41;
    private static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 42;
    ImageView arow, idfile;
    ImageView photo, video, document, music, download, more;
    TextView textView;
    boolean add = false;
    private List<Uri> selectedVideoUris;
    private Uri newImageUri = null;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
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
                restoreVideo((newImageUri));
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
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void requestPermissionForWriteExtertalStorage() {
        try {
            Log.e("permission ", "requested");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);
        arow = findViewById(R.id.arrow);
        idfile = findViewById(R.id.fileAdd);
        recyclerView = findViewById(R.id.recyclerView);
        photo = findViewById(R.id.photos);
        video = findViewById(R.id.videos);
        document = findViewById(R.id.documents);
        music = findViewById(R.id.music);
        download = findViewById(R.id.downloads);
        more = findViewById(R.id.more);


        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FileManager.this, GalleryActivity.class);
                startActivity(intent);
            }
        });
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FileManager.this, VideosActivity.class);
                startActivity(intent);
            }
        });
        document.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FileManager.this, DocumentActivity.class);
                startActivity(intent);
            }
        });
        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FileManager.this, MusicActivity.class);
                startActivity(intent);
            }
        });
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FileManager.this, DownloadActivity.class);
                startActivity(intent);
            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FileManager.this, MoreActivity.class);
                startActivity(intent);
            }
        });

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // back to home button
        arow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                /*Intent intent = new Intent(FileManager.this, HomeActivity.class);
                startActivity(intent);
                finish();*/

            }
        });
        idfile.setOnClickListener(new View.OnClickListener() {
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
                 /*   Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, 1);*/
                    openFileManager();
                }
                //openFileManager();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        } else {
            /// getExternalStorageDirectory path
            try {
                selectedVideoUris = new ArrayList<>();
                // Add files to the selectedTrash list
                String path = Environment.getExternalStorageDirectory().toString() + "/Download/.Calculator/file";
                Log.d("Files", "Path: " + path);
                File directory = new File(path);
                File[] files = directory.listFiles();
                Log.d("Files", "Size: " + files.length);

                for (File file : files) {
                    Log.d("Files", "FileName: " + file.getName());
                    selectedVideoUris.add(Uri.fromFile(file));
                }
            } catch (Exception e) {
                //Toast.makeText(this, "No File Add File ", Toast.LENGTH_SHORT).show();
            }
            fileAdapter = new FileAdapter(selectedVideoUris, this);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            recyclerView.setAdapter(fileAdapter);

        }
        selectedPositions = new HashSet<>();
        try {
            fileAdapter.setOnItemClickListener(new FileAdapter.OnItemClickListener() {
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
            //Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
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
    }

    private void updateActionModeTitle() {
        int selectedCount = selectedPositions.size();
        actionMode.setTitle(String.valueOf(selectedCount));
    }

    private void openFileManager() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("*/*"); // Set the MIME type to select any file
        //intent = Intent.createChooser(intent, "Choose a file");
        startActivityForResult(intent, FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();
            String filePath = getDataColumn(selectedFileUri);
            if (filePath != null) {
                // Handle the file path as needed
                saveDataToFile(selectedFileUri);
                selectedFileUri = newImageUri;
                selectedVideoUris.add(selectedFileUri); // Add the file URI to your list
                fileAdapter.notifyDataSetChanged(); // Notify the adapter that the data has changed
            } else {
                Toast.makeText(this, "Unable to get file path", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void saveDataToFile(Uri selectedVideoUri) {
        String folderName = ".Calculator" + File.separator + "download";

        if (getFileExtension(selectedVideoUri.getPath()).equals("jpeg") || getFileExtension(selectedVideoUri.getPath()).equals("png")) {
            folderName = folderName + File.separator + "photos";
        } else if (getFileExtension(selectedVideoUri.getPath()).equals("mp4") || getFileExtension(selectedVideoUri.getPath()).equals("mkv")) {
            folderName = folderName + File.separator + "videos";
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
        String sourceFilePath = getDataColumn(selectedVideoUri);
        if (sourceFilePath == null) {
            Toast.makeText(this, "Source file path is null", Toast.LENGTH_SHORT).show();
            return;
        }
        // Create a temporary file with the original file name
        File sourceFile = new File(sourceFilePath);
        File tempFile = new File(folder.getPath() + File.separator + sourceFile.getName());
        if (tempFile.exists()) {
            tempFile.delete();
        }
        try {
            // Copy the content of the selected image to the temporary file
            InputStream inputStream = getContentResolver().openInputStream(selectedVideoUri);
            OutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();

            // Delete the source file
            if (sourceFile.exists()) {
                newImageUri = getFileUri(tempFile);
                deleteFiles(getFileUri(sourceFile));
            } else {
                Toast.makeText(this, "Source file doesn't exist", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to move the file", Toast.LENGTH_SHORT).show();
        }
    }

    private void restoreVideo(Uri videoUri) {
        if (videoUri == null) {
            Toast.makeText(this, "Invalid video URI", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.e("Uri is ", videoUri.getPath());
        Log.e("Uri extension ", getFileExtension(videoUri.getPath()));

        String folderName = "RestoredVideos";
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

            Toast.makeText(this, "Video restored and moved successfully", Toast.LENGTH_SHORT).show();
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

    private void MoveFile(Uri fileUri) {
        if (fileUri == null) {
            Toast.makeText(this, "Invalid image URI", Toast.LENGTH_SHORT).show();
            return;
        }
        String folderName = "Trash";
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), ".CalculatorFile" + File.separator + folderName);

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
        String authority = context.getPackageName() + ".TrashActivity";

        // Get the Uri for the file using FileProvider
        return FileProvider.getUriForFile(context, authority, file);
    }

    private void deleteFiles(Uri fileUri) {
        // Check if the app has permission to delete files
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            // Request the MANAGE_EXTERNAL_STORAGE permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    .setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }

        // Delete the file from media store
        ContentResolver contentResolver = getContentResolver();
        int rowsDeleted = contentResolver.delete(fileUri, null, null);
        if (rowsDeleted > 0) {
            // File deleted successfully
            Toast.makeText(this, "File deleted successfully", Toast.LENGTH_SHORT).show();
        } else {
            // Failed to delete the file
            Toast.makeText(this, "Failed to delete the file", Toast.LENGTH_SHORT).show();
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





