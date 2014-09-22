package com.obcerver.iamhere.model;

/**
 * Custom Geofence Object
 * @author Cary Zeyue Chen
 */
public class CGeofence {
    private String id, address;
    private double latitude, longitude, radius;

    public String getId() {
        return this.id;
    }
    public void setId(String pId) {
        this.id = pId;
    }

    public String getAddress() {
        return this.address;
    }
    public void setAddress(String pAddr) {
        this.address = pAddr;
    }

    public double getLatitude() {
        return this.latitude;
    }
    public void setLatitude(double pLatitude) {
        this.latitude = pLatitude;
    }

    public double getLongitude() {
        return this.longitude;
    }
    public void setLongitude(double pLongitude) {
        this.longitude = pLongitude;
    }

    public double getRadius() {
        return this.radius;
    }
    public void setRadius(double pRadius) {
        this.radius = pRadius;
    }
}
