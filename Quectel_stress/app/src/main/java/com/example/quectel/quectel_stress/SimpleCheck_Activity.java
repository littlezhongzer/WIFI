package com.example.quectel.quectel_stress;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.WindowManager;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.example.quectel.quectel_stress.ListViewActivity.et_normal_wifi_password_str;
import static com.example.quectel.quectel_stress.ListViewActivity.et_normal_wifi_ssid_str;


public class SimpleCheck_Activity extends AppCompatActivity {
    public static final String TAG = "show activity";
    TextView tv_wifi_ssid, tv_wifi_switcher_open, tv_wifi_switcher_close, tv_wifi_scan, tv_wifi_connect, tv_wifi_disconnect, tv_ping, tv_ap_level, tv_result;
    public WIFIConnectionManager wifiConnectionManager;
    public static WifiManager mWifiManager;
    //private String ssid = "Xiaomi_4CBF";
    //private String password = "quectel88";
    int status;
    int i = 0;
    wifi_status_thread wifi_status_receiver_simplecheck = new wifi_status_thread();
    Simpelcheck_Thread mSimplecheck_Thread = new Simpelcheck_Thread();
    Show_wificonnect show_Wificonnect;
    List list;
    SparseArray<String> mSparseArray_result = new SparseArray();
    ScanResult mScanResult = null;
    WifiInfo wifiInfo;
    int result = 0;

    boolean flag_simple_check=true; //control thread

