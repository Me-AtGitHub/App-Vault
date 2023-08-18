package com.example.calculater.utils;

import android.net.Uri;

public class SavedData {

    public Uri fileUri;
    public FileType fileType;

    public SavedData(Uri fileUri, FileType fileType) {
        this.fileUri = fileUri;
        this.fileType = fileType;
    }

}
