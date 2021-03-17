package com.nanosoft.mobilinq;

import com.google.gson.annotations.Expose;

public class UploadedProperty {
    @Expose
    private String  size;
    @Expose
    private String  description;

    public UploadedProperty(String size, String description) {
        this.size = size;
        this.description = description;
    }
}
