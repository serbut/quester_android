package com.sergeybutorin.quester.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.LinkedList;

public class Quest extends QuestBase {
    private String title = "unknownQuestName";
    private String description = "unknownQuestDescription";
    private LinkedList<LatLng> points = new LinkedList<>();
    private final LinkedList<Marker> markers = new LinkedList<>();

    public Quest() {
    }

    public Quest(String title,
                 LinkedList<LatLng> points) {
        this.title = title;
        this.points = points;
        this.description = "Loaded from DB";
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
