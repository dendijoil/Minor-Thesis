package com.example.jol.testing;

import android.os.Parcel;
import android.os.Parcelable;

public class ModelSensor implements Parcelable {
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

    protected ModelSensor(Parcel in) {
        x = in.readString();
        y = in.readString();
        z = in.readString();
        time = in.readString();
        currSpeed = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<ModelSensor> CREATOR = new Creator<ModelSensor>() {
        @Override
        public ModelSensor createFromParcel(Parcel in) {
            return new ModelSensor(in);
        }

        @Override
        public ModelSensor[] newArray(int size) {
            return new ModelSensor[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(x);
        dest.writeString(y);
        dest.writeString(z);
        dest.writeString(time);
        dest.writeString(currSpeed);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