    FileUtils mFileUtil = new FileUtils();
    public FileIOUtils mFileIOutils = new FileIOUtils();
    String SD_path="/sdcard/";
    String fileName;
    String mkName="simple_check/";
    String private_filename="simple_check.txt";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_check);
        InitView();
        if (wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)) ;
        {
            tv_wifi_ssid.setText(et_normal_wifi_ssid_str);
            // tv_show_wifi_con_status.setText("Connected");
        }
        //show_Wificonnect.start();
        Log.d(TAG, "onCreate: size" + mSparseArray_result.get(0));
        mSimplecheck_Thread.start();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


    }

    public void InitView() {
        tv_wifi_ssid = findViewById(R.id.tv_simpleCheck_wifi_ssid);
        tv_wifi_switcher_open = findViewById(R.id.tv_simpleCheck_wifi_switcher_open);
        tv_wifi_switcher_close = findViewById(R.id.tv_simpleCheck_wifi_switcher_close);
        tv_wifi_scan = findViewById(R.id.tv_simpleCheck_wifi_scan);
        tv_wifi_connect = findViewById(R.id.tv_simpleCheck_wifi_connect);
        tv_wifi_disconnect = findViewById(R.id.tv_simpleCheck_wifi_disconnect);
        tv_ping = findViewById(R.id.tv_simpleCheck_ping);
        tv_ap_level = findViewById(R.id.tv_simpleCheck_connect_ap_level);
        tv_result = findViewById(R.id.tv_simpleCheck_result);
        // tv_show_wifi_switcher_status = findViewById(R.id.tv_show_wifi_switcher_status);
        fileName = mFileUtil.setFileName(private_filename);
        mFileIOutils.makeFilePath(SD_path,mkName,fileName);
        mFileUtil.file_head_SK(SD_path+mkName+fileName);



        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter2.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter2.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter2.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        //registerReceiver(wifi_status_receiver_simplecheck, filter2);


        String[] PERMS_INITIAL = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMS_INITIAL, 127);
        }

        wifiConnectionManager = new WIFIConnectionManager(this);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        show_Wificonnect = new Show_wificonnect();

    }

    class Simpelcheck_Thread extends Thread {
        public static final String TAG = "simple check  thead";
        public void run() {
            while (true) {
                if (flag_simple_check == true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //*************wifi close****************//
                if (i == 0) {
                    mWifiManager.setWifiEnabled(false);
                    i++;
                    Log.d(TAG, "run: close wifi");
                }
                if (i == 1 && !mWifiManager.isWifiEnabled()) {
                    Message message = new Message();
                    message.what = 0x2000;
                    mHandler.sendMessage(message);
                    mWifiManager.setWifiEnabled(true);
                    i++;
                    mSparseArray_result.put(0, "PASS");
                    Log.d(TAG, "run: open wifi");
                }
                //*************wifi open****************//
                try {
                    if (i == 2 && wifi_status_thread.getWifistate() == 2) {
                        Message message = new Message();
                        message.what = 0x2001;
                        mHandler.sendMessage(message);
                        i++;
                        Log.d(TAG, "run: reopen wifi");
                        Log.d(TAG, "run: iii" + i);
                        mSparseArray_result.put(1, "PASS");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //*************wifi scan****************//
                if (i == 3 && mWifiManager.isWifiEnabled()) {
                    mWifiManager.startScan();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "run: scaned wifi");

                    list = mWifiManager.getScanResults();
                    Log.d(TAG, "run: list" + list);
                    if (list != null) {
                        Message message = new Message();
                        message.what = 0x2002;
                        mHandler.sendMessage(message);
                        i++;
                        Log.d(TAG, "scan wifi");
                        mSparseArray_result.put(2, "PASS");
                    }
                }
                //*************wifi connect****************//
                if (i == 4 && mWifiManager.isWifiEnabled()) {
                    wifiConnectionManager.connect(et_normal_wifi_ssid_str, et_normal_wifi_password_str);
                    if (wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)) {
                        Message message = new Message();
                        message.what = 0x2003;
                        mHandler.sendMessage(message);
                        i++;
                        Log.d(TAG, "run: dd" + i);
                        Log.d(TAG, "run: connect wifi");
                        mSparseArray_result.put(3, "PASS");
                    }
                }
                //*************wifi disconnect****************//
                if (i == 5 && wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)) {
                    wifiConnectionManager.disconnect();
                    Log.d(TAG, "run: run here");
                    Log.d(TAG, "run: run here1");
                    Message message = new Message();
                    message.what = 0x2004;
                    mHandler.sendMessage(message);
                    i++;
                    Log.d(TAG, "run: " + i);
                    Log.d(TAG, "run: disconnect wifi");
                    mSparseArray_result.put(4, "PASS");

                }
                //*************wifi ping****************//
                if (i == 6 && mWifiManager.isWifiEnabled()) {
                    wifiConnectionManager.connect(et_normal_wifi_ssid_str, et_normal_wifi_password_str);
                    if (wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)) {
                        isPingSuccess(10, "www.baidu.com");
                        if (status == 0) {
                            Message message = new Message();
                            message.what = 0x2005;
                            mHandler.sendMessage(message);
                            i++;
                            mSparseArray_result.put(5, "PASS");
                        }
                    }
                }
                //*************get connected wifi ip****************//
                if (i == 7 && wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)) {
                    wifiInfo = mWifiManager.getConnectionInfo();
                    wifiInfo.getRssi();
                    Message message = new Message();
                    message.what = 0x2006;
                    mHandler.sendMessage(message);
                    Log.d(TAG, "run: getrssi" + wifiInfo.getRssi());
                    i++;
                    mSparseArray_result.put(6, "PASS");
                }
                //*************statistics result****************//
                if (i == 8) {
                    for (int a = 0; a < mSparseArray_result.size(); a++) {
                        Log.d(TAG, "run: size" + mSparseArray_result.size());
                        if (mSparseArray_result.get(a) == "PASS") {
                            result++;
                            Log.d(TAG, "run: result" + result);
                        }
                    }
                    i++;
                    Message message = new Message();
                    message.what = 0x2007;
                    mHandler.sendMessage(message);
                    break;
                }

            }
            }
        }
    }


    private void isPingSuccess(int pingNum, String m_strForNetAddress) {
        StringBuffer tv_PingInfo = new StringBuffer();
        try {

            Process p = Runtime.getRuntime()
                    .exec("ping -c 5 -s 128 www.baidu.com");// 10.83.50.111
            // m_strForNetAddress
            status = p.waitFor();
            String result = "";
            Log.d(TAG, "isPingSuccess: status" + status);
            if (status == 0) {
                result = "success";
                Log.d(TAG, "isPingSuccess: ping success");
            } else {
                result = "failed";
                // String pingResult = "failded";
                Message msg = new Message();
                msg.obj = m_strForNetAddress;
                msg.what = 0;
                //mHandler.sendMessage(msg);
                return;
            }
            String lost = new String();
            String delay = new String();
            BufferedReader buf = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));

            String str = new String();
            // 读出所有信息并显示
            while ((str = buf.readLine()) != null) {
                str = str + "\r\n";
                tv_PingInfo.append(str);
            }

            /// pingResult = tv_PingInfo.toString();
            Message msg = new Message();
            msg.obj = m_strForNetAddress;
            msg.what = 1;
            /// mHandler.sendMessage(msg);
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
            //pingResult = "拼通了，但是有异常";
            //mHandler.sendEmptyMessage(2);
            return;
        }
    }


    class Show_wificonnect extends Thread {
        public static final String TAG = "show AC wifi connect thead";

        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)) {
                    Message message = new Message();
                    message.what = 0x1003;
                    mHandler.sendMessage(message);//show AC update wifi connect status to conneccted
                } else if (!wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)) {
                    Message message = new Message();
                    message.what = 0x1004;
                    mHandler.sendMessage(message); // show AC update wifi connect status to disconnected
                }

                if (mWifiManager.isWifiEnabled()) {
                    Message message = new Message();
                    message.what = 0x1007;
                    mHandler.sendMessage(message);//show AC update wifi switcher status to enable
                } else if (!mWifiManager.isWifiEnabled()) {
                    Message message = new Message();
                    message.what = 0x1008;
                    mHandler.sendMessage(message);//show AC update wifi switcher status to unable
                }

            }
        }
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x2000) {
                // wifi close
                Log.d(TAG, "handleMessage: 0x000");

                tv_wifi_switcher_close.setText("PASS");
                mFileUtil.result_append(SD_path+mkName+fileName,mFileUtil.getDate()+"\t"+"*Close Wifi*"+"\t"+"PASS"+"\t\n");
            }
            if (msg.what == 0x2001) {
                //wifi open
                tv_wifi_switcher_open.setText("PASS");
                mFileUtil.result_append(SD_path+mkName+fileName,mFileUtil.getDate()+"\t"+"*Open Wifi*"+"\t"+"PASS"+"\t\n");
            }
            if (msg.what == 0x2002) {
                // wifi scan
                tv_wifi_scan.setText("PASS");
                mFileUtil.result_append(SD_path+mkName+fileName,mFileUtil.getDate()+"\t"+"*Wifi Scan*"+"\t"+"PASS"+"\t\n");
            }
            if (msg.what == 0x2003) {
                // wifi connect
                tv_wifi_connect.setText("PASS");
                mFileUtil.result_append(SD_path+mkName+fileName,mFileUtil.getDate()+"\t"+"*Wifi Connection*"+"\t"+"PASS"+"\t\n");
            }
            if (msg.what == 0x2004) {
                // wifi disconnect
                tv_wifi_disconnect.setText("PASS");
                mFileUtil.result_append(SD_path+mkName+fileName,mFileUtil.getDate()+"\t"+"*Wifi Disconnection*"+"\t"+"PASS"+"\t\n");
            }
            if (msg.what == 0x2005) {
                // wifi ping
                tv_ping.setText("PASS");
                mFileUtil.result_append(SD_path+mkName+fileName,mFileUtil.getDate()+"\t"+"*Wifi Ping*"+"\t"+"PASS"+"\t\n");
            }
            if (msg.what == 0x2006) {
                // connected ap level
                tv_ap_level.setText("PASS &" + "  level: " + wifiInfo.getRssi());
                mFileUtil.result_append(SD_path+mkName+fileName,mFileUtil.getDate()+"\t"+"*Connected AP Level*"+"\t"+"PASS"+"\t\n");
            }
            if (msg.what == 0x2007) {
                // check result
                int fail = mSparseArray_result.size() - result;
                tv_result.setText("Total： " + mSparseArray_result.size() + "\n" + "PASS： " + result + "\n" + "FAIL： " + fail + "\n");
            }
        }
    };




    @Override
    protected void onDestroy() {
        super.onDestroy();
        flag_simple_check=false;
    }
}
