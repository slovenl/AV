package com.tencent.audiochanneldemo.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.tencent.audiochanneldemo.AudioChannelDemoActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static android.text.format.Formatter.formatIpAddress;

/**
 * Created by zoroweili on 2019-2-21.
 */

public class DemoUtil {
    private static final String TAG = "DemoUtil";
    /**
     * 录音采样率
     */
    public static final int SAMPLE_RATE_IN_HZ = 44100;

    public static String getWavFilePath(Context context) {
        if (context == null) {
            return null;
        }
        String path = context.getApplicationContext().getFilesDir()
            .getAbsolutePath() + "/test44.wav"; // data/data目录
        return path;
    }

    public static String getMicFilePath(Context context, String fileName) {
        if (context == null) {
            return null;
        }
        String path = context.getApplicationContext().getFilesDir()
            .getAbsolutePath() + "/" + fileName; // data/data目录
        return path;
    }

    public static void copyFiletoData(Context context)
    {
        if (context == null) {
            return;
        }
        InputStream in = null;
        FileOutputStream out = null;
        String path = getWavFilePath(context);
        System.out.println(path);
        Log.i("DemoUtil", "path " + path);
        File file = new File(path);
        if (!file.exists()) {
            try
            {
                in = context.getAssets().open("test44.wav"); // 从assets目录下复制
                out = new FileOutputStream(file);
                int length = -1;
                byte[] buf = new byte[1024];
                while ((length = in.read(buf)) != -1)
                {
                    out.write(buf, 0, length);
                }
                out.flush();
                System.out.println("file  test44.wav copy success");
            }
            catch (Exception e)
            {
                System.out.println("file  test44.wav open fail the file is exist???");
                e.printStackTrace();
            }
            finally{
                if (in != null)
                {
                    try {

                        in.close();

                    } catch (IOException e1) {

                        // TODO Auto-generated catch block
                        System.out.println("file  test44.wav open fail the file is exist???");
                        e1.printStackTrace();
                    }
                }
                if (out != null)
                {
                    try {
                        out.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        System.out.println("file  test44.wav open fail the file is exist???");
                        e1.printStackTrace();
                    }
                }
            }
        }else{
            System.out.println("test44.wav file is exist");
            Log.i("DemoUtil", "test44.wav file is exist");
        }
    }

    /**
     * 用于音频处理中，将帧数大小转换成时间（单位毫秒）。
     * <p>
     * 注意，其默认采样率为44100，声道数为2，位深为2(字节)
     *
     * @param frameSize
     *            帧数大小
     * @return 时间（单位毫秒）
     */
    public static int frameSizeToTimeMillis(int frameSize) {
        return frameSizeToTimeMillis(frameSize, SAMPLE_RATE_IN_HZ);
    }

    /**
     * 用于音频处理中，将字节大小转换成时间（单位毫秒）。
     *
     * @param sampleRate
     *            采样率
     * @param frameSize
     *            帧数大小
     * @return 时间（单位毫秒）
     */
    public static int frameSizeToTimeMillis(int frameSize, int sampleRate) {
        // 时长，单位秒
        double duration = frameSize / sampleRate;
        // 时长，单位毫秒
        double d = (duration * 1000);
        return (int)d;
    }

    public static String getIp(Context context) {
        if (context == null) {
            return "";
        }
        String ip = "";
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            ip = getLocalIp();
            Log.d(TAG,"getLocalIp1--->:" + ip);
            return ip;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if(wifiInfo!= null) {
            int ipAddress = wifiInfo.getIpAddress();
            ip = formatIpAddress(ipAddress);
            Log.d(TAG,"wifiInfoIp:" + ip);
        }else {
            ip = getLocalIp();
            Log.d(TAG,"getLocalIp2--->:" + ip);
        }
        return ip;
    }

    /**
     * 得到有限网关的IP地址
     *
     * @return
     */
    public static String getLocalIp() {
        try {
            // 获取本地设备的所有网络接口
            Enumeration<NetworkInterface> enumerationNi = NetworkInterface
                    .getNetworkInterfaces();
            if(enumerationNi == null)
                return "";
            while (enumerationNi.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNi.nextElement();
                String interfaceName = networkInterface.getDisplayName();
                Log.i("DemoUtil", "网络名字" + interfaceName);

                // 如果是有线网卡
                if (interfaceName != null && interfaceName.equals("eth0")) {
                    Enumeration<InetAddress> enumIpAddr = networkInterface
                            .getInetAddresses();

                    while (enumIpAddr.hasMoreElements()) {
                        // 返回枚举集合中的下一个IP地址信息
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        // 不是回环地址，并且是ipv4的地址
                        if (!inetAddress.isLoopbackAddress()
                                && inetAddress instanceof Inet4Address) {
                            Log.i("DemoUtil", inetAddress.getHostAddress() + "   ");

                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";

    }
}
