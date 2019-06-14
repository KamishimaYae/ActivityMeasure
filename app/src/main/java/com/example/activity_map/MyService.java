package com.example.activity_map;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MyService extends Service {
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // to do something
        return START_NOT_STICKY;//勝手な再起動を防ぐ
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }//バックグラウンドで動くスレッドを使っている場合、onDestroy()で確実に止めること

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

