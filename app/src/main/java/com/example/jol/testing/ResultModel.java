package com.example.jol.testing;

public class ResultModel {
    private String time, avgX, avgY, avgZ, threshX, threshY, threshZ, hasil;
    private double latitude, longitude;

    public ResultModel() {
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAvgX() {
        return avgX;
    }

    public void setAvgX(String avgX) {
        this.avgX = avgX;
    }

    public String getAvgY() {
        return avgY;
    }

    public void setAvgY(String avgY) {
        this.avgY = avgY;
    }

    public String getAvgZ() {
        return avgZ;
    }

    public void setAvgZ(String avgZ) {
        this.avgZ = avgZ;
    }

    public String getThreshX() {
        return threshX;
    }

    public void setThreshX(String threshX) {
        this.threshX = threshX;
    }

    public String getThreshY() {
        return threshY;
    }

    public void setThreshY(String threshY) {
        this.threshY = threshY;
    }

    public String getThreshZ() {
        return threshZ;
    }

    public void setThreshZ(String threshZ) {
        this.threshZ = threshZ;
    }

    public String getHasil() {
        return hasil;
    }

    public void setHasil(String hasil) {
        this.hasil = hasil;
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
}
