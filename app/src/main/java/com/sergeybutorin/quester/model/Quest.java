package com.sergeybutorin.quester.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.LinkedList;

public class Quest {
    private String name = "unknownQuestName";
    private String description = "unknownQuestDescription";
    private LinkedList<LatLng> positions = new LinkedList<>();
    private final LinkedList<Marker> markers = new LinkedList<>();

    public Quest() {
    }

    public Quest(String name,
                 LinkedList<LatLng> positions) {
        this.name = name;
        this.positions = positions;
        this.description = "Loaded from DB";

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addPosition(LatLng position) {
        positions.add(position);
    }

    public LinkedList<LatLng> getPositions() {
        return positions;
    }

    public void addMarkers(Marker marker) {
        markers.add(marker);
    }

    public LinkedList<Marker> getMarkers() {
        return markers;
    }

    public void clear() {
        positions.clear();
        markers.clear();
    }
}
