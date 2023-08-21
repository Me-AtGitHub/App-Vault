package com.example.calculater.utils;

import java.io.File;

public class SavedData {

    public File file;
    public FileType fileType;

    public SavedData(File file, FileType fileType) {
        this.file = file;
        this.fileType = fileType;
    }

}
