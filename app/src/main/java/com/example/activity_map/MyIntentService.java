package com.example.activity_map;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Geocoder;
import android.location.Location;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MyIntentService extends IntentService{

    private String filePath = "/testfileAct5.txt";//csv
    public float stime=0, wtime=0, rtime=0, btime=0, vtime=0, utime=0;
    public double lat = 0.0, lng = 0.0;
    private float sKcal=0, wKcal=0, rKcal=0, bKcal=0, vKcal=0, uKcal=0;
    public static String intentActivity, txtConfidence;
    private float sumkcal=0;
    public String label;
    BroadcastReceiver broadcastReceiver;
    MapsActivity map = new MapsActivity();

    public MyIntentService() {
        super("MyIntentService");
        Log.d("debug", "TestIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("debug", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("debug", "onStartCommand");

        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        filePath = Environment.getExternalStorageDirectory().getPath() + filePath;
        Log.d("debug", "onHandleIntent");
        /*
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
        for (DetectedActivity activity : detectedActivities) {
            Log.e(TAG, "Intent Detected activity: " + activity.getType() + ", " + activity.getConfidence());
            handleUserActivity(activity.getType(),activity.getConfidence());
        }

*/


        int count = 60*24+2;

        try {
            for(int i=0 ; i< count ; i++) {


                timecount();
                timesendbroadcast();
                setUpReadWriteExternalStorage();
                Thread.sleep(60000);
                Log.d("debug", "sleep: " + String.valueOf(i));

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    private void handleUserActivity(int type, int confidence) {
        // 状態表示

        label = getString(R.string.activity_unknown);
        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = getString(R.string.activity_in_vehicle);
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = getString(R.string.activity_on_bicycle);
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = getString(R.string.activity_walking);
                break;
            }
            case DetectedActivity.RUNNING: {
                label = getString(R.string.activity_running);
                break;
            }
            case DetectedActivity.STILL: {
                label = getString(R.string.activity_still);
                break;
            }
            case DetectedActivity.TILTING: {
                label = getString(R.string.activity_tilting);
                break;
            }
            case DetectedActivity.WALKING: {
                label = getString(R.string.activity_walking);
                break;
            }
            case DetectedActivity.UNKNOWN: {
                label = getString(R.string.activity_unknown);
                break;
            }
        }
        Log.d("MyIntent", "User activity: " + label + ", Confidence: " + confidence);//表示

        if (confidence > Constants.CONFIDENCE) {
            intentActivity = label;
            map.txtActivity = label;
            map.txtConfidence = "" + confidence;
        }
    }





    public  void timecount(){
        if(map.txtActivity != null) {
            if (map.txtActivity.equals("Vehicle")) {
                vtime += 1;
            } else if (map.txtActivity.equals("Bicycle")) {
                btime += 1;
            } else if (map.txtActivity.equals("Running")) {
                rtime += 1;
            } else if (map.txtActivity.equals("Walking")) {
                wtime += 1;
            } else if (map.txtActivity.equals("Still")) {
                stime += 1;
            } else if (map.txtActivity.equals("Unknown")) {
                utime += 1;
            }
            System.out.println("********unknown=" + utime);
            System.out.println("********vehicle=" + vtime);
            System.out.println("********bicycle=" + btime);
            System.out.println("********walking=" + wtime);
            System.out.println("********running=" + rtime);
            System.out.println("********still=" + stime);
        }
        //timesendbroadcast();

    }

    private void timesendbroadcast() {
        Intent intent = new Intent(Constants.COUNT_BROADCAST_DETECTED_ACTIVITY);
        intent.putExtra("sTime", stime);
        intent.putExtra("wTime", wtime);
        intent.putExtra("rTime", rtime);
        intent.putExtra("bTime", btime);
        intent.putExtra("vTime", vtime);
        intent.putExtra("uTime", utime);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

//intentService内に運動状態更新を入れる
    private void setUpReadWriteExternalStorage () {
        if (map.txtActivity != null) {
            if (isExternalStorageWritable()) {

                String sact = map.txtActivity;//活動
                double slat = map.lat;
                double slng = map.lng;

                sKcal = (float)(1.05  * (stime / 60));
                wKcal = (float) (3.5  * 1.05 * (wtime / 60));
                rKcal = (float)(8.3  * 1.05 * (rtime / 60));
                bKcal = (float) (4.0  * 1.05 * (btime / 60));
                vKcal = (float) (2.0  * 1.05 * (vtime / 60));
                uKcal =(0 *  (utime/60));

                sumkcal = sKcal + wKcal + rKcal + bKcal + vKcal + uKcal;
                //String sloc = locationname;//位置情報
                String swea = map.weather;
                Date dateParse = new Date(System.currentTimeMillis());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                final String sdate = sdf.format(dateParse);
                File file = new File(filePath);
                try (FileOutputStream fileOutputStream =
                             new FileOutputStream(file, true);
                     OutputStreamWriter outputStreamWriter =
                             new OutputStreamWriter(fileOutputStream, "UTF-8");
                     BufferedWriter bw =
                             new BufferedWriter(outputStreamWriter);
                ) {
                    // bw.write(sdate+","+slat+","+slng+","+sact+","+swea+","+map.stillcount+","+map.walkingcount+","+map.runningcount+","+map.bicyclecount+
                    //       ","+map.vehiclecount+","+map.unknowncount+","+map.stillKcal+","+map.walkingKcal+","+map.runnigKcal+","
                    //     +map.bicycleKcal+","+map.vehicleKcal+","+map.unknownKcal+","+sumkcal);//書き込み
                    bw.write(sdate + "," + slat + "," + slng + "," + sact + "," + swea + "," + (int)stime + "," + (int)wtime + "," + (int)rtime
                            + "," + (int)btime + "," + (int)vtime + "," + (int)utime + "," + sKcal + "," + wKcal + "," + rKcal + ","
                            + bKcal + "," + vKcal + "," + uKcal + "," + sumkcal + ",");
                    //bw.write(sdate+","+slat+","+slng+","+sact+",");
                    // bw.write(Hlat+","+Hlng);
                    bw.newLine();
                    bw.flush();
                    Toast.makeText(this, "txt保存ok", Toast.LENGTH_SHORT).show();
                    Log.d("MyIntentdebug", "ログ保存");
                    //保存成功
                } catch (Exception e) {
                    //失敗
                    e.printStackTrace();
                }

            }
        }else{
            Log.d("MyIntentdebug", "読み込み中です");
        }

    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        //setUpReadWriteExternalStorage();//csv
        Log.d("debug", "onDestroy");

        super.onDestroy();
    }


}
