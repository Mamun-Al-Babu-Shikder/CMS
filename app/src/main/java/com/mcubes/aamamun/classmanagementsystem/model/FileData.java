package com.mcubes.aamamun.classmanagementsystem.model;

/**
 * Created by A.A.MAMUN on 3/3/2019.
 */

public class FileData {

    private String file_name, name_at_store, download_link, file_size, upload_date;

    public FileData() {
    }


    public FileData(String file_name, String file_size) {
        this.file_name = file_name;
        this.file_size = file_size;
    }

    public FileData(String download_link, String file_name, String file_size, String name_at_store, String upload_date) {
        this.download_link = download_link;
        this.file_name = file_name;
        this.file_size = file_size;
        this.name_at_store = name_at_store;
        this.upload_date = upload_date;
    }

    public String getDownload_link() {
        return download_link;
    }

    public void setDownload_link(String download_link) {
        this.download_link = download_link;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_size() {
        return file_size;
    }

    public void setFile_size(String file_size) {
        this.file_size = file_size;
    }

    public String getName_at_store() {
        return name_at_store;
    }

    public void setName_at_store(String name_at_store) {
        this.name_at_store = name_at_store;
    }

    public String getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(String upload_date) {
        this.upload_date = upload_date;
    }
}
