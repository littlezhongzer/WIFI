package com.example.quectel.quectel_stress;

import java.io.File;
import java.io.IOException;

/**
 * Created by loyal.zhong on 2019/4/9.
 */

class Logcat_class extends Thread {
    private static final String TAG = "savefileactivity";
    static Process app_logcat;
    public void run() {
       if (MainActivity.flag_app_logcat_prc == true) {
            //creat logcat file
            File file1 = new File("/sdcard/WiFi_logcat.txt");//创建文件
            if (!file1.exists()) {
                if (file1.exists()) {
                    try {
                        file1.delete();
                        file1.createNewFile();
                        //Toast.makeText(this, "创建成功！", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                try {
                    file1.createNewFile();
                    // Toast.makeText(this, "创建成功！", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                app_logcat = Runtime.getRuntime().exec("logcat -v time -f /sdcard/wifi_stress_logcat.txt");
            } catch (IOException e) {
                e.printStackTrace();
            }
            //ShellUtils.execCommand( "logcat  -c" ,false);
            //ShellUtils.execCommand( "logcat -b main -v time -f /mnt/sdcard/stress_logcat/app.log" ,false);


        }else {return;}
    }
}



