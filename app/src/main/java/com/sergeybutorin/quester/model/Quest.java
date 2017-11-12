package com.sergeybutorin.quester.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;


public class Quest {
    private String name = "unknown";
    private LinkedList<LatLng> positions = new LinkedList<>();

    public Quest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addPosition(LatLng position) {
        positions.add(position);
    }

    public LinkedList<LatLng> getPositions() {
        return positions;
    }
}
