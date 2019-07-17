package com.example.activity_map;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Schedule extends RealmObject {
    //保存するための関数作成
    @PrimaryKey
    private long id;
    private String activity1;
    private Date date;
    private double latitude;
    private double longitude;
    private String locationname;
    //private String weather;
    public String getActivity1() {
        return activity1;
    }

    public void setActivity1(String activity1) {
        this.activity1 = activity1;//運動状態
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }//日付

    public void setDate(Date date) {
        this.date = date;
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
    public String getLocationname() {
        return locationname;
    }//住所

    public void setLocationname(String locationname) {
        this.locationname = locationname;
    }

    //public void setWeather(String weather){return weather;}


}



