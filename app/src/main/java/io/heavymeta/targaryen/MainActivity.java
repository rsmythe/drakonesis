package io.heavymeta.targaryen;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import io.heavymeta.targaryen.views.EmittersView;

public class MainActivity extends Activity {
    public static final String intent = "io.kate.coatrack.update";
    public static final String LOG_TAG = MainActivity.class.getName();
    public static final int NUM_EFFECTS_IN_RING = 8;

    /* Turn off an effects if it has not been triggered in this many milliseconds */
    static final int EFFECT_OFF_TIMEOUT = 100;
    static final int EFFECT_REFRESH_TIMEOUT = 50;

    Button buttonEruption;
    Button buttonEruption2;
    Button buttonPinwheel;
    Button buttonRandom;

    public String ipString;
    public int port = 2000;

    static final int hack = 48;
    
    WifiManager wifiManager;

    EmittersView ringView;

    SharedPreferences prefs;

    PendingIntent pintent;

    private boolean meditationShouldFire = false;
    private boolean attentionShouldFire = false;

    private long step;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ringView == null || ringView.getEmitters() == null) {
                return;
            }

            for (EmittersView.EmitterView emitter : ringView.getEmitters()) {
                if (System.currentTimeMillis() - emitter.lastActivated > EFFECT_OFF_TIMEOUT) {
                    if (emitter.touching) {
                        triggerEffect(emitter.id);
                    } else {
                        emitter.intensity = 0;
                    }
                    ringView.postInvalidate();
                }
            }

            if (buttonEruption2.isPressed() || buttonEruption.isPressed() || meditationShouldFire || attentionShouldFire) {
                triggerEffects(new int[]{0, 1, 2, 3, 4, 5, 6, 7});
            }

            if (buttonRandom.isPressed()) {
                doRandom();
            }

            if (buttonPinwheel.isPressed()) {
                doPinwheel();
            }

            step++;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        ipString = prefs.getString(TargaryenApplication.PREF_SERVER_ADDRESS,
                TargaryenApplication.DEFAULT_IP);
        port = prefs.getInt(TargaryenApplication.PREF_SERVER_PORT,
                TargaryenApplication.DEFAULT_PORT);

        ringView = (EmittersView) findViewById(R.id.ring_view);
        ringView.postInvalidate();

        ringView.onEmitterTouch = ringView.new OnEmitterTouch() {
            @Override
            public void onEmitterTouch(EmittersView.EmitterView[] emitters, int id) {
                triggerEffect(id);
            }
        };

        buttonEruption = (Button) findViewById(R.id.button_eruption);
        buttonEruption2 = (Button) findViewById(R.id.button_eruption2);
        buttonPinwheel = (Button) findViewById(R.id.button_pinwheel);
        buttonRandom = (Button) findViewById(R.id.button_random);
    }

    @Override
    public void onResume() {
        super.onResume();

        startTimeout();
        registerReceiver(receiver, new IntentFilter(intent));
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        createWifiAccessPoint();

        step = 0;
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        wifiManager.setWifiEnabled(false);

        AlarmManager manager = (AlarmManager) (this
                .getSystemService(Context.ALARM_SERVICE));
        manager.cancel(pintent);
    }

    public void startTimeout() {
        pintent = PendingIntent.getBroadcast(this, 0, new Intent(intent), 0);
        AlarmManager manager = (AlarmManager) (this
                .getSystemService(Context.ALARM_SERVICE));

        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + EFFECT_REFRESH_TIMEOUT, EFFECT_REFRESH_TIMEOUT,
                pintent);
    }

    private class ConnectTask extends AsyncTask<byte[], Void, Void> {
        protected Void doInBackground(byte[]... arg0) {
            send(arg0[0]);
            return null;
        }
    }

    private void triggerEffects(int[] ids) {
        EmittersView.EmitterView[] emitters = ringView.getEmitters();
        byte[] message = new byte[ids.length];
        for (int i = 0; i < ids.length; i++) {
            emitters[ids[i]].lastActivated = System.currentTimeMillis();
            emitters[ids[i]].intensity = 1;
            message[i] = (byte) (ids[i] + hack);
        }
        (new ConnectTask()).execute(message);
        ringView.postInvalidate();
    }

    // This is dumb and probably means I can't java
    private void triggerEffects(Integer[] ids) {
        EmittersView.EmitterView[] emitters = ringView.getEmitters();
        byte[] message = new byte[ids.length];
        for (int i = 0; i < ids.length; i++) {
            emitters[ids[i]].lastActivated = System.currentTimeMillis();
            emitters[ids[i]].intensity = 1;
            message[i] = (byte) (ids[i] + hack);
        }
        (new ConnectTask()).execute(message);
        ringView.postInvalidate();
    }

    private void triggerEffect(int id) {
        EmittersView.EmitterView[] emitters = ringView.getEmitters();
        emitters[id].lastActivated = System.currentTimeMillis();
        emitters[id].intensity = 1;
        (new ConnectTask()).execute(new byte[]{(byte) (id + hack)});
        ringView.postInvalidate();
    }

    public void send(byte[] msg) {
        try {
            InetAddress ip = InetAddress.getByName(ipString);
            DatagramSocket s = new DatagramSocket();
            DatagramPacket p = new DatagramPacket(msg, msg.length, ip, port);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DialogFragment newFragment;
        switch (item.getItemId()) {
            case R.id.menu_settings:
                newFragment = new SettingsFragment();
                newFragment.show(getFragmentManager(), "dialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private WifiConfiguration getWifiConfig() {
        Context context = getBaseContext();

        WifiConfiguration netConfig = new WifiConfiguration();
        netConfig.SSID = context.getString(R.string.ssid);
        //netConfig.preSharedKey = context.getString(R.string.wireless_password);
        //netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        //netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        return netConfig;
    }

    private void createWifiAccessPoint() {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }

        // Get all declared methods in WifiManager class
        Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();

        boolean methodFound = false;
        for (Method method : wmMethods) {
            if (method.getName().equals("setWifiApEnabled")) {
                methodFound = true;

                WifiConfiguration netConfig = getWifiConfig();

                try {
                    boolean apstatus = (Boolean) method.invoke(wifiManager, netConfig, true);

                    Log.e(LOG_TAG,
                            String.format("Creating a Wi-Fi Network %s", netConfig.SSID));
                    for (Method isWifiApEnabledmethod : wmMethods) {
                        if (isWifiApEnabledmethod.getName().equals("isWifiApEnabled")) {
                            while (!(Boolean) isWifiApEnabledmethod.invoke(wifiManager)) {
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
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
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

    private void doRandom() {
        if (step % 2 == 0) return;

        List<Integer> effectsToFire = new ArrayList<Integer>(8);
        for (int i = 0; i < NUM_EFFECTS_IN_RING; i++) {
            if (Math.random() > .5) {
                effectsToFire.add(i);
            }
        }

        Integer[] fireEffects = new Integer[0];
        fireEffects = effectsToFire.toArray(fireEffects);
        triggerEffects(fireEffects);
    }

    private void doPinwheel() {
        int slowDown = 5;
        int fireEffect = (int) ((step / slowDown) % NUM_EFFECTS_IN_RING);
        triggerEffect(fireEffect);
    }
}