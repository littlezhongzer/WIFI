package com.example.quectel.quectel_stress;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.List;

import static com.example.quectel.quectel_stress.ListViewActivity.et_normal_wifi_password_str;
import static com.example.quectel.quectel_stress.ListViewActivity.et_normal_wifi_ssid_str;
import static com.example.quectel.quectel_stress.ListViewActivity.et_stress_times_str;


public class ShowActivity extends AppCompatActivity {
    public static final String TAG = "show activity";
    TextView tv_show_wifi_con_status, tv_show_wifi_ssid, tv_show_wifi_open, tv_show_con, tv_show_ping,tv_show_discon,tv_show_wifi_close,
            tv_show_scan, tv_show_scan_wifi_nums,tv_show_ssid1_level, tv_show_ssid2_level,tv_show_wifi_switcher_status,tv_round_times,tv_testdata,tv_total_time;
    public WIFIConnectionManager wifiConnectionManager;
    public static WifiManager mWifiManager;
   // private String ssid = "Xiaomi_4CBF";
   // private String password = "quectel88";
    int status;
    int i=0;
    wifi_status_thread wifi_status_receiver = new wifi_status_thread();
    TestThread mTestThread = new TestThread();
    Show_wificonnect show_Wificonnect;
    List list_show;
    SparseArray<String> mSparseArray = new SparseArray();
    SparseArray<Integer> mSparseArray_ap_level = new SparseArray();
    ScanResult mScanResult = null;

    public  long time_start_forsum;
    public  long time_end_forsum;
    public  long sum_time;
    public  long sum_time_foraverage;
    public  long time_foraverage_start;
    public  long time_foraverage_end;
    public  long time_foraverage_diff;
    public double time_average;
    public long time_for_total; // ui total time

    String start_date ,end_date;

    int stress_times_int;
    private long mExitTime;

    boolean flag_control=true; //control thread

    FileUtils mFileutil = new FileUtils();
    FileIOUtils mFileIOutils = new FileIOUtils();
    String fileName;
    String SD_path="/sdcard/";
    String mkName="wifi_stress/";
    String private_filename="wifi_stress.txt";

    private boolean flag_wifi_open =false;            //switch stress flag
    private boolean flag_wifi_scan =false;              //scan stress flag
    private boolean flag_wifi_connect =false;    //connect flag
    private boolean flag_wifi_ping =false;              //ping stress flag
    private boolean flag_wifi_disconnect =false;              //disconnect stress flag
    private boolean flag_wifi_close =false;              //close stress flag

