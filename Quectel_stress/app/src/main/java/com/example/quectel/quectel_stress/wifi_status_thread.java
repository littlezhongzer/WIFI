package com.example.quectel.quectel_stress;

/**
 * Created by loyal.zhong on 2019/3/5.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import java.util.List;

import static com.example.quectel.quectel_stress.MainActivity.mainWifiManager;
import static com.example.quectel.quectel_stress.ShowActivity.mWifiManager;
import static com.example.quectel.quectel_stress.WifiRoamAC.mmWifiManager;


public class wifi_status_thread extends BroadcastReceiver {
    private static final String TAG = "savefileactivity";
    static int state;
    static boolean wifiIsConnect;
    List<ScanResult> list;
    WifiInfo wifiinfo;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        Log.d(TAG, "onReceive: action" + action);
        Log.d(TAG, "onReceive:==>" + printBundle(extras));

        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {//这个监听wifi的打开与关闭，与wifi的连接无关
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    Log.d(TAG, "onReceive: wifiState:WIFI_STATE_DISABLED");
                    state = 0;
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    Log.d(TAG, "onReceive: wifiState:WIFI_STATE_DISABLING");
                    state = 1;
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.d(TAG, "onReceive: wifiState:WIFI_STATE_ENABLED");
                    state = 2;
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    Log.d(TAG, "onReceive:wifiState:WIFI_STATE_ENABLING");
                    state = 3;
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    Log.d(TAG, "onReceive: wifiState:WIFI_STATE_UNKNOWN");
                    state = 4;
                    break;
                //
            }
        }

        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
            Log.d(TAG, "onReceive: wifi scan"+list);
            Log.d(TAG, "onReceive: "+WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction()));
            list = mainWifiManager.getScanResults();

        }

        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (parcelableExtra != null) {
                if(mainWifiManager.getConnectionInfo() != null){
                    Log.d(TAG, "onReceive: here"+mainWifiManager.getConnectionInfo().getBSSID());
                   // wifiinfo=mainWifiManager.getConnectionInfo();
                }

                // 获取联网状态的NetWorkInfo对象
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                //获取的State对象则代表着连接成功与否等状态
                NetworkInfo.State conn_state = networkInfo.getState();
                //判断网络是否已经连接
                boolean isConnected = conn_state == NetworkInfo.State.CONNECTED;
                wifiIsConnect = isConnected;
                Log.i(TAG, "isConnected:" + isConnected);
            }
        }
    }



    public  static int getWifistate() throws InterruptedException {
        Thread.currentThread().sleep(100);
        return state;
    }


    /**
     * @return 返回当前wifi bssid
     */
    public  String getBssid() throws InterruptedException {
        if(mainWifiManager.getConnectionInfo() != null){
            Log.d(TAG, "onReceive: here"+mainWifiManager.getConnectionInfo().getBSSID());
            return mainWifiManager.getConnectionInfo().getBSSID();
        }
        return mainWifiManager.getConnectionInfo().getBSSID();
    }

    /**
     * @return 返回当前wifi IP
     */
    public String getIP() throws InterruptedException {
        wifiinfo=mainWifiManager.getConnectionInfo();
        if(wifiinfo.getIpAddress()!=0){
            Log.d(TAG, "onReceive: heredd"+wifiinfo.getIpAddress());
            return intToIp(wifiinfo.getIpAddress());
        }

        return intToIp(wifiinfo.getIpAddress());
    }


    /**
     * @return 返回扫描wifi
     */
    public  List getScanResult() throws InterruptedException {
        if(list!=null){
            Log.d(TAG, "getScanResult: wifi status "+list.size());
        }
       return list;

    }

    public static boolean getConn_state(){
        return wifiIsConnect;
    }

    private String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        if(bundle==null){return null;}
        for (String key : bundle.keySet()) {
            if (key.equals(WifiManager.EXTRA_WIFI_STATE)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            } else {
                sb.append("\nkey:" + key + ", value:" + bundle.get(key));
            }
        }
//        L.e("bundle:"+bundle);
        return sb.toString();
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }

}

