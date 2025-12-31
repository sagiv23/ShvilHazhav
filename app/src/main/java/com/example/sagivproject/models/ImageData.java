package com.example.sagivproject.models;

//למחוק בסוף הפרויקט!
public class ImageData {
    private String id;
    private String base64;

    public ImageData() {}

    public ImageData(String id, String base64) {
        this.id = id;
        this.base64 = base64;
    }

    public String getId() {
        return id;
    }

    public String getBase64() {
        return base64;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }
}
