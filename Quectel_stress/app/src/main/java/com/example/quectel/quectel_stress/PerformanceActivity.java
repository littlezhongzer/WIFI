package com.example.quectel.quectel_stress;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.quectel.quectel_stress.Receiver.count;
import static com.example.quectel.quectel_stress.Receiver.rate;
import static com.example.quectel.quectel_stress.Receiver.sock;
import static com.example.quectel.quectel_stress.Receiver.t;

public class PerformanceActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String TAG="performance ac";
    Button btn_sender,btn_receiver;
    TextView tv_show_receiver_ip,tv_received_data,tv_received_speed;
    EditText edittext_ip,editText_port;
    WifiInfo wifiInfo;
    public static String input_ip;
    public static String input_port;
    public WifiManager mWifiManager;
    Receiver receiver_thread;
    public static boolean flag_receiver=false;
    public static boolean flag_sender=false;
    Received_Data received_data = new Received_Data();



    FileUtils mFileutil = new FileUtils();
    FileIOUtils mFileIOutils = new FileIOUtils();
    public static String fileName;
    public static String SD_path="/sdcard/";
    public static String mkName="wifi_performance/";
    String private_filename="wifi_received.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance);
        initView();
        showAlterDialog_perform();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void initView(){
        btn_sender=findViewById(R.id.btn_sender);
        btn_sender.setOnClickListener(this);
        btn_receiver=findViewById(R.id.btn_receiver);
        btn_receiver.setOnClickListener(this);
        tv_show_receiver_ip=findViewById(R.id.tv_show_receiver_ip);
        tv_received_data=findViewById(R.id.tv_received_data);
        tv_received_speed=findViewById(R.id.tv_received_speed);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        fileName = mFileutil.setFileName(private_filename);
        mFileIOutils.makeFilePath(SD_path,mkName,fileName);
        mFileutil.file_head_performance(SD_path+mkName+fileName);

        receiver_thread = new Receiver();

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_sender:
                flag_receiver=false;
                input_ip=null;
                input_port=null;
                tv_show_receiver_ip.setText("");
                AlertDialog.Builder builder= new AlertDialog.Builder(PerformanceActivity.this);
                view= LayoutInflater.from(PerformanceActivity.this).inflate(R.layout.dialog, null);
                TextView cancel =view.findViewById(R.id.dialog_cancel);
                TextView sure =view.findViewById(R.id.dialog_sure);
                edittext_ip =view.findViewById(R.id.dialog_edittext_ip);
                editText_port=view.findViewById(R.id.dialog_edittext_port);
                final Dialog dialog= builder.create();
                dialog.show();
                dialog.getWindow().setContentView(view);
                //使editext可以唤起软键盘
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                cancel.setOnClickListener(new View.OnClickListener() {


                    @Override
                    public void onClick(View v) {
                        flag_sender=false;
                        input_ip=null;
                        input_port=null;
                        Toast.makeText(PerformanceActivity.this, "取消", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        input_ip=edittext_ip.getText().toString();
                        input_port=editText_port.getText().toString();
                        Toast.makeText(PerformanceActivity.this, "输入成功", Toast.LENGTH_SHORT).show();
                        flag_sender=true;
                        Log.d(TAG, "onClick: "+input_port);
                        Log.d(TAG, "onClick: input ip"+input_ip);


                        Message message = new Message();
                        message.what=0x3000;
                        mHandler.sendMessage(message);
                        new Sender(input_ip,input_port).start();
                        dialog.dismiss();
                    }
                });
                break;
            case R.id.btn_receiver:
                flag_sender=false;
                input_ip=null;
                input_port=null;
                flag_receiver=true;
                received_data.start();
                receiver_thread.start();
                wifiInfo = mWifiManager.getConnectionInfo();
                Message message2 = new Message();
                message2.what=0x3001;
                mHandler.sendMessage(message2);
                break;

        }
    }

    class Received_Data extends Thread {
        public static final String TAG="receive data thead";

    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(sock!=null){
               // Log.d(TAG, "run: sock not null");
                if(!sock.isClosed()&&sock.isConnected()){

                       // Log.d(TAG, "run: run here");
                        Message message = new Message();
                        message.what=0x3002;
                        mHandler.sendMessage(message);

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
            if (msg.what == 0x3000) {
                tv_show_receiver_ip.setText("此应用为发送端"+"\n"+"输入的IP地址为： "+input_ip+"\n"+"Port： "+input_port);
            }
            if (msg.what == 0x3001) {
                String receiver_ip = intToIp(wifiInfo.getIpAddress());
                tv_show_receiver_ip.setText("此应用为接收端"+"\n"+"接收端IP为： "+receiver_ip+"\n"+"Port： "+"3358");
            }
            if (msg.what == 0x3002) {
               // Log.d(TAG, "handleMessage: ");
              //  Log.d(TAG, "handleMessage: "+re_data+count);
                if(count==0){

                }else {
                    String re_data_str = String.valueOf(count);
                    tv_received_data.setText(re_data_str + " B");
                    tv_received_speed.setText(count + "B data" + " in " + t + " ms" + " at rate:" + rate + " kB/second\n" + "Mbps： " + rate * 8 / 1024 + "Mbps");
                }
            }
        }
    };

    private String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }


    private void showAlterDialog_perform(){
        final AlertDialog.Builder alterDiaglog = new AlertDialog.Builder(PerformanceActivity.this);
        alterDiaglog.setTitle("提示！！");//文字
        alterDiaglog.setMessage("需要将测试机和辅助机屏幕都设置为常亮 -->方法：设置-显示");//提示消息
        //积极的选择
        alterDiaglog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        //显示
        alterDiaglog.show();
    }


    protected void onDestroy(){
        super.onDestroy();
        flag_receiver=false;
        flag_sender=false;
        input_port=null;
        input_ip=null;
    }

}


