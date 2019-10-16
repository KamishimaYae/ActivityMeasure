package com.example.activity_map;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);

        Realm.init(this);

        RealmConfiguration realmConfig=new RealmConfiguration.Builder().build();
        Realm.deleteRealm(realmConfig);
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

        /*
        RealmQuery<Weightdata> query = mrealm.where(Weightdata.class);
        RealmResults<Weightdata> result = query.findAll();
        System.out.println(result.get(0).getweight());

         */


        button.setOnClickListener(new View.OnClickListener() {


            public void onClick(View v) {
                // エディットテキストのテキストを取得
                number  = editText.getText().toString();
                //System.out.println("********"+number+"******");

                mrealm.beginTransaction();
                Weightdata data=mrealm.createObject(Weightdata.class);
                data.setweight(Integer.parseInt(number));
                mrealm.commitTransaction();


               Double weight = 1.05*Double.parseDouble(number);
               Double weight2 = 3.5*Double.parseDouble(number)*1.05;
               Double weight3 = 8.3*Double.parseDouble(number)*1.05;
               Double weight4 = 4.0*Double.parseDouble(number)*1.05;
               Double weight5 = 2.0*Double.parseDouble(number)*1.05;
               Double weight6 = 0*Double.parseDouble(number);
                // 取得したテキストを TextView に張り付ける
                stillView.setText("stiil:"+weight.toString());
                walkingView.setText("walk:"+weight2.toString());
                runningView.setText("run:"+weight3.toString());
                bicycleView.setText("bicycle:"+weight4.toString());
                vehicleView.setText("vehicle:"+weight5.toString());
                unknownView.setText("unknown:"+weight6.toString());
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
