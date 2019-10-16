package com.example.activity_map;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Weightdata extends RealmObject{
    //@PrimaryKey
    private int weightdata;

    public int getweight() {
        return weightdata;
    }

    public void setweight(int weightdata) {
        this.weightdata = weightdata;
    }

}
