package io.heavymeta.targaryen;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import io.heavymeta.targaryen.DragonCommand.DragonHead;
import io.heavymeta.targaryen.DragonCommand.DragonMouthState;
import io.heavymeta.targaryen.DragonCommand.DragonWingState;
import io.heavymeta.targaryen.DragonConnect.DragonConnect;

public class MainActivity extends Activity {
    public static final String LOG_TAG = MainActivity.class.getName();

    SharedPreferences prefs;

    private DragonConnect _wifi;
    private DragonHead _dragonHead;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        String ipString = prefs.getString(TargaryenApplication.PREF_SERVER_ADDRESS,
                TargaryenApplication.DEFAULT_IP);
        int port = prefs.getInt(TargaryenApplication.PREF_SERVER_PORT,
                TargaryenApplication.DEFAULT_PORT);
        this._wifi = new DragonConnect(ipString, port, getApplicationContext());
        this._dragonHead = new DragonHead(this._wifi);
    }

    public void TriggerOpenMouth(View view)
    {
        this._dragonHead.OpenMouth();
    }

    public void TriggerCloseMouth(View view)
    {
        this._dragonHead.CloseMouth();
    }

    public void TriggerBreatheFireSequence(View view)
    {
        this._dragonHead.BreathFire();
    }

    public void TriggerExtendWings(View view)
    {

    }

    public void TriggerRetractWings(View view)
    {

    }

    @Override
    public void onResume() {
        super.onResume();
        this._wifi.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        this._wifi.onPause();
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

}