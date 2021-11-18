package com.example.pizzaapp.models;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

public class Marker {
    private String id;
    private String name;
    private String content;
    private GeoPoint geoPoint;
    private Bitmap bitmap;
    private boolean hasNewImage = false;

    public Marker(String name, String content, GeoPoint geoPoint) {
        this.name = name;
        this.content = content;
        this.geoPoint = geoPoint;
    }

    public Marker(String id, String name, String content, GeoPoint geoPoint) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.geoPoint = geoPoint;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public boolean hasNewImage() {
        return hasNewImage;
    }

    public void setHasNewImage(boolean hasNewImage) {
        this.hasNewImage = hasNewImage;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
