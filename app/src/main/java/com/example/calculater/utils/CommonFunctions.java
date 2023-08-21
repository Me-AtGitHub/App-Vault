package com.example.calculater.utils;

import static com.example.calculater.utils.FileHelper.copyFile;
import static com.example.calculater.utils.FileHelper.processUri;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.TextUtils;
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    public static List<File> getFilesOne(@Nullable FileType fileType) {

        List<File> list = new ArrayList<File>();
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
            if (files != null) {
                assert files != null;
                list.addAll(Arrays.asList(files));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("CommonFunctions", "getFiles: " + list);
        return list;
    }


    public static File saveFile(Context context, Uri selectedVideoUri, FileType fileType) {

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

        String sourceFilePath = getRealPathFromURI(context, selectedVideoUri);
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
        return sourceFile;
    }


    @Nullable
    public static String getRealPathFromURI(final Context context, final Uri uri) {
        String path = null;
        try {
            path = processUri(context, uri);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (TextUtils.isEmpty(path)) {
            path = copyFile(context, uri);
        }
        return path;
    }


    @SuppressLint("Range")
    public static String getFileName(Uri uri, Context context) {
        String fileName = null;
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            ContentResolver contentResolver = context.getContentResolver();
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


    public static void deleteFile(Context context, Uri fileUri) {
        try {
            checkManagePermission(context);
            Log.d("deleteFile", "deleteFile: => " + fileUri);
            File sourceFile = new File(fileUri.getPath());
            Log.d("deleteFile", "deleteFile: exists => " + sourceFile.exists());
            Log.d("deleteFile", "deleteFile: can read => " + sourceFile.canRead());
            Log.d("deleteFile", "deleteFile: can write => " + sourceFile.canWrite());
            Log.d("deleteFile", "deleteFile: can run => " + sourceFile.canExecute());
            ContentResolver contentResolver = context.getContentResolver();
            if (fileUri.getScheme().startsWith("content")) {
                int rowsDeleted = contentResolver.delete(fileUri, null, null);
                Log.d("deleteFile", "deleteFile: rowsDelete => " + rowsDeleted);
            } else {
                boolean rowsDeleted = new File(String.valueOf(fileUri)).delete();
                Log.d("deleteFile", "deleteFile: rowsDelete => " + rowsDeleted);
            }
        } catch (Exception e) {
            Log.d("deleteFile", "deleteFile: exception => " + e.getLocalizedMessage());
            Log.d("deleteFile", "deleteFile: exception cause => " + e.getCause());
            Log.d("deleteFile", "deleteFile: exception cause => " + e.getClass());
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

        if (selectedFileUri == null) {
            return;
        }

        String folderName = "";
        if (fileType != null) {
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
        } else folderName = "";

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folderName);

        if (!folder.exists()) {
            if (!folder.mkdirs()) return;
        }

        String sourceFileName = getRealPathFromURI(context, selectedFileUri);
        String sourceFilePath = selectedFileUri.getPath();
        if (sourceFilePath == null) return;

        File sourceFile = new File(sourceFilePath);
        File destinationFile = new File(folder, sourceFile.getName());

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(selectedFileUri);
            OutputStream outputStream = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                outputStream = Files.newOutputStream(destinationFile.toPath());
            } else outputStream = new FileOutputStream(sourceFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
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
                    Log.i("notifyResolver", "Scanned " + path + ":");
                    Log.i("notifyResolver", "-> uri=" + uri);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
