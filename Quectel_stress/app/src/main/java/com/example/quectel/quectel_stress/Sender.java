package com.example.quectel.quectel_stress;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import static com.example.quectel.quectel_stress.PerformanceActivity.flag_sender;
import static com.example.quectel.quectel_stress.PerformanceActivity.input_ip;


class Sender extends Thread {
    //DisplayMesage console;
    public static final String TAG = "sender class";
    String serverIp;
    String port;

    // ITransferResult transferResult;
    Sender(String serverAddress, String Port) {
        super();
        serverIp = serverAddress;
        port = Port;
    }
    public void run() {
        while (true) {
            if (flag_sender) {
            Socket sock = null;
            PrintWriter out = null;
            if (port.isEmpty()) {
                Log.d(TAG, "run: sender port" + port + "return");
                return;
            }
            try {
                // 声明sock，其中参数为服务端的IP地址与自定义端口
                Log.d(TAG, "serverip: " + serverIp);
                sock = new Socket(input_ip, Integer.parseInt(port));
                Log.d(TAG, "I am try to writer" + sock);
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            char data[] = new char[1024 * 10];
            for (int i = 0; i < data.length; i++) {
                data[i] = (char) i;
            }

            if (sock != null) {
                Random random = new Random();
                int number = random.nextInt(62) + 2000;
                String msg = getRandomString(number);
                // 声明输出流out，向服务端输出“Output Message！！”
                // String msg = "Hello,Quectel.Hello,Quectel.Hello,Quectel.Hello,Quectel.Hello,Quectel.Hello,Quectel.Hello,Quectel.Hello,Quectel.啥都好说电话方式的咖啡机水电费水电费水电费京津冀发欧舒丹佛奥手动阀好哦按时豆花饭";
                Log.d(TAG, "try to writer");
                try {
                    out = new PrintWriter(sock.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                StringBuffer strBuffer = new StringBuffer();
                strBuffer.append(msg);
                String str = msg;
                Log.w("Quectel", str);
                for (int i = 0; i < 2048; i++) {
                    if (i != 0) {
                        str = msg + System.currentTimeMillis() + "|";
                        out.write(data);
                    }
                    out.println(str);
                    if (i == 0) {
                        //console.displayMesage("send message....");
                    } else if (i % 100 == 0) {
                        //console.displayMesage("send message " + i + " success!");
                    }
                    if (strBuffer.length() > 1024) {
                        strBuffer.delete(0, strBuffer.length());
                    }
                }
                //out.println(Constant.END);
                out.flush();
            }
        }
    }
    }

    public static String getRandomString(int length){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length;i++){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
