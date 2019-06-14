package com.example.activity_map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import io.realm.Realm;
import io.realm.RealmResults;

public class ScheduleActivity extends AppCompatActivity {
    private Realm mrealm;//bd
    private ListView mlistview;//listview

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        mrealm = Realm.getDefaultInstance();//db in
        mlistview=(ListView)findViewById(R.id.listview);
        RealmResults<Schedule> schedules=mrealm.where(Schedule.class).findAll();
        Adapter adapter=new Adapter(schedules);
        mlistview.setAdapter(adapter);
        mlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Schedule schedule=(Schedule)parent.getItemAtPosition(position);
            }
        });


    }
    public void onDestroy() {
        super.onDestroy();
        mrealm.close();
        ;//データベースに保存
    }

}

