package com.example.data;

public class DataNow {

    private String name;

    private int imageId;

    private float temperatureNow;
    private float heartBeatNow;
    private float bloodPressureNow;
    private float bloodFatNow;

    public float getTemperatureNow() {
        return temperatureNow;
    }

    public void setTemperatureNow(float temperatureNow) {
        this.temperatureNow = temperatureNow;
    }

    public float getHeartBeatNow() {
        return heartBeatNow;
    }

    public void setHeartBeatNow(float heartBeatNow) {
        this.heartBeatNow = heartBeatNow;
    }

    public float getBloodPressureNow() {
        return bloodPressureNow;
    }

    public void setBloodPressureNow(float bloodPressureNow) {
        this.bloodPressureNow = bloodPressureNow;
    }

    public float getBloodFatNow() {
        return bloodFatNow;
    }

    public void setBloodFatNow(float bloodFatNow) {
        this.bloodFatNow = bloodFatNow;
    }
}
