package com.example.activity_map;

import android.Manifest;
        import android.app.ProgressDialog;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.content.pm.PackageManager;
        import android.location.Geocoder;
        import android.location.Location;
        import android.location.LocationManager;
        import android.os.AsyncTask;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Environment;
        import android.support.annotation.NonNull;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.app.FragmentActivity;
        import android.support.v4.content.LocalBroadcastManager;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
        import android.widget.Toast;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.location.DetectedActivity;
        import com.google.android.gms.location.LocationListener;
        import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.location.LocationServices;
        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.LocationSource;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.BitmapDescriptorFactory;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.MarkerOptions;

        import java.io.BufferedWriter;
        import java.io.File;
        import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
        import java.util.Date;
        import java.util.Locale;
        import io.realm.Realm;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMyLocationButtonClickListener, LocationSource {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;
    public String txtActivity, txtConfidence;
    public Button btnStartTracking, btnStopTracking;//ボタン
    public Button activityedit;
    public Realm mRealm;
    private ListView mListView;
    public  double lat;
    public  double lng;
    public double Hlat;
    public double Hlng;
    private TextView locationTextView;
    private TextView locationDetailTextView;
    private TextView locationWeather;
    private TextView currentTemperatureField;
    private TextView temp;
    private ProgressBar loader;
    public String locationname;
    public String weather;
    private LocationManager locationManager;
    private Location location = null;
    private Geocoder geocoder;//4GEO
    public String home;
    private final int REQUEST_PERMISSION = 1000;//csv
    private String filePath = "/testfileAct.txt";//csv
    private String filePath_h = "/h.txt";//csv


    private String TAG = MainActivity.class.getSimpleName();//db
    BroadcastReceiver broadcastReceiver;
    String city = "Tokyo, JP";

    String OPEN_WEATHER_MAP_API = "6fed7f8a8b65d9b677317a8833be2d0c";


    private OnLocationChangedListener onLocationChangedListener = null;

    private int priority[] = {LocationRequest.PRIORITY_HIGH_ACCURACY, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
            LocationRequest.PRIORITY_LOW_POWER, LocationRequest.PRIORITY_NO_POWER};
    private int locationPriority;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        filePath = Environment.getExternalStorageDirectory().getPath() + filePath;
        filePath_h = Environment.getExternalStorageDirectory().getPath() + filePath_h;
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        } else {
            setUpReadWriteExternalStorage();
        }
        locationRequest = LocationRequest.create();

        // 精度
        locationPriority = priority[1];

        if (locationPriority == priority[0]) {
            // GPSの精度高い
            locationRequest.setPriority(locationPriority);
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(16);
        } else if (locationPriority == priority[1]) {
            //WIFI 電力消費量を抑える
            locationRequest.setPriority(locationPriority);
            locationRequest.setInterval(60000);
            locationRequest.setFastestInterval(16);
        } else if (locationPriority == priority[2]) {
            // １０ＫＭ
            locationRequest.setPriority(locationPriority);
        } else {
            // 他のアプリから
            locationRequest.setPriority(locationPriority);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);//地図表示

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        //AR
        btnStartTracking = findViewById(R.id.btn_start_tracking);//開始ボタン
        btnStopTracking = findViewById(R.id.btn_stop_tracking);//終了ボタン

        btnStartTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTracking();
            }
        });

        btnStopTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTracking();
            }
        });


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);

                }
            }
        };
        startTracking();
        locationTextView = (TextView) findViewById(R.id.locationTextView);
        locationDetailTextView = (TextView) findViewById(R.id.locationDetailTextView);//場所表示
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationWeather = (TextView) findViewById(R.id.locationWeather);

    }


    private void handleUserActivity(int type, int confidence) {
        // 状態表示
        String label = getString(R.string.activity_unknown);
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
        Log.d(TAG, "User activity: " + label + ", Confidence: " + confidence);//LogCatに表示

        if (confidence > Constants.CONFIDENCE) {
            txtActivity = label;
            txtConfidence = "" + confidence;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
    }

    private void startTracking() {
        Intent intent1 = new Intent(MapsActivity.this, BackgroundDetectedActivitiesService.class);//
        startService(intent1);
    }

    private void stopTracking() {
        Intent intent = new Intent(MapsActivity.this, BackgroundDetectedActivitiesService.class);//
        stopService(intent);
    }


    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("debug", "permission granted");

            mMap = googleMap;
            mMap.setLocationSource(this);
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
        } else {
            Log.d("debug", "permission error");
            return;
        }
        mMap.moveCamera(CameraUpdateFactory.zoomTo(18));
        //touch

    }


    public void onLocationChanged(Location location) {

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                //allPoints.add(point);
                Hlat=point.latitude;
                Hlng=point.longitude;
                // mMap.clear();
                mMap.addMarker(new MarkerOptions().position(point).title("Home").icon(BitmapDescriptorFactory.defaultMarker(100)));
                setUpReadWriteExternalStorage_home();//csv
            }
        });
        if (onLocationChangedListener != null) {

            onLocationChangedListener.onLocationChanged(location);

            lat = location.getLatitude();
            lng = location.getLongitude();
            //Toast.makeText(this, "location=" + lat + "," + lng, Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, txtActivity, Toast.LENGTH_LONG).show();
            // Add a marker and move the camera
            LatLng newLocation = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(newLocation).title(txtActivity));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));
            //geo
            String finalCoordinator = location.getLatitude() + " " + location.getLongitude();
            locationTextView.clearComposingText();
            locationTextView.append("\n " + finalCoordinator);
            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
            locationDetailTextView.setText(GeoCoding.reverseGeoCoding(geocoder, location.getLatitude(), location.getLongitude()));//geo重要
            locationname = locationDetailTextView.getText().toString();//
            locationWeather.clearComposingText();
            try {
                new AsyncHttpRequest(this).execute(new URL("https://api.openweathermap.org/data/2.5/weather?lat="+
                        location.getLatitude()+"&lon="+location.getLongitude()+"&units=metric&appid=6fed7f8a8b65d9b677317a8833be2d0c"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            weather = locationWeather.getText().toString();

            if (locationname.equals(home) == true) locationname = "home";
            String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%.4f,%.4f&sensor=false", location.getLatitude(), location.getLongitude());

            Log.d(TAG, "doInBackground: url :" + url);
            //Toast.makeText(this, locationname, Toast.LENGTH_LONG).show();
            //geo

            setUpReadWriteExternalStorage();//csv
            // setUpReadWriteExternalStorage_home();
            Toast.makeText(this, "追加した", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onConnected(Bundle bundle) {
        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("debug", "permission granted");

            //
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, locationRequest, this);
        } else {
            Log.d("debug", "permission error");
            return;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("debug", "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("debug", "onConnectionFailed");
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "onMyLocationButtonClick", Toast.LENGTH_SHORT).show();

        return false;
    }

    // OnLocationChangedListener calls activate() method
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        this.onLocationChangedListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        this.onLocationChangedListener = null;
    }

    public class multiThreading extends AsyncTask<Void, Void, GeoCoding> {
        ProgressDialog dialog = new ProgressDialog(MapsActivity.this);

        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Please wait...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        protected void onPostExecute(GeoCoding geoCoding) {
            super.onPostExecute(geoCoding);
        }

        protected GeoCoding doInBackground(Void... voids) {


            locationDetailTextView.setText(GeoCoding.reverseGeoCoding(geocoder, location.getLatitude(), location.getLongitude()));
            String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%.4f,%.4f&sensor=false", location.getLatitude(), location.getLongitude());
            Log.d(TAG, "doInBackground: url :" + url);
            return null;
        }





    }


    //ひきだし
    private void setUpReadWriteExternalStorage () {
        if (isExternalStorageWritable()) {
            String sact = txtActivity;//活動
            double slat =lat;
            double slng=lng;
            //String sloc = locationname;//位置情報
            String swea = weather;
            Date dateParse = new Date(System.currentTimeMillis());
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            final String sdate= sdf.format(dateParse);
            File file = new File(filePath);
            try (FileOutputStream fileOutputStream =
                         new FileOutputStream(file, true);
                 OutputStreamWriter outputStreamWriter =
                         new OutputStreamWriter(fileOutputStream, "UTF-8");
                 BufferedWriter bw =
                         new BufferedWriter(outputStreamWriter);
            ) {
                bw.write(sdate+","+slat+","+slng+","+sact+","+swea+",");//書き込み
                //bw.write(sdate+","+slat+","+slng+","+sact+",");
                // bw.write(Hlat+","+Hlng);
                bw.newLine();
                bw.flush();
                Toast.makeText(this, "txt保存ok", Toast.LENGTH_SHORT).show();
                //保存成功
            } catch (Exception e) {
                //失敗
                e.printStackTrace();
            }
        }
    }
    private void setUpReadWriteExternalStorage_home () {
        if (isExternalStorageWritable()) {
            File file = new File(filePath_h);
            try (FileOutputStream fileOutputStream =
                         new FileOutputStream(file, true);
                 OutputStreamWriter outputStreamWriter =
                         new OutputStreamWriter(fileOutputStream, "UTF-8");
                 BufferedWriter bw =
                         new BufferedWriter(outputStreamWriter);
            ) {
                bw.write("Home"+Hlat+Hlng);
                bw.newLine();
                bw.flush();
                Toast.makeText(this, "home保存ok", Toast.LENGTH_SHORT).show();
                //保存成功を表示
            } catch (Exception e) {
                //失敗
                e.printStackTrace();
            }
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    // permissionの確認
    public void checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            setUpReadWriteExternalStorage();
        }
        // 拒否していた場合
        else {
            requestLocationPermission();
        }
    }

    // 許可を求める
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);

        } else {
            Toast toast =
                    Toast.makeText(this, "アプリ実行に許可が必要です", Toast.LENGTH_SHORT);
            toast.show();

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,},
                    REQUEST_PERMISSION);

        }
    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpReadWriteExternalStorage();
            } else {
                // それでも拒否された時の対応
                Toast toast =
                        Toast.makeText(this, "何もできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
