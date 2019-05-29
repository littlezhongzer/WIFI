package com.example.quectel.quectel_stress;


import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static com.example.quectel.quectel_stress.PerformanceActivity.SD_path;
import static com.example.quectel.quectel_stress.PerformanceActivity.fileName;
import static com.example.quectel.quectel_stress.PerformanceActivity.flag_receiver;
import static com.example.quectel.quectel_stress.PerformanceActivity.mkName;

class Receiver extends Thread {
    public static final String TAG = "receiver class";
    private static BufferedReader in;
    public static long count = 0;
    public static long rate;
    public static Socket sock;
    public static long time;
    public static long t;
    FileUtils mFileutil = new FileUtils();
    public void run() {
        while (true) {
            if (flag_receiver) {
                try {
                    ServerSocket socketService = new ServerSocket(3358);
                    Log.d(TAG, "waiting a connection from the client " + socketService);
                    sock = socketService.accept();
                    String hostAddress = sock.getLocalAddress().getHostAddress();
                    String inetAddress = sock.getInetAddress().getHostAddress();
                    Log.d(TAG, "local:" + hostAddress + "| inetAddress" + inetAddress + "|" + sock.getRemoteSocketAddress());
                    Log.d(TAG, "local name:" + sock.getLocalAddress().getHostName() + "| inetAddress" + sock.getInetAddress().getHostName() + "|" + InetAddress.getLocalHost().getHostAddress());
                    in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    String line;
                    boolean flag = true;
                    count = 0;
                    time = System.currentTimeMillis();


                    line = in.readLine();
                    if (flag) {
                        //console.displayMesage("Recevie:" + line);
                        Log.d(TAG, "run:here1 ");
                        flag = false;
                    }
                    count = count + line.length();  //返回是 B  ==>一个long 8byte,所以count 的长度就是多少B
                    Log.d(TAG, "run: here 2 " + count + "B data");
                    if (count % 1024 == 0) {
                        //console.displayMesage("Recevied:" + ((count << 1) >> 10) + "kB data");
                        Log.d(TAG, "run: here 3" + ((count << 1) >> 10) + "kB data");
                    }
                    Log.w("Quectel", "you input is :" + line);
                    t = System.currentTimeMillis() - time;
                    if (t == 0) {
                        t = 1;
                    }
                    //count = count << 1;
                    rate = ((count / t) * 1000) / 1024;
                    //count = count >> 10;
                    Log.i("Quectel", "exit the app" + count + "B data" + " in " + t + " ms" + " at rate:" + rate + " kB/second");
                    sock.close();
                    socketService.close();
                    String re_data_str = String.valueOf(count);
                    mFileutil.result_append(SD_path + mkName + fileName, mFileutil.getDate() + "\t" + re_data_str +"B"+ "\t" + t + "ms" + "\t" + rate * 8 / 1024 + "Mbps" + "\t\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 返回传输数据大小
     * @return
     */
    public static long getCount(){
        count+=count;
        return count;
    }
}
