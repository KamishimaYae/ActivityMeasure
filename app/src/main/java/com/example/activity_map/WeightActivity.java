package com.example.activity_map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class WeightActivity extends AppCompatActivity {

    private Realm mrealm;//bd
    private EditText editText;
    private TextView stillView;
    private TextView walkingView;
    private TextView runningView;
    private TextView bicycleView;
    private TextView vehicleView;
    private TextView unknownView;
    private String number;
    public  float sweight,wweight,rweight,bweight,vweight,uweight;
    public  float vehiclecount,bicyclecount,walkingcount,runningcount,stillcount,unknowncount;
    BroadcastReceiver broadcastReceiver1;
    MyIntentService myservice = new MyIntentService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);
        Intent intent = getIntent();
/*
        vehiclecount = intent.getIntExtra("Vehicle", 0);
        bicyclecount = intent.getIntExtra("Bicycle", 0);
        walkingcount = intent.getIntExtra("Walking", 0);
        runningcount = intent.getIntExtra("Running", 0);
        stillcount = intent.getIntExtra("Stiil", 0);
        unknowncount = intent.getIntExtra("Unknown", 0);
*/

        sweight = intent.getFloatExtra("StillKcal",0);
        wweight = intent.getFloatExtra("WalkingKcal",0);
        rweight = intent.getFloatExtra("RunningKcal",0);
        bweight = intent.getFloatExtra("BicycleKcal",0);
        vweight = intent.getFloatExtra("VehicleKcal",0);
        uweight = intent.getFloatExtra("UnknownKcal",0);


        broadcastReceiver1 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.COUNT_BROADCAST_DETECTED_ACTIVITY)) {
                    stillcount = intent.getFloatExtra("sTime", 0);
                    walkingcount = intent.getFloatExtra("wTime", 0);
                    runningcount = intent.getFloatExtra("rTime", 0);
                    bicyclecount = intent.getFloatExtra("bTime", 0);
                    vehiclecount = intent.getFloatExtra("vTime", 0);
                    unknowncount = intent.getFloatExtra("uTime", 0);

                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver1,
                new IntentFilter(Constants.COUNT_BROADCAST_DETECTED_ACTIVITY));



        Realm.init(this);

        RealmConfiguration realmConfig=new RealmConfiguration.Builder().build();
        //Realm.deleteRealm(realmConfig);
        Realm.setDefaultConfiguration(realmConfig);

        mrealm = Realm.getDefaultInstance();

        Button ReturnButton = findViewById(R.id.back_button);
        editText = findViewById(R.id.edit_text);
        stillView = findViewById(R.id.still_view);
        walkingView = findViewById(R.id.walking_view);
        runningView = findViewById(R.id.running_view);
        bicycleView = findViewById(R.id.bicycle_view);
        vehicleView = findViewById(R.id.vehicle_view);
        unknownView = findViewById(R.id.unknown_view);
        Button button = findViewById(R.id.measure);

        RealmQuery<Weightdata> query = mrealm.where(Weightdata.class);
        RealmResults<Weightdata> result = query.findAll();
        System.out.println("********" + result.get(0).getweight() + "******");
        System.out.println("********" + stillcount + "******");
        System.out.println("********" + myservice.stime + "******");
        editText.setText(String.valueOf(result.get(0).getweight()));


        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // エディットテキストのテキストを取得
                if(editText.getText().toString().isEmpty()) {
                    editText.setError("体重を入力してください");
                }
                else {
                    number = editText.getText().toString();

                    RealmQuery<Weightdata> query = mrealm.where(Weightdata.class);
                    RealmResults<Weightdata> result = query.findAll();

                    mrealm.beginTransaction();
                    Weightdata data = mrealm.createObject(Weightdata.class);
                    data.setweight(Integer.parseInt(number));
                    result.get(0).setweight(Integer.parseInt(number));
                    mrealm.commitTransaction();


                    sweight = (float)( 1.05 * Double.parseDouble(number) * (myservice.stime / 60));
                    wweight = (float) (3.5 * Double.parseDouble(number) * 1.05 * (myservice.wtime / 60));
                    rweight = (float) (8.3 * Double.parseDouble(number) * 1.05 * (myservice.rtime/ 60));
                    bweight = (float) (4.0 * Double.parseDouble(number) * 1.05 * (myservice.btime / 60));
                    vweight = (float) (2.0 * Double.parseDouble(number) * 1.05 * (myservice.vtime / 60));
                    uweight = (float) (0 * Double.parseDouble(number) * myservice.utime);
                    // 取得したテキストを TextView に張り付ける
                    stillView.setText("stiil:" + sweight);
                    walkingView.setText("walk:" + wweight);
                    runningView.setText("run:" + rweight);
                    bicycleView.setText("bicycle:" + bweight);
                    vehicleView.setText("vehicle:" + vweight);
                    unknownView.setText("unknown:" + uweight);
                }
            }
        });


        ReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (sweight > 0 || wweight > 0 ||rweight > 0 ||bweight > 0 ||vweight > 0 ||uweight > 0) {
                    intent.putExtra("StillKcal", sweight);
                    intent.putExtra("WalkingKcal", wweight);
                    intent.putExtra("RunningKcal", rweight);
                    intent.putExtra("BicycleKcal", bweight);
                    intent.putExtra("VehicleKcal", vweight);
                    intent.putExtra("UnknownKcal", uweight);
                }
                System.out.println("********" + sweight + "******");
                setResult(RESULT_OK, intent);

                finish();
            }
        });
    }



    public void onDestroy(){
        super.onDestroy();
        mrealm.close();
        ;//bd 終了
    }

}
