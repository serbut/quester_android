package com.sergeybutorin.quester.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

/**
 * Created by sergeybutorin on 11/12/2017.
 */

public class Point {
    private UUID uuid;
    private LatLng coordinates;

    public Point() {}

    public Point(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public Point(UUID uuid, LatLng coordinates) {
        this.uuid = uuid;
        this.coordinates = coordinates;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }
}
