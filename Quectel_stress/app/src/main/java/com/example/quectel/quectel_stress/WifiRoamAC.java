package com.example.quectel.quectel_stress;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Objects;

import static com.example.quectel.quectel_stress.ListViewActivity.et_normal_wifi_ssid_str;

public class WifiRoamAC extends AppCompatActivity {
    public static final String TAG = "a Activity";
    public WIFIConnectionManager wifiConnectionManager;
    public static WifiManager mmWifiManager;
    WifiInfo wifiInfo;
    TextView tv_roam_ssid,tv_roam_status,tv_roam_bssid,tv_roam_bssid_compare,tv_roam_time,tv_roam_success_times;
    Roam_wificonnect roam_wificonnect = new Roam_wificonnect();
    wifi_status_thread wifi_status_receiver = new wifi_status_thread();
    //private String ssid = "Quectel-Hf";
    //private String password = "*quectel-i-hf*";
    String Bssid;
    String RAW_Bssid;
    SparseArray<String> mySparseArray = new SparseArray();
    int i=0;
    public  long time_raw;
    public  long time_new;
    public  long diff_time;

    boolean flag_raom_thread=true;//control  thread

    FileUtils mFileutil = new FileUtils();
    FileIOUtils mFileIOutils = new FileIOUtils();
    String fileName;
    String SD_path="/sdcard/";
    String mkName="wifi_roam/";
    String private_filename="wifi_roam.txt";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_roam_ac);
        InitView();
        tv_roam_ssid.setText(et_normal_wifi_ssid_str);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void InitView() {
        mmWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiConnectionManager = new WIFIConnectionManager(this);
        wifiInfo = mmWifiManager.getConnectionInfo();
        tv_roam_ssid=findViewById(R.id.tv_roam_wifi_ssid);
        tv_roam_status=findViewById(R.id.tv_roam_wifi_status);
        tv_roam_bssid=findViewById(R.id.tv_roam_wifi_bssid);
        tv_roam_bssid_compare=findViewById(R.id.tv_roam_wifi_bssid_compare);
        tv_roam_time=findViewById(R.id.tv_roam_wifi_time);
        tv_roam_success_times=findViewById(R.id.tv_roam_success_times);

        fileName = mFileutil.setFileName(private_filename);
        mFileIOutils.makeFilePath(SD_path,mkName,fileName);
        mFileutil.file_head_roam(SD_path+mkName+fileName);
        roam_wificonnect.start();
        
    }

    class Roam_wificonnect extends Thread {

        public void run() {
            //wifiConnectionManager.connect(ssid,password);
            Bssid = mmWifiManager.getConnectionInfo().getBSSID();
            RAW_Bssid = Bssid;
            // Log.d(TAG, "run: bssid raw"+mmWifiManager.getConnectionInfo().getBSSID());
            while (true) {
                if (flag_raom_thread) {
                Log.d(TAG, "run: roam");
                try {
                    Bssid = wifi_status_receiver.getBssid();
                    time_raw = System.currentTimeMillis();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //  Log.d(TAG, "run: get bssid"+mmWifiManager.getConnectionInfo().getBSSID());
                if (wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)) {
                    Message message = new Message();
                    message.what = 0x7001;
                    mHandler.sendMessage(message);//show ROAM AC update wifi connect status to conneccted
                } else if (!wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)) {
                    Message message = new Message();
                    message.what = 0x7002;
                    mHandler.sendMessage(message);
                    // show ROAM AC update wifi connect status to disconnected
                }
                if (wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)) {
                    if (!RAW_Bssid.equals(Bssid)&&!Objects.equals(Bssid, "00:00:00:00:00:00")&& !Objects.equals(RAW_Bssid, "00:00:00:00:00:00")) {
                        time_new = System.currentTimeMillis();
                        diff_time = time_new - time_raw;
                        Message message = new Message();
                        message.what = 0x7003;
                        mHandler.sendMessage(message);
                        Log.d(TAG, "run: iasdfasdfasdf"+i);

                    } else {

                    }
                }
            }
        }
            }
        }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x7001) {
                tv_roam_status.setText("Connected");
                tv_roam_bssid.setText(RAW_Bssid);
            }
            if (msg.what == 0x7002) {
                tv_roam_status.setText("Disconnected");
            }
            if (msg.what == 0x7003) {
                //adapter android 9 ,avoid to writer date twice
                if(!Objects.equals(Bssid, RAW_Bssid) && !Objects.equals(Bssid, "00:00:00:00:00:00")&& !Objects.equals(RAW_Bssid, "00:00:00:00:00:00")) {
                    tv_roam_bssid_compare.setText("*" + RAW_Bssid + "*\n" + "*" + Bssid + "*");
                    tv_roam_time.setText(String.valueOf(diff_time) + " ms");
                    ++i;
                    tv_roam_bssid.setText(Bssid);
                    tv_roam_success_times.setText(String.valueOf(i));
                    Log.d(TAG, "handleMessage: aushdiufhasiudhfisadf" + i);
                    mFileutil.result_append(SD_path + mkName + fileName, mFileutil.getDate() + "\t" + "Wifi Roaming" + "\t" + i + "\t" + diff_time + "ms" + "\t" + RAW_Bssid + "**" + Bssid + "\t" + "PASS" + "\t\n");
                    RAW_Bssid = Bssid;

                }else{}

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
       flag_raom_thread=false;
    }

}
