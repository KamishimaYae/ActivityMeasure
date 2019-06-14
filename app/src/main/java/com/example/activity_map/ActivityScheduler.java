package com.example.activity_map;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class ActivityScheduler extends Application {
    public void onCreate(){
        super.onCreate();
        Realm.init(this);
        RealmConfiguration realmConfig=new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(realmConfig);
        // Realm.deleteRealm(realmConfig);
    }
}
