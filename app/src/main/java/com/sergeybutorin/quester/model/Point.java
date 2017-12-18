package com.sergeybutorin.quester.model;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by sergeybutorin on 11/12/2017.
 */

public class Point implements Serializable {
    private UUID uuid;
    private double latitude;
    private double longitude;

    public Point() {}

    public Point(LatLng coordinates) {
        this.latitude = coordinates.latitude;
        this.longitude = coordinates.longitude;
    }

    public Point(UUID uuid, LatLng coordinates) {
        this.uuid = uuid;
        this.latitude = coordinates.latitude;
        this.longitude = coordinates.longitude;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public LatLng getCoordinates() {
        return new LatLng(latitude, longitude);
    }
}
