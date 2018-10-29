package com.example.data;

import android.app.Activity;
import android.content.Context;

public class DataHistory {

    //每周的身体状况
    private float[] temperatureWeek;
    private float[] weightWeek;
    private float[] heartBeatWeek;
    private float[] bloodPressureWeek;
    private float[] bloodFatWeek;

    //每月的身体状况
    private float[] temperatureMonth;
    private float[] weightMonth;
    private float[] heartBeatMonth;
    private float[] bloodPressureMonth;
    private float[] bloodFatMonth;

    public float[] getTemperatureWeek() {
        return temperatureWeek;
    }

    public void setTemperatureWeek(float[] temperatureWeek) {
        this.temperatureWeek = temperatureWeek;
    }

    public float[] getWeightWeek() {
        return weightWeek;
    }

    public void setWeightWeek(float[] weightWeek) {
        this.weightWeek = weightWeek;
    }

    public float[] getHeartBeatWeek() {
        return heartBeatWeek;
    }

    public void setHeartBeatWeek(float[] heartBeatWeek) {
        this.heartBeatWeek = heartBeatWeek;
    }

    public float[] getBloodPressureWeek() {
        return bloodPressureWeek;
    }

    public void setBloodPressureWeek(float[] bloodPressureWeek) {
        this.bloodPressureWeek = bloodPressureWeek;
    }

    public float[] getBloodFatWeek() {
        return bloodFatWeek;
    }

    public void setBloodFatWeek(float[] bloodFatWeek) {
        this.bloodFatWeek = bloodFatWeek;
    }

    public float[] getTemperatureMonth() {
        return temperatureMonth;
    }

    public void setTemperatureMonth(float[] temperatureMonth) {
        this.temperatureMonth = temperatureMonth;
    }

    public float[] getWeightMonth() {
        return weightMonth;
    }

    public void setWeightMonth(float[] weightMonth) {
        this.weightMonth = weightMonth;
    }

    public float[] getHeartBeatMonth() {
        return heartBeatMonth;
    }

    public void setHeartBeatMonth(float[] heartBeatMonth) {
        this.heartBeatMonth = heartBeatMonth;
    }

    public float[] getBloodPressureMonth() {
        return bloodPressureMonth;
    }

    public void setBloodPressureMonth(float[] bloodPressureMonth) {
        this.bloodPressureMonth = bloodPressureMonth;
    }

    public float[] getBloodFatMonth() {
        return bloodFatMonth;
    }

    public void setBloodFatMonth(float[] bloodFatMonth) {
        this.bloodFatMonth = bloodFatMonth;
    }
}
