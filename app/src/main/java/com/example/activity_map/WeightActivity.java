package com.example.activity_map;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    public  double sweight,wweight,rweight,bweight,vweight,uweight;
    public  double vehiclecount,bicyclecount,walkingcount,runningcount,stillcount,unknowncount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);
        Intent intent = getIntent();
        vehiclecount = intent.getIntExtra("Vehicle", 0);
        bicyclecount = intent.getIntExtra("Bicycle", 0);
        walkingcount = intent.getIntExtra("Walking", 0);
        runningcount = intent.getIntExtra("Running", 0);
        stillcount = intent.getIntExtra("Stiil", 0);
        unknowncount = intent.getIntExtra("Unknown", 0);

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
        System.out.println("********"+result.get(0).getweight()+"******");
        editText.setText(String.valueOf(result.get(0).getweight()));


        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // エディットテキストのテキストを取得
                number  = editText.getText().toString();
                //System.out.println("********"+number+"******");

                RealmQuery<Weightdata> query = mrealm.where(Weightdata.class);
                RealmResults<Weightdata> result = query.findAll();

                mrealm.beginTransaction();
                Weightdata data=mrealm.createObject(Weightdata.class);
                data.setweight(Integer.parseInt(number));
                result.get(0).setweight(Integer.parseInt(number));
                mrealm.commitTransaction();


               sweight = 1.05*Double.parseDouble(number)*(stillcount/60);
               wweight = 3.5*Double.parseDouble(number)*1.05*(walkingcount/60);
               rweight = 8.3*Double.parseDouble(number)*1.05*(runningcount/60);
               bweight = 4.0*Double.parseDouble(number)*1.05*(bicyclecount/60);
               vweight = 2.0*Double.parseDouble(number)*1.05*(vehiclecount/60);
               uweight = 0*Double.parseDouble(number)*unknowncount;
                // 取得したテキストを TextView に張り付ける
                stillView.setText("stiil:"+sweight);
                walkingView.setText("walk:"+wweight);
                runningView.setText("run:"+rweight);
                bicycleView.setText("bicycle:"+bweight);
                vehicleView.setText("vehicle:"+vweight);
                unknownView.setText("unknown:"+uweight);
            }
        });



        /*
        mrealm.beginTransaction();
        Weightdata data=mrealm.createObject(Weightdata.class);
        data.setweight(Integer.parseInt(number));
        mrealm.commitTransaction();
         */


        ReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
