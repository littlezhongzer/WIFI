package com.example.quectel.quectel_stress;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;


public class MainActivity extends Activity implements View.OnClickListener{
    public static final String TAG="MAIN AC";
    EditText et_normal_wifi_ssid,et_normal_wifi_password;
    Button btn_ok,btn_reset;
    wifi_status_thread wifi_status_receiver=new wifi_status_thread();
    public WIFIConnectionManager wifiConnectionManager;
    public static WifiManager  mainWifiManager;
    String et_normal_wifi_ssid_str,et_normal_wifi_password_str,et_stress_times_str;
    SharedPreferences mSharedPreferences;

    public static boolean flag_app_logcat_prc=true;
    PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);

            }
           // if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            //        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //请求权限

          //  }
        }
        //enable location permission to get bssid
        wakeLock=((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PostLocationService");


        InitView();
        if(wakeLock!=null)
        {
            Log.d(TAG, "onCreate: wake"+wakeLock);
            Log.d(TAG, "onCreate: wakelock start");
            wakeLock.acquire();//这句执行后，手机将不会休眠，直到执行wakeLock.release();方法
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        showAlterDialog_mainAC();
        Logcat_class mlogcat = new Logcat_class();
        mlogcat.start();
        Log.d(TAG, "onCreate: held"+ wakeLock.isHeld());


    }
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
    };    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 2;




    public void InitView(){
        et_normal_wifi_ssid=findViewById(R.id.et_normal_ssid);
        et_normal_wifi_password=findViewById(R.id.et_normal_password);
        btn_ok=findViewById(R.id.btn_ok);
        btn_reset=findViewById(R.id.btn_reset);
        btn_ok.setOnClickListener(this);
        btn_reset.setOnClickListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifi_status_receiver, filter);

        wifiConnectionManager = new WIFIConnectionManager(this);
        mainWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mSharedPreferences = getSharedPreferences("file", Context.MODE_PRIVATE);
        Log.d(TAG, "InitView: "+ mSharedPreferences.getString("ssid", null));
        et_normal_wifi_ssid.setText( mSharedPreferences.getString("ssid", null));
        et_normal_wifi_password.setText(  mSharedPreferences.getString("password",null));

    }

    public void ET_Reset() {
        et_normal_wifi_ssid.setText("");
        et_normal_wifi_password.setText("");

    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_ok:
                et_normal_wifi_ssid_str=et_normal_wifi_ssid.getText().toString();
                et_normal_wifi_password_str=et_normal_wifi_password.getText().toString();
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("wifi_ssid", et_normal_wifi_ssid_str);  //key-"sff",通过key得到value-"value值"(String型)
                bundle.putString("wifi_password", et_normal_wifi_password_str);  //key-"sff",通过key得到value-"value值"(String型)
                //bundle.putString("stress_time", et_stress_times_str);  //key-"sff",通过key得到value-"value值"(String型)
                intent.putExtras(bundle); //通过intent将bundle传到另个Activity

                mSharedPreferences=getSharedPreferences("file", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit=mSharedPreferences.edit();
                edit.putString("ssid",et_normal_wifi_ssid_str);
                edit.putString("password",et_normal_wifi_password_str);
                edit.commit();
                Log.d(TAG, "onClick: "+  mSharedPreferences.getString("ssid", null));
                intent.setClass(MainActivity.this,ListViewActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_reset:
                ET_Reset();
                break;
        }
    }

    private void showAlterDialog_mainAC(){
        final AlertDialog.Builder alterDiaglog = new AlertDialog.Builder(MainActivity.this);
        alterDiaglog.setTitle("提示！！");//文字
        alterDiaglog.setMessage("测试报告路径："+"/sdcard/"+"\n\n"+"文件夹名\n"+"\t功能验证："+"simple_check"+"\n"+"\t压力测试："+"wifi_stress"+"\n"+"\t性能测试："+"wifi_performance"+"\n"+"\t漫游测试："+"wifi_roam"+"\n\n"+"日志"+"\n"+"\tLogcat："+"WiFi_logcat.txt");//提示消息
        //积极的选择
        alterDiaglog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        //显示
        alterDiaglog.show();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifi_status_receiver);
        flag_app_logcat_prc=false;
        if(wakeLock!=null)
        {
            wakeLock.release();
        }
        Log.d(TAG, "onDestroy: "+ wakeLock.isHeld());
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

}
