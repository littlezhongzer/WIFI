package com.example.quectel.quectel_stress;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;


import static android.content.ContentValues.TAG;


public class ListViewActivity extends Activity implements View.OnClickListener{

    ArrayList<Group> groups;
    ExpandableListView listView;
    EListAdapter adapter;

    SparseArray<Boolean> mySparseArray = new SparseArray();


    public WIFIConnectionManager wifiConnectionManager;
    WifiConnect myWifiConnect;
    public static WifiManager  mainWifiManager;

    Context context;

    Button btn_start,btn_all_check;
    static boolean flag_btn_start=false;
    static boolean flag_btn_allcheck_group=true;

    //TestThread mTestthread;
    int status;

    Bundle mbundle = new Bundle();

    int checked;


    public static String et_stress_times_str,et_normal_wifi_ssid_str,et_normal_wifi_password_str;


   // private String ssid = "Xiaomi_4CBF";
   // private String password = "quectel88";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        myWifiConnect = new WifiConnect();
        InitView();

        if(et_normal_wifi_ssid_str.equals("") | et_normal_wifi_password_str.equals("")){
            Log.d(TAG, "onCreate: wifi is null");
        }else {
            myWifiConnect.start();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    /** 解悉 JSON 字串 */
    private void getJSONObject() {
        String jsonStr = "{'CommunityUsersResult':[{'CommunityUsersList':[{'fullname':'Wifi打开关闭','userid':11,'username':'a1'}"
                + ",{'fullname':'扫描AP','userid':12,'username':'b2'}"
                +",{'fullname':'连接AP','userid':12,'username':'b2'}"
                +",{'fullname':'ping网络','userid':12,'username':'b2'}"
                +",{'fullname':'连接AP信号强度','userid':12,'username':'b2'}],'id':1,'title':'功能验证'}"
                +",{'CommunityUsersList':[{'fullname':"
                + "'Wifi打开关闭','userid':13,'username':'c3'}"
                +",{'fullname':'扫描AP','userid':14,'username':'d4'}"
                +",{'fullname':'wifi连接断开','userid':"
                + "15,'username':'e5'}"
                +",{'fullname':'Ping压力','userid':12,'username':'b2'}"
                +",{'fullname':'AP信号强度','userid':12,'username':'b2'}],'id':2,'title':'Wlan压力测试'}"
                +",{'CommunityUsersList':[{'fullname':'接收数据','userid':11,'username':'a1'}"
                + ",{'fullname':'接收数据速率','userid':12,'username':'b2'}"
                +",{'fullname':'传送数据','userid':12,'username':'b2'}"
                +",{'fullname':'传送数据速率','userid':12,'username':'b2'}],'id':1,'title':'Wlan性能测试'}"
              //  +",{'fullname':'建链压力','userid':12,'username':'b2'}],}"
               // +",{'CommunityUsersList':[{'fullname':'跨AP','userid':12,'username':'b2'}"
              //  +",{'fullname':'连接异常','userid':12,'username':'b2'}"
               // +",{'fullname':'禁止DHCP','userid':12,'username':'b2'}],'id':1,'title':'Wlan异常测试'}"
                // +",{'CommunityUsersList':[],'id':1,'title':'AP兼容性测试'}"
                +",{'CommunityUsersList':[],'id':1,'title':'漫游测试'}]}";

        try {
            JSONObject CommunityUsersResultObj = new JSONObject(jsonStr);
            JSONArray groupList = CommunityUsersResultObj.getJSONArray("CommunityUsersResult");

            for (int i = 0; i < groupList.length(); i++) {
                JSONObject groupObj = (JSONObject) groupList.get(i);
                Group group = new Group(groupObj.getString("id"), groupObj.getString("title"));
                JSONArray childrenList = groupObj.getJSONArray("CommunityUsersList");

                for (int j = 0; j < childrenList.length(); j++) {
                    JSONObject childObj = (JSONObject) childrenList.get(j);
                    Child child = new Child(childObj.getString("userid"), childObj.getString("fullname"),
                            childObj.getString("username"));
                    group.addChildrenItem(child);
                }

                groups.add(group);
            }
        } catch (JSONException e) {
            Log.d("quectel", e.toString());
        }
    }

    public void InitView(){
        btn_start=findViewById(R.id.btn_click_start);
        btn_all_check=findViewById(R.id.btn_click_allcheck);
        btn_start.setOnClickListener(this);
        btn_all_check.setOnClickListener(this);

        wifiConnectionManager = new WIFIConnectionManager(this);
        mainWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //mTestthread = new TestThread();
        groups = new ArrayList<Group>();
        getJSONObject();
        listView = (ExpandableListView) findViewById(R.id.expend_list);
        adapter = new EListAdapter(this, groups);
        listView.setAdapter(adapter);
        listView.setOnChildClickListener(adapter);

        mbundle = this.getIntent().getExtras();
        et_normal_wifi_ssid_str= mbundle.getString("wifi_ssid");
        et_normal_wifi_password_str= mbundle.getString("wifi_password");
        et_stress_times_str = mbundle.getString("stress_time");
        Log.d(TAG, "InitView: " +et_normal_wifi_ssid_str);
        Log.d(TAG, "InitView: " +et_normal_wifi_password_str);
        Log.d(TAG, "InitView: " +et_stress_times_str);


    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_click_start:
                checked=0;
                flag_btn_start=true;
                    //last one ,because of adapter android 9 to avoid process can't run
                if(!et_normal_wifi_ssid_str.equals("") && !et_normal_wifi_password_str.equals("") && wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)) {
                    for (int i = 0; i < groups.size(); i++) {
                        mySparseArray.put(i, groups.get(i).getChecked());
                        if (mySparseArray.get(i)) {
                            checked++;
                        }
                    }
                    //ban multi check
                    if (checked > 1) {
                        Log.d(TAG, "onClick: break");
                        Toast.makeText(this, "一次只能选择一个选项，请重新选择", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (mySparseArray.get(0)) {
                        Log.d(TAG, "onClick: get0");
                        Intent intent1 = new Intent();
                        intent1.setClass(ListViewActivity.this, SimpleCheck_Activity.class);
                        startActivity(intent1);
                    }
                    if (mySparseArray.get(1)) {
                            Intent intent2 = new Intent();
                            intent2.setClass(ListViewActivity.this, ShowActivity.class);
                            startActivity(intent2);

                    }
                    if (mySparseArray.get(2)) {
                        Intent intent3 = new Intent();
                        intent3.setClass(ListViewActivity.this, PerformanceActivity.class);
                        startActivity(intent3);
                    }
                    if (mySparseArray.get(3)) {
                        Intent intent3 = new Intent();
                        intent3.setClass(ListViewActivity.this, WifiRoamAC.class);
                        startActivity(intent3);
                    }

                }else if(!et_normal_wifi_ssid_str.equals("") && !et_normal_wifi_password_str.equals("") && !wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)){
                    Toast.makeText(this,"请等待wifi连接成功",Toast.LENGTH_SHORT).show();
                }else if(et_normal_wifi_ssid_str.equals("") | et_normal_wifi_password_str.equals("")){
                    Toast.makeText(this,"没有输入Wifi",Toast.LENGTH_SHORT).show();
                }


                break;
            case R.id.btn_click_allcheck:
                if(flag_btn_allcheck_group==true) {
                    Message message_allcheck = new Message();
                    //handler.removeMessages(message_allcheck.what);
                    message_allcheck.what = 0x11;
                    handler.sendMessageDelayed(message_allcheck, 100);
                    btn_all_check.setText("重置");
                    flag_btn_allcheck_group=false;
                    //flag_btn_allclear=true;
                    break;
                }
                if(flag_btn_allcheck_group==false){Message message_allclear = new Message();
                    message_allclear.what = 0x12;
                    handler.sendMessage(message_allclear);
                    btn_all_check.setText("全选");
                    flag_btn_allcheck_group=true;
                    //flag_btn_allclear=false;
                    Log.d(TAG, "Main Click all clear:" + flag_btn_allcheck_group);
                    break;
                }
                Log.d(TAG, "Main Click all check flag:"+ flag_btn_allcheck_group);


        }
    }


