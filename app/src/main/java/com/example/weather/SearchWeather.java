package com.example.weather;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class SearchWeather extends Activity implements View.OnClickListener{
    private Button bt_refresh;
    private EditText cityname;
    private Button bt_search;
    private Button bt_1;
    private Button bt_2;
    private Button bt_3;
    private Cursor cursor;
    private DBOpenHelper mDBOpenHelper;
    private SQLiteDatabase dbreader;

    private static final String CITY_TABLE_NAME = "city";
    List<city> mList;
    private TextView S_time,S_weather,S_temperature,S_winddirection,S_windpower,S_humidity,S_province,S_city;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt_refresh=findViewById(R.id.btn_refresh);
        cityname=findViewById(R.id.edit_city);
        bt_search=findViewById(R.id.search_w_btn);
        bt_1=findViewById(R.id.chaxun1);
        bt_2=findViewById(R.id.chaxun2);
        bt_3=findViewById(R.id.chaxun3);

        bt_refresh.setOnClickListener(this);
        bt_search.setOnClickListener(this);
        bt_1.setOnClickListener(this);
        bt_2.setOnClickListener(this);
        bt_3.setOnClickListener(this);

        mDBOpenHelper = new DBOpenHelper(this);
        dbreader = mDBOpenHelper.getReadableDatabase();

        initView();

        //检查网络连接状态
        if(CheckNet.getNetState(this)==CheckNet.NET_NONE){
            Log.d("MWEATHER","网络不通");
            Toast.makeText(SearchWeather.this,"网络不通",Toast.LENGTH_LONG).show();
        }else
        {
            Log.d("MWEATHER","网络OK");
            Toast.makeText(SearchWeather.this,"网络OK",Toast.LENGTH_LONG).show();
        }
    }
    public void onClick(View v){
        String citycode=((EditText)cityname).getText().toString();
      switch (v.getId()){
          case R.id.btn_refresh:
              getWeatherDatafromNet(citycode);
              selectDb();
              break;
          case R.id.search_w_btn:
              getWeatherDatafromNet(citycode);
              //将城市名加入到数据库中
              mDBOpenHelper.add(citycode);
              break;
          case R.id.chaxun1:
              getWeatherDatafromNet(bt_1.getText().toString());
              break;
          case R.id.chaxun2:
              getWeatherDatafromNet(bt_2.getText().toString());
              break;
          case R.id.chaxun3:
              getWeatherDatafromNet(bt_3.getText().toString());
              break;
      }
    }
    void initView(){
        S_time=findViewById(R.id.reporttime);
        S_weather=findViewById(R.id.weather);
        S_temperature=findViewById(R.id.temperature);
        S_winddirection=findViewById(R.id.winddirection);
        S_windpower=findViewById(R.id.windpower);
        S_humidity=findViewById(R.id.humidity);
        S_province=findViewById(R.id.province);
        S_city=findViewById(R.id.city);

        selectDb();
    }
    public void selectDb() {
        mList = new ArrayList<city>();
        cursor = dbreader.query("city", new String[] { "name" },null, null,null,null, "_id desc");
        while (cursor.moveToNext()) {
            city note = new city();
            String cityname = cursor.getString(cursor.getColumnIndex("name"));
            note.setCityname(cityname);
            mList.add(note);
        }
        cursor.close();

        bt_1.setText(mList.get(0).getCityname());
        bt_2.setText(mList.get(1).getCityname());
        bt_3.setText(mList.get(2).getCityname());
    }
    private void getWeatherDatafromNet(String cityCode)
    {
        final String address = "https://restapi.amap.com/v3/weather/weatherInfo?city="+cityCode+"&key=57a8ea4689c3da8e705b5d094982ef20&extensions=base";
        Log.d("Address:",address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL(address);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(8000);
                    urlConnection.setReadTimeout(8000);
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuffer sb = new StringBuffer();
                    String str;
                    while((str=reader.readLine())!=null)
                    {
                        sb.append(str);
                        Log.d("date from url",str);
                    }
                    String response = sb.toString();
                    Log.d("response",response);
                    parseJSONWithGSON(response);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJSONWithGSON(String jsonData){
        Gson gson=new Gson();
        Weather weather=gson.fromJson(jsonData,Weather.class);
        List<Weather.LivesBean> livesBean=weather.getLives();
        S_time.setText(livesBean.get(0).getReporttime());
        S_weather.setText("天气现象："+livesBean.get(0).getWeather());
        S_temperature.setText("实时温度："+livesBean.get(0).getTemperature()+"℃");
        S_winddirection.setText("风向描述："+livesBean.get(0).getWinddirection());
        S_windpower.setText("风力级别："+livesBean.get(0).getWindpower()+"级");
        S_humidity.setText("空气湿度："+livesBean.get(0).getHumidity()+"g/kg");
        S_province.setText(livesBean.get(0).getProvince()+"省");
        S_city.setText(livesBean.get(0).getCity());
    }

}