    private boolean flag_quit= false; // control quit
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        InitView();
        if (wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)) ;
        {
            tv_show_wifi_ssid.setText(et_normal_wifi_ssid_str);
            tv_show_wifi_con_status.setText("Connected");
        }
        show_Wificonnect.start();
        mTestThread.start();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


    }

    public void InitView() {
        tv_show_wifi_con_status = findViewById(R.id.tv_show_wifi_connect_status);
        tv_show_wifi_ssid = findViewById(R.id.tv_show_wifi_ssid);
        tv_show_wifi_open = findViewById(R.id.tv_show_wifi_open);
        tv_show_scan = findViewById(R.id.tv_wifi_scan);
        tv_show_con=findViewById(R.id.tv_show_wifi_connect);
        tv_show_ping = findViewById(R.id.tv_show_wifi_ping);
        tv_show_discon=findViewById(R.id.tv_show_wifi_disconnect);
        tv_show_wifi_close=findViewById(R.id.tv_show_wifi_close);
        tv_show_scan_wifi_nums = findViewById(R.id.tv_wifi_scan_wifi_nums);
        tv_show_ssid1_level = findViewById(R.id.tv_wifi_ssid1_level);
        tv_show_ssid2_level = findViewById(R.id.tv_wifi_ssid2_level);
        tv_show_wifi_switcher_status = findViewById(R.id.tv_show_wifi_switcher_status);
        tv_round_times=findViewById(R.id.tv_show_round_times);
        tv_total_time=findViewById(R.id.tv_show_total_time);
        fileName = mFileutil.setFileName(private_filename);
        mFileIOutils.makeFilePath(SD_path,mkName,fileName);
        mFileutil.file_head_stress(SD_path+mkName+fileName);


        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifi_status_receiver, filter);


        String[] PERMS_INITIAL = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMS_INITIAL, 127);
        }

        wifiConnectionManager = new WIFIConnectionManager(this);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        show_Wificonnect = new Show_wificonnect();

       // stress_times_int = Integer.parseInt(et_stress_times_str);
    }

    class TestThread extends Thread {
        boolean wifi_auto_connect_flag = false;
        public static final String TAG = "wifi auto connect thead";
        public void run() {
            time_start_forsum=System.currentTimeMillis();
            start_date=mFileutil.getDate();
            while (true) {
                if (flag_control) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "run: origin i " + i);

                    //wifi open
                    if (!flag_wifi_open) {
                        flag_wifi_close=false;
                        mWifiManager.setWifiEnabled(true);
                        flag_wifi_open = true;
                        time_foraverage_start=System.currentTimeMillis();
                        Log.d(TAG, "run: wifi open");
                        Message msg = new Message();
                        msg.what = 0x1000;
                        mHandler.sendMessage(msg);
                    } else if (flag_wifi_open && !flag_wifi_scan) {
                        //wifi scan
                        try {
                            if (wifi_status_receiver.getWifistate() == 2) {
                                mWifiManager.startScan();
                                Log.d(TAG, "run: wifi scan");
                                Thread.sleep(1000);
                                list_show = wifi_status_receiver.getScanResult();
                                if (list_show != null) {
                                    Log.d(TAG, "run: wifi size"+list_show.size());
                                    flag_wifi_scan = true;
                                    for (int i = 0; i < list_show.size(); i++) {
                                        mScanResult = (ScanResult) list_show.get(i);
                                        mSparseArray_ap_level.put(i, mScanResult.level);
                                        mSparseArray.put(i, mScanResult.SSID);
                                    }
                                    Message message_wifi_num1 = new Message();
                                    message_wifi_num1.what = 0x1001;
                                    mHandler.sendMessage(message_wifi_num1);
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (flag_wifi_open && flag_wifi_scan && !flag_wifi_connect) {
                        //wifi connect
                        if (mWifiManager.isWifiEnabled()) {
                            Log.d(TAG, "run: wifi connect");
                            wifiConnectionManager.connect(et_normal_wifi_ssid_str, et_normal_wifi_password_str);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)) {
                                flag_wifi_connect = true;
                                Message msg = new Message();
                                msg.what = 0x1002;
                                mHandler.sendMessage(msg);
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else if (flag_wifi_open && flag_wifi_scan && flag_wifi_connect && !flag_wifi_ping) {
                        //wifi ping
                        Log.d(TAG, "run: wifi ping");
                        isPingSuccess(10, "www.baidu.com");
                        flag_wifi_ping = true;
                        if (status == 0) {
                           // flag_wifi_ping = true;
                            Message msg = new Message();
                            msg.what = 0x1003;
                            mHandler.sendMessage(msg);
                        }
                    }else if(flag_wifi_open && flag_wifi_scan && flag_wifi_connect && flag_wifi_ping&&!flag_wifi_disconnect){
                        Log.d(TAG, "run: wifi disconnecct");
                        wifiConnectionManager.disconnect();
                       // Log.d(TAG, "run: aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(!wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)){
                           // Log.d(TAG, "run: haslkhflahsdfhlshdlfjkhashdfjkahskdjjfhakjshdfkjahsdjklfhajkslhdfjkahsdljkfhajklshdlfkjahsljkddf");
                            flag_wifi_disconnect=true;
                            Message msg = new Message();
                            msg.what = 0x1004;
                            mHandler.sendMessage(msg);
                        }
                    }else if(flag_wifi_open && flag_wifi_scan && flag_wifi_connect && flag_wifi_ping&&flag_wifi_disconnect&&!flag_wifi_close){
                        Log.d(TAG, "run: wifi close");
                        mWifiManager.setWifiEnabled(false);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            if(wifi_status_receiver.getWifistate() == 0){
                                time_foraverage_end=System.currentTimeMillis();
                                time_foraverage_diff=time_foraverage_end-time_foraverage_start-5000;
                                sum_time_foraverage+=time_foraverage_diff;
                                Message msg = new Message();
                                msg.what = 0x1005;
                                mHandler.sendMessage(msg);
                                flag_wifi_close=true;
                                flag_wifi_open = false;
                                flag_wifi_scan = false;
                                flag_wifi_connect = false;
                                flag_wifi_ping = false;
                                flag_wifi_disconnect=false;
                                i++;
                                //Log.d(TAG, "run: asdfshdfahsjdhfashdjsfhaskhdfkgasjkgdfkashdfkjasgdfhagsdjk iiiiiiiiiiiii"+i);

                                Thread.sleep(3000);
                                msg = new Message();
                                msg.what = 0x1010;
                                mHandler.sendMessage(msg);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

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
                if (flag_control == true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)) {
                    Message message = new Message();
                    message.what = 0x1006;
                    mHandler.sendMessage(message);//show AC update wifi connect status to conneccted
                } else if (!wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)) {
                    Message message = new Message();
                    message.what = 0x1007;
                    mHandler.sendMessage(message); // show AC update wifi connect status to disconnected
                }

                if (mWifiManager.isWifiEnabled()) {
                    Message message = new Message();
                    message.what = 0x1008;
                    mHandler.sendMessage(message);//show AC update wifi switcher status to enable
                } else if (!mWifiManager.isWifiEnabled()) {
                    Message message = new Message();
                    message.what = 0x1009;
                    mHandler.sendMessage(message);//show AC update wifi switcher status to unable
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
            if (msg.what == 0x1000) {
                //wifi open
                tv_show_wifi_open.setText("Done");
                //mFileutil.result_append(SD_path+mkName+fileName,mFileutil.getDate()+"\t"+"*Close/Open Wifi Stress Test*"+"\t"+i_st+"\t"+"PASS"+"\t\n");
            }
            if (msg.what == 0x1001) {
                //wifi scan
                String wifi_nums = String.valueOf(list_show.size());
                //String wifi_scan_times =String.valueOf(++i-stress_times_int/4);
                String wifi_ssid1_level= String.valueOf(mSparseArray_ap_level.get(0,mScanResult.level));
                String wifi_ssid2_level= String.valueOf(mSparseArray_ap_level.get(1,mScanResult.level));
                String wifi_ssid3_level= String.valueOf(mSparseArray_ap_level.get(2,mScanResult.level));
                tv_show_ssid1_level.setText(mSparseArray.get(0, mScanResult.SSID)+"  "+wifi_ssid1_level);
                tv_show_ssid2_level.setText(mSparseArray.get(1, mScanResult.SSID)+"  "+wifi_ssid2_level);
                tv_show_scan.setText("Done");
                tv_show_scan_wifi_nums.setText(wifi_nums);

                //mFileutil.result_append(SD_path+mkName+fileName,mFileutil.getDate()+"\t"+"*Connect/Disconnect Wifi Stress Test*"+"\t"+i_st+"\t"+"PASS"+"\t\n");
            }
            if (msg.what == 0x1002) {
                //wifi connect
                tv_show_con.setText("Done");
                //mFileutil.result_append(SD_path+mkName+fileName,mFileutil.getDate()+"\t"+"*Wifi Ping Network Stress Test*"+"\t"+i_st+"\t"+"PASS"+"\t\n");
            }
            if (msg.what == 0x1003) {
                //wifi ping
                tv_show_ping.setText("Done");
            }
            if (msg.what == 0x1004) {
               //wifi disconect
                tv_show_discon.setText("Done");
            }
            if (msg.what == 0x1005) {
              //wifi close
                tv_show_wifi_close.setText("Done");
            }
            if (msg.what == 0x1006) {
                // wifi connect status  - connect
               tv_show_wifi_con_status.setText("Connected");

            }
            if(msg.what==0x1007){
                //wifi connect status  - disconnect
                tv_show_wifi_con_status.setText("Disconnected");
            }
            if(msg.what==0x1008){
                //wifi switcher status - enable
                tv_show_wifi_switcher_status.setText("True");
            }
            if(msg.what==0x1009){
                //wifi switcher status - unable
                tv_show_wifi_switcher_status.setText("False");
            }
            if(msg.what==0x1010){
                // reset test progress ui
                tv_show_wifi_open.setText("");
                tv_show_scan.setText("");
                tv_show_con.setText("");
                tv_show_ping.setText("");
                tv_show_discon.setText("");
                tv_show_wifi_close.setText("");
                tv_show_scan_wifi_nums.setText("");
                tv_show_ssid1_level.setText("");
                tv_show_ssid2_level.setText("");
                String i_st = String.valueOf(i);
                tv_round_times.setText(i_st);
                time_for_total=System.currentTimeMillis();
                sum_time=time_for_total-time_start_forsum;
                DecimalFormat df = new DecimalFormat("#.0000");
                Double sum_time_decimal= Double.valueOf(sum_time/3600000d);
                Double sum_time_decimal_4= Double.valueOf(df.format(sum_time_decimal));
                String sum_time_decimal_4_st = String.valueOf(sum_time_decimal_4);
                tv_total_time.setText(sum_time_decimal_4_st+" hours");
                mFileutil.result_append(SD_path+mkName+fileName,mFileutil.getDate()+"\t"+"*Wifi Open/Scan/Connect/Disconnect/Close Stress Test*"+"\t"+i_st+"\t"+"PASS"+"\t\n");
            }
            if(msg.what==0x1011){
                if(i==0){
                    mFileutil.result_append(SD_path+mkName+fileName,"Data Statistics："+"\n"+"["+start_date+"]"+"--"+"["+end_date+"]"+"\t\n"+"You test less than 1 ,not to statistics "+"\t\n");
                }else if(i>0){
                    sum_time=time_end_forsum-time_start_forsum;
                    time_average=sum_time_foraverage/i/1000d;
                    DecimalFormat df = new DecimalFormat("#.0000");
                    Double sum_time_decimal= Double.valueOf(sum_time/3600000d);
                    Double sum_time_decimal_4= Double.valueOf(df.format(sum_time_decimal));
                    Log.d(TAG, "handleMessage: decimal"+sum_time_decimal);
                    //String time_average_st = String.valueOf(time_average);
                    mFileutil.result_append(SD_path+mkName+fileName,"Data Statistics: "+"\n"+"["+start_date+"]"+"--"+"["+end_date+"]"+"\t\n"+"Test Duration:"+"\t"+sum_time_decimal_4+" hours"+"\t\n"+"Test Times: "+"\t"+i+"\n"+"Test Average Time: "+time_average+" s"+"\t\n");
                    Toast.makeText(ShowActivity.this,"数据统计成功，请按返回键退出",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };



  /*  private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: herehere");
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                List<ScanResult> list = mWifiManager.getScanResults();
                Log.d(TAG, "onReceive: wifi scan"+list);
                Log.d(TAG, "onReceive: wifi scan"+list);
                Log.d(TAG, "onReceive: wifi scan"+list);
            }
        }
    };*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(!flag_quit){
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                exit();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            View view;
            AlertDialog.Builder builder= new AlertDialog.Builder(ShowActivity.this);
            view= LayoutInflater.from(ShowActivity.this).inflate(R.layout.dialog_save, null);
            TextView cancel =view.findViewById(R.id.dialog_cancel);
            TextView sure =view.findViewById(R.id.dialog_sure);
            tv_testdata=view.findViewById(R.id.tv_testdata);
            tv_testdata.setText("\n"+SD_path+mkName+fileName+"\n\n");
            final Dialog dialog= builder.create();
            dialog.show();
            dialog.getWindow().setContentView(view);
            cancel.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: cancel");
                    dialog.dismiss();
                }
            });
            sure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: sure");
                    flag_control=false;
                    time_end_forsum=System.currentTimeMillis();
                    end_date=mFileutil.getDate();
                    //time_end_forsum=System.currentTimeMillis();
                    //end_date=mFileutil.getDate();
                    //finish();
                    dialog.dismiss();
                    flag_quit=true;
                    Message msg = new Message();
                    msg.what=0x1011;
                    mHandler.sendMessage(msg);
                }

            });
            mExitTime = System.currentTimeMillis();
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifi_status_receiver);
        flag_control=false;
    }
}
