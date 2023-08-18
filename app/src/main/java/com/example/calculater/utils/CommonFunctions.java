package com.example.calculater.utils;

import static com.example.calculater.utils.FileHelper.getFileName;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class CommonFunctions {

    public static List<Uri> getFiles(@Nullable FileType fileType) {

        List<Uri> list = new ArrayList<Uri>();
        try {

            String badePath = Environment.getExternalStorageDirectory().toString() + "/Download/";
            String additionalPath = "";
            if (fileType == null) {
                additionalPath = Constants.TRASH_PATH;
            } else {
                switch (fileType) {
                    case IMAGE:
                        additionalPath = Constants.IMAGE_PATH;
                        break;
                    case AUDIO:
                        additionalPath = Constants.AUDIO_PATH;
                        break;
                    case VIDEO:
                        additionalPath = Constants.VIDEO_PATH;
                        break;
                    case DOCUMENT:
                        additionalPath = Constants.DOCUMENT_PATH;
                        break;
                    default:
                        additionalPath = Constants.TRASH_PATH;
                        break;
                }
            }

            String path = badePath + additionalPath;

            File directory = new File(path);
            File[] files = directory.listFiles();
            if (files != null) for (File file : files)
                list.add(Uri.fromFile(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("CommonFunctions", "getFiles: " + list);
        return list;
    }

    public static Uri saveFile(Context context, Uri selectedVideoUri, FileType fileType) {

        Uri newFileUri = null;
        String folderName = "";
        switch (fileType) {
            case AUDIO:
                folderName = Constants.AUDIO_PATH;
                break;
            case IMAGE:
                folderName = Constants.IMAGE_PATH;
                break;
            case VIDEO:
                folderName = Constants.VIDEO_PATH;
                break;
            case DOCUMENT:
                folderName = Constants.DOCUMENT_PATH;
                break;
            default:
                folderName = "";
                break;
        }

        // Create the folder in the Downloads directory if it doesn't exist
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folderName);
        if (!folder.exists()) {
            if (!folder.mkdirs()) return null;
        }

        String fileName = getFileName(selectedVideoUri, context);
        if (fileName == null) return null;

        String sourceFilePath = FileHelper.getRealPathFromURI(context, selectedVideoUri);
        if (sourceFilePath == null) return null;

        // Create a temporary file with the original file name
        File sourceFile = new File(sourceFilePath);
        File tempFile = new File(folder.getPath() + File.separator + sourceFile.getName());
        if (tempFile.exists()) {
            tempFile.delete();
        }
        try {
            // Copy the content of the selected image to the temporary file
            InputStream inputStream = context.getContentResolver().openInputStream(selectedVideoUri);
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
                try {
                    newFileUri = CommonFunctions.getFileUri(context, tempFile);
                    CommonFunctions.deleteFile(context, CommonFunctions.getFileUri(context, sourceFile));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        notifyResolver(context, selectedVideoUri);
        notifyResolver(context, newFileUri);
        return newFileUri;
    }

    public static void deleteFile(Context context, Uri fileUri) {
        try {
            checkManagePermission(context);
            ContentResolver contentResolver = context.getContentResolver();
            int rowsDeleted = contentResolver.delete(fileUri, null, null);
//        if (rowsDeleted < 0) // operation failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        notifyResolver(context, fileUri);
    }

    public static void checkManagePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        }
    }

    public static void moveToTrash(Context context, Uri fileUri) {

        if (fileUri == null) return;

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Constants.TRASH_PATH);

        if (!folder.exists()) if (!folder.mkdirs()) return;

        String sourceFileName = getFileName(fileUri, context);
        String sourceFilePath = fileUri.getPath();
        if (sourceFilePath == null) return;

        // Create a temporary file with the original file name
        File sourceFile = new File(sourceFilePath);
        File destinationFile = new File(folder, sourceFileName);
        try {
            // Open an InputStream for the selected file
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            // Create an OutputStream for the destination file
            OutputStream outputStream = new FileOutputStream(destinationFile);
            // Copy the contents from the InputStream to the OutputStream
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            // Delete the source file
            if (sourceFile.exists())
                CommonFunctions.deleteFile(context, CommonFunctions.getFileUri(context, sourceFile));

        } catch (IOException e) {
            e.printStackTrace();
        }

        notifyResolver(context, fileUri);
    }

    public static FileType getFileType(Context context, Uri uri) {
        FileType fileType = null;
        String mimeType = context.getContentResolver().getType(uri);
        Log.d("onBindViewHolder", "getFileType: " + mimeType);
        if (mimeType == null) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (extension != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
        }

        if (mimeType != null) {
            if (mimeType.startsWith("video")) {
                fileType = FileType.VIDEO;
            } else if (mimeType.startsWith("image")) {
                fileType = FileType.IMAGE;
            } else if (mimeType.startsWith("audio")) {
                fileType = FileType.AUDIO;
            } else if (mimeType.startsWith("application")) {
                fileType = FileType.DOCUMENT;
            } else {
                Toast.makeText(context, "File not supported", Toast.LENGTH_SHORT).show();
            }
        }

        return fileType;
    }

    public static void recoverFile(Context context, Uri selectedFileUri, FileType fileType) {
        Log.d("recoverFile", "recoverFile: URI => " + selectedFileUri);
        Log.d("recoverFile", "recoverFile: fileType => " + fileType.name());
        if (selectedFileUri == null) {
            return;
        }

        String folderName = "";

        switch (fileType) {
            case VIDEO:
                folderName = "RestoredVideo";
                break;
            case AUDIO:
                folderName = "RestoredAudio";
                break;
            case IMAGE:
                folderName = "RestoredImage";
                break;
            case DOCUMENT:
                folderName = "RestoredDocuments";
                break;
            default:
                folderName = "";
                break;
        }

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folderName);

        Log.d("recoverFile", "recoverFile: folder where recovered " + folder);
        if (!folder.exists()) {
            if (!folder.mkdirs()) return;
        }

        String sourceFilePath = getFileName(selectedFileUri, context);
        if (sourceFilePath == null) return;

        File sourceFile = new File(sourceFilePath);
        File destinationFile = new File(folder, sourceFile.getName());

        Log.d("recoverFile", "recoverFile: sourceFile " + sourceFile);
        Log.d("recoverFile", "recoverFile: destinationFile " + destinationFile);

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(selectedFileUri);
            OutputStream outputStream = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
            Log.d("recoverFile", "recoverFile: recover complete => ");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("recoverFile", "recoverFile: exception => " + e.getMessage());
        }

        notifyResolver(context, selectedFileUri);

    }

    public static Uri getFileUri(Context context, File file) {
        String authority = context.getPackageName() + Constants.PROVIDER_PATH;
        return FileProvider.getUriForFile(context, authority, file);
    }

    private static void notifyResolver(Context context, @Nullable Uri fileUri) {
        try {
            if (fileUri != null) {
                MediaScannerConnection.scanFile(context, new String[]{fileUri.toString()}, null, (path, uri) -> {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
