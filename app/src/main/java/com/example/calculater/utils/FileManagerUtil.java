package com.example.calculater.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileManagerUtil {

    private static final String TAG = "FileManagerUtils";
    public static String STORAGE_PATH_PHOTO = ".Calculator" + File.separator + "photos";
    public static String STORAGE_PATH_VIDEO = ".Calculator" + File.separator + "video";
    public static String STORAGE_PATH_RESTORED_PHOTO = ".Calculator" + File.separator + "photos";
    public static String STORAGE_PATH_RESTORED_VIDEO = ".Calculator" + File.separator + "video";
    private final Context mContext;

    private FileManagerUtil(Context context) {
        mContext = context;
    }

    public static FileManagerUtil getInstance(Context context) {
        return new FileManagerUtil(context);
    }

    public List<Uri> getSavedPhotos() {
        List<Uri> savedFiles = new ArrayList<Uri>();
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        } else {
            try {
                String path = Environment.getExternalStorageDirectory().toString() + "/Download/" + STORAGE_PATH_PHOTO;
                File directory = new File(path);
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        savedFiles.add(Uri.fromFile(file));
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "getSavedPhotos: " + e.getLocalizedMessage());
            }
        }
        return savedFiles;
    }

    private String getDataColumn(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }

    public Uri getFileUri(File file) {
        String authority = mContext.getPackageName() + ".activities.TrashActivity";
        // Get the Uri for the file using FileProvider
        return FileProvider.getUriForFile(mContext, authority, file);
    }

    public boolean deleteFiles(Uri fileUri) {

        // Check for permission MANAGE_EXTERNAL_STORAGE to delete files
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).setData(Uri.parse("package:" + mContext.getPackageName()));
            mContext.startActivity(intent);
            return false;
        }

        // Delete the file from media store
        ContentResolver contentResolver = mContext.getContentResolver();
        int rowsDeleted = contentResolver.delete(fileUri, null, null);
        if (rowsDeleted > 0) {
            Log.d(TAG, "deleteFiles: file deleted!!!!");
        } else {
            Log.d(TAG, "deleteFiles: Failed to save the video");
        }
        return true;
    }

    @SuppressLint("Range")
    public String getFileName(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver contentResolver = mContext.getContentResolver();
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

    @Nullable
    public Uri saveDataToFile(Uri selectedImageUri, String savePathType) {

        Uri newImageUri = null;

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), savePathType);

        if (!folder.exists()) {
            if (folder.mkdirs()) {
                Log.d(TAG, "folder created --> " + savePathType);
            } else {
                Log.d(TAG, "Unable to create folder --> " + savePathType);
                return null;
            }
        }

        String sourceFilePath = getDataColumn(selectedImageUri);
        if (sourceFilePath == null) {
            return null;
        }

        File sourceFile = new File(sourceFilePath);
        File tempFile = new File(folder.getPath() + File.separator + sourceFile.getName());

        if (tempFile.exists()) {
            boolean isDeleted = tempFile.delete();
        }

        try {
            InputStream inputStream = mContext.getContentResolver().openInputStream(selectedImageUri);
            OutputStream outputStream = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                outputStream = Files.newOutputStream(tempFile.toPath());
            } else new FileOutputStream(tempFile);

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
                boolean fileDeleted = deleteFiles(getFileUri(sourceFile));
                if (!fileDeleted)
                    return null;
            } else {
                Log.d(TAG, "saveDataToFile: Source file doesn't exist");
            }

            notifyResolver(selectedImageUri);

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "saveDataToFile exception : " + e.getMessage());
        }
        Log.d(TAG, "saveDataToFile: path " + newImageUri);
        return newImageUri;
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


    public void restoreFile(Uri fileUri) {

        if (fileUri == null) {
            Toast.makeText(mContext, "Invalid image URI", Toast.LENGTH_SHORT).show();
            return;
        }

        String folderName = "RestoredPhotos";
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folderName);

        if (!folder.exists()) {
            if (folder.mkdirs()) {
                Toast.makeText(mContext, "Folder created successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Failed to create the folder", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String sourceFilePath = getFileName(fileUri);
        if (sourceFilePath == null) {
            Toast.makeText(mContext, "Source file path is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a temporary file with the original file name
        File sourceFile = new File(sourceFilePath);
        File destinationFile = new File(folder, sourceFile.getName());
        try {
            // Open an InputStream for the selected file
            InputStream inputStream = mContext.getContentResolver().openInputStream(fileUri);
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
            Toast.makeText(mContext, "File restored and moved successfully", Toast.LENGTH_SHORT).show();
            notifyResolver(fileUri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "restoreFile: Failed " + e.getLocalizedMessage());
            Toast.makeText(mContext, "Failed to restore the file", Toast.LENGTH_SHORT).show();
        }

    }


    public void restoreVideo(Uri fileUri) {

        if (fileUri == null) {
            Toast.makeText(mContext, "Invalid video URI", Toast.LENGTH_SHORT).show();
            return;
        }
        String folderName = "RestoredVideos";
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folderName);

        if (!folder.exists()) {
            if (folder.mkdirs()) {
                Toast.makeText(mContext, "Folder created successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Failed to create the folder", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String sourceFilePath = getFileName(fileUri);
        if (sourceFilePath == null) {
            Toast.makeText(mContext, "Source file path is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a temporary file with the original file name
        File sourceFile = new File(sourceFilePath);
        File destinationFile = new File(folder, sourceFile.getName());
        try {
            // Open an InputStream for the selected file
            InputStream inputStream = mContext.getContentResolver().openInputStream(fileUri);
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
            Toast.makeText(mContext, "File restored and moved successfully", Toast.LENGTH_SHORT).show();

            notifyResolver(fileUri);
        } catch (IOException e) {
            Log.d(TAG, "restoreVideo: Failed" + e.getLocalizedMessage());
            e.printStackTrace();
            Toast.makeText(mContext, "Failed to restore the file", Toast.LENGTH_SHORT).show();
        }


    }


    public void moveFile(Uri fileUri) {
        if (fileUri == null) {
            Toast.makeText(mContext, "Invalid image URI", Toast.LENGTH_SHORT).show();
            return;
        }
        String folderName = "Trash";
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), ".Calculator" + File.separator + folderName);

        if (!folder.exists()) {
            if (folder.mkdirs()) {
                Toast.makeText(mContext, "Folder created successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Failed to create the folder", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String sourceFileName = getFileName(fileUri);
        String sourceFilePath = fileUri.getPath();
        if (sourceFilePath == null) {
            Toast.makeText(mContext, "Source file path is null", Toast.LENGTH_SHORT).show();
            return;
        }
        // Create a temporary file with the original file name
        File sourceFile = new File(sourceFilePath);
        File destinationFile = new File(folder, sourceFileName);

        try {
            // Open an InputStream for the selected file
            InputStream inputStream = mContext.getContentResolver().openInputStream(fileUri);
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
                Toast.makeText(mContext, "Source file doesn't exist", Toast.LENGTH_SHORT).show();
            }
            notifyResolver(fileUri);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Failed to restore the file", Toast.LENGTH_SHORT).show();
        }
    }

    private void notifyResolver(Uri fileUri) {

        MediaScannerConnection.scanFile(mContext, new String[]{fileUri.toString()}, null, (path, uri) -> {
            Log.i("ExternalStorage", "Scanned " + path + ":");
            Log.i("ExternalStorage", "-> uri=" + uri);
        });


    }

}