    @SuppressLint("HandlerLeak")
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x11) {
                for(int i=0;i<groups.size();i++) {
                    Log.d(TAG, "handleMessage: "+groups.size());
                    groups.get(i).setChecked(true);
                    mySparseArray.put(i,groups.get(i).getChecked());
                    Log.d(TAG, "onClick: start click sparsearray "+ mySparseArray.get(0));
                    Log.d(TAG, "onClick: start click sparsearray "+ mySparseArray.get(1));
                    Log.d(TAG, "onClick: start click sparsearray "+ mySparseArray.get(2));
                    Log.d(TAG, "onClick: start click sparsearray "+ mySparseArray.get(3));
                    Log.d(TAG, "onClick: start click sparsearray "+ mySparseArray.get(4));
                    Log.d(TAG, "onClick: start click sparsearray "+ mySparseArray.get(5));
                    adapter.notifyDataSetChanged();
                }
                // removeMessages(newMsg.what);
            }else if (msg.what==0x12) {
                for(int ii=0;ii<groups.size();ii++) {
                    groups.get(ii).setChecked(false);
                    mySparseArray.put(ii,groups.get(ii).getChecked());
                    Log.d(TAG, "onClick: start click sparsearray "+ mySparseArray.get(0));
                    Log.d(TAG, "onClick: start click sparsearray "+ mySparseArray.get(1));
                    Log.d(TAG, "onClick: start click sparsearray "+ mySparseArray.get(2));
                    Log.d(TAG, "onClick: start click sparsearray "+ mySparseArray.get(3));
                    Log.d(TAG, "onClick: start click sparsearray "+ mySparseArray.get(4));
                    Log.d(TAG, "onClick: start click sparsearray "+ mySparseArray.get(5));
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };


    class WifiConnect extends Thread {

        boolean wifi_auto_connect_flag=false;
        public static final String TAG="wifi auto connect thead";

        public void run() {
            while (true) {

                if(wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)){               //如果之前有连接过默认的wifi,检测到成功，停止线程循环
                    break;
                }
                //连接5次edittext wifi,连接不成功发送消息
                for(int a=0;a<10;a++){
                    wifiConnectionManager.connect(et_normal_wifi_ssid_str,et_normal_wifi_password_str);
                    //Log.d(TAG, "run: assssssssssssssssssssssssssssssssssssssssssssssssssssssssssss");
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "run: for connect "+et_normal_wifi_ssid_str +et_normal_wifi_password_str);
                    if(wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)){
                        return;
                        //如果之前有连接过edittext的wifi,for不走完，检测到连接成功，直接发消息并停止for循环
                    }
                }
                if(!wifiConnectionManager.isConnected(et_normal_wifi_ssid_str)){
                   // Log.d(TAG, "run: aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                    Message message = new Message();
                    message.what=0x000;
                    mHandler.sendMessage(message);
                    break;
                }




            }
        }
    }







    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==0x000){
                // test_tv.append("Wifi Connected\n"+"Wifi SSID = "+str_ssid+"\n");
                Log.d(TAG, "handleMessage: 0x000");
                Toast.makeText(ListViewActivity.this,"尝试连接Wifi 10次失败，请检查密码或者路由器是否正确",Toast.LENGTH_LONG).show();

            }
            if(msg.what==0x001) {
                //test_tv.append("wifi: "+ str_ssid +" 尝试连接10次仍未连接成功，请检查wifi状态"+ "\n");
            }
            if(msg.what==0x002){
                // test_tv.append("默认Wifi "+ ssid+" 未连接上，请确认Wifi状态，或者手动输入一个wifi "+"\n");
            }
            if(msg.what==0x003){
                //test_tv.append("Enabling Wifi\n");
            }
            if(msg.what==0x004){
                /// test_tv.append("Wifi Already Enabled\n");
            }
            if(msg.what==0x005) {
                // test_tv.append("Wifi Connected\n"+ "Wifi SSID = " +ssid +"\n");
                Log.d(TAG, "handleMessage: receive msg");
            }
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        //unregisterReceiver(wifi_status_receiver);
    }

}
