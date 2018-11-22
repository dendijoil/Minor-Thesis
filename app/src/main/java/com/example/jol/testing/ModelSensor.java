package com.example.jol.testing;

public class ModelSensor {
    private String x, y, z, time, currSpeed;
    private double latitude, longitude;

    public ModelSensor(String time, String x, String y, String z, String currSpeed, double latitude, double longitude) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.time = time;
        this.currSpeed = currSpeed;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getCurrSpeed() {
        return currSpeed;
    }

    public void setCurrSpeed(String currSpeed) {
        this.currSpeed = currSpeed;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getZ() {
        return z;
    }

    public void setZ(String z) {
        this.z = z;
    }
}
