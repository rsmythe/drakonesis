package io.heavymeta.drakonesis.DragonConnect;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.content.Context;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import io.heavymeta.drakonesis.DragonCommand.DragonAction;
import io.heavymeta.drakonesis.MainActivity;
import io.heavymeta.drakonesis.R;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by Ryan on 5/6/2017.
 */

public class DragonConnect {
    private static final String LOG_TAG = MainActivity.class.getName();

    private String _ip;
    private int _port;

    private static final int hack = 48;

    private android.net.wifi.WifiManager _wifiManager;

    private Context _context;

    public DragonConnect(String ip, int port, Context context)
    {
        this._ip = ip;
        this._port = port;
        this._context = context;
    }

    public void SendAction(DragonAction id) {
        (new ConnectTask()).execute(new byte[]{(byte) (id.getValue() + hack)});
    }

    public void onPause()
    {
        this._wifiManager.setWifiEnabled(false);
    }

    public void onResume()
    {
        _wifiManager = (WifiManager) this._context.getApplicationContext().getSystemService(WIFI_SERVICE);
        this.CreateWifiAccessPoint();
    }

    private class ConnectTask extends AsyncTask<byte[], Void, Void> {
        protected Void doInBackground(byte[]... arg0) {
            Send(arg0[0]);
            return null;
        }
    }

    private void Send(byte[] msg) {
        try {
            InetAddress ip = InetAddress.getByName(this._ip);
            DatagramSocket s = new DatagramSocket();
            DatagramPacket p = new DatagramPacket(msg, msg.length, ip, this._port);
            s.send(p);
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private WifiConfiguration getWifiConfig() {
        Context context = this._context.getApplicationContext();

        WifiConfiguration netConfig = new WifiConfiguration();
        netConfig.SSID = context.getString(R.string.ssid);
        //netConfig.preSharedKey = context.getString(R.string.wireless_password);
        //netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        //netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        return netConfig;
    }

    private void CreateWifiAccessPoint() {
        if (_wifiManager.isWifiEnabled()) {
            _wifiManager.setWifiEnabled(false);
        }

        // Get all declared methods in WifiManager class
        Method[] wmMethods = _wifiManager.getClass().getDeclaredMethods();

        boolean methodFound = false;
        for (Method method : wmMethods) {
            if (method.getName().equals("setWifiApEnabled")) {
                methodFound = true;

                WifiConfiguration netConfig = getWifiConfig();

                try {
                    boolean apstatus = (Boolean) method.invoke(_wifiManager, netConfig, true);

                    Log.e(LOG_TAG,
                            String.format("Creating a Wi-Fi Network %s", netConfig.SSID));
                    for (Method isWifiApEnabledmethod : wmMethods) {
                        if (isWifiApEnabledmethod.getName().equals("isWifiApEnabled")) {
                            while (!(Boolean) isWifiApEnabledmethod.invoke(_wifiManager)) {
                            }

                            for (Method method1 : wmMethods) {
                                if (method1.getName().equals("getWifiApState")) {
                                    Log.e(LOG_TAG, String.format("SSID: %s, Password: %s",
                                            netConfig.SSID, netConfig.preSharedKey));
                                }
                            }
                        }
                    }
                    if (apstatus) {
                        Log.e(LOG_TAG, "Access Point Created!");
                    } else {
                        Log.e(LOG_TAG, "Access Point Creation failed!");
                    }
                } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!methodFound) {
            Log.e(
                    LOG_TAG,
                    "Your phone's API does not contain setWifiApEnabled method to configure an access point");
        }
    }
}
