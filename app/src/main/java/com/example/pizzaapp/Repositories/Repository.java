package com.example.pizzaapp.Repositories;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.pizzaapp.R;
import com.example.pizzaapp.Updatable;
import com.example.pizzaapp.models.Marker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Repository {
    private static GoogleMap mGoogleMap;
    private static List<Marker> markers = new ArrayList<>();
    private static Marker currentMarker;
    private static Updatable caller;
    private static FirebaseFirestore db;
    private static FirebaseStorage storage;
    private static UUID uuid = UUID.randomUUID(); // world-wide unique (almost)

    public static void init(Context context) {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        caller = (Updatable)context;
    }

    public static void uploadMarker(String name, String content, double lat, double lng) {
        DocumentReference ref = db.collection("markers").document(uuid.toString());
        Marker marker = new Marker(uuid.toString(), name, content, new GeoPoint(lat, lng));
        ref.set(marker).addOnCompleteListener(obj -> {
            //markers.add(marker);
            System.out.println("Added new marker: " + marker.getId());
        }).addOnFailureListener(exception -> {
            System.out.println("Failed to add new marker " + exception);
        });
    }

    public static void downloadMarker(GoogleMap googleMap) {
        Repository.mGoogleMap = googleMap;

        db.collection("markers").addSnapshotListener((value,error) -> {
            if(error == null) {
                markers.clear();
                for(DocumentSnapshot snap: value.getDocuments()) {
                    if(snap.get("geoPoint") != null) {
                        String name = (String) snap.get("name");
                        String content = (String) snap.get("content");
                        GeoPoint geoPoint = (GeoPoint) snap.get("geoPoint");
                        LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                        Repository.mGoogleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(name)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pizza_marker)));
                        Marker marker = new Marker(snap.getId(), name, content, geoPoint);
                        markers.add(marker);
                    }
                }
                caller.update(null);
            }else {
                System.out.println("Error reading from Firestore: " + error);
            }
        });
    }

    public static Marker getCurrentMarker() {
        return currentMarker;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void setCurrentMarker(String name) {
        List<Marker> result = markers.stream()
                .filter(item -> item.getName().equals(name))
                .collect(Collectors.toList());
        System.out.println("Hvad er result: " + result.get(0).getName());
        Repository.currentMarker = result.get(0);
    }

    public static void updateMarker(String newName, String newContent) {
        System.out.println("Hvad er dette ID: "  + currentMarker.getId());
        currentMarker.setName(newName);
        currentMarker.setContent(newContent);
        DocumentReference ref = db.collection("markers").document(currentMarker.getId());
        if(currentMarker.hasNewImage()) {
            uploadBitmapToCurrentMarker(currentMarker.getBitmap());
        }
        // ref.update only updates the mentioned field, were as ref.set removes everything not mentioned
        ref.update("name", currentMarker.getName(), "content", currentMarker.getContent()).addOnCompleteListener(obj -> {
            System.out.println("Updated marker: " + currentMarker.getId());
        }).addOnFailureListener(exception -> {
            System.out.println("Failed to update marker " + exception);
        });
    }

    public static void deleteMarker() {
        storage.getReference(currentMarker.getId()).delete();
        db.collection("markers").document(currentMarker.getId()).delete();
        System.out.println("Deleted marker: " + currentMarker.getId());
    }

    public static void uploadBitmapToCurrentMarker(Bitmap bitmap) {
        StorageReference ref = storage.getReference(currentMarker.getId()); // new reference
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        ref.putBytes(baos.toByteArray()).addOnCompleteListener(snap -> {
            System.out.println("Image uploaded. " + snap);
        }).addOnFailureListener(exception -> {
            System.out.println("Failed to upload. " + exception);
        });
    }

    public static void downloadBitmapForCurrentMarker(Updatable caller) {
        StorageReference ref = storage.getReference(currentMarker.getId());
        int max = 1024 * 1024;
        ref.getBytes(max).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            currentMarker.setBitmap(bitmap);
            caller.update(true);
        }).addOnFailureListener(exception -> {
            System.out.println("No bitmap in DB for this marker");
        });
    }
}
