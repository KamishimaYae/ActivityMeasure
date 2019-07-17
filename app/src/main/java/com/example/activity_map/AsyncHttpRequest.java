package com.example.activity_map;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 非同期処理を行うクラス.
 */
public final class AsyncHttpRequest extends AsyncTask<URL, Void, String> {
    private int TODAY_FORCAST_INDEX = 0;
    private Activity mainActivity;

    public AsyncHttpRequest(Activity activity) {
        // 呼び出し元のアクティビティ
        this.mainActivity = activity;
    }

    /**
     * 非同期処理で天気情報を取得する.
     * @param urls 接続先のURL
     * @return 取得した天気情報
     */
    @Override
    protected String doInBackground(URL... urls) {

        final URL url = urls[0];
        HttpURLConnection con = null;

        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            // リダイレクトを自動で許可しない設定
            con.setInstanceFollowRedirects(false);
            con.connect();
            //System.out.println("***********aaaaaa\n"+urls[0]);

            final int statusCode = con.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                System.err.println("正常に接続できていません。statusCode:" + statusCode);
                return null;
            }

            // レスポンス(JSON文字列)を読み込む準備
            final InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            if(null == encoding){
                encoding = "UTF-8";
            }
            final InputStreamReader inReader = new InputStreamReader(in, encoding);
            final BufferedReader bufReader = new BufferedReader(inReader);
            StringBuilder response = new StringBuilder();
            String line = null;
            // 1行ずつ読み込む
            while((line = bufReader.readLine()) != null) {
                response.append(line);
            }
            bufReader.close();
            inReader.close();
            in.close();

            // 受け取ったJSON文字列をパース
            JSONObject jsonObject = new JSONObject(response.toString());
            JSONObject details = jsonObject.getJSONArray("weather").getJSONObject(TODAY_FORCAST_INDEX);
            JSONObject todayForcasts = jsonObject.getJSONObject("main");//getJCONArrayにしていたため動かなかった。型があったため動いた
            String tenkou = details.getString("main");
            if (tenkou.equals("Rain")) {
                tenkou = "雨";
            } else if (tenkou.equals("Clear")) {
                tenkou = "晴れ";
            } else if (tenkou.equals("Clouds")) {
                tenkou = "曇り";
            } else if (tenkou.equals("Snow")){
                tenkou = "雪";
            }


            return "気温:" + todayForcasts.getDouble("temp") +"°\n天気:" +  tenkou ;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    /**
     * 非同期処理が終わった後の処理.
     * @param result 非同期処理の結果得られる文字列
     */
    @Override
    protected void onPostExecute(String result) {
        TextView tv = mainActivity.findViewById(R.id.weather);
        tv.setText(result);
    }
}