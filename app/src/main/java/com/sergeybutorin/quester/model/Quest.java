package com.sergeybutorin.quester.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.LinkedList;

public class Quest extends QuestBase {
    private String title = "Очень классный маршрут";
    private String description = "Это очень классный маршрут! Советую посетить всем!";
    private LinkedList<LatLng> points = new LinkedList<>();
    private final LinkedList<Marker> markers = new LinkedList<>();

    public Quest() {
    }

    public Quest(int id,
                 int version,
                 String title,
                 String description) {
        super(id, version);
        this.title = title;
        this.description = description;
    }

    public Quest(String title,
                 String description,
                 LinkedList<LatLng> points) {
        this.title = title;
        this.description = description;
        this.points = points;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addPoint(LatLng point) {
        points.add(point);
    }

    public void setPoints(LinkedList<LatLng> points) {
        this.points = points;
    }


    public LinkedList<LatLng> getPoints() {
        return points;
    }

    public void addMarkers(Marker marker) {
        markers.add(marker);
    }

    public LinkedList<Marker> getMarkers() {
        return markers;
    }

    public void clear() {
        points.clear();
        markers.clear();
    }
}
