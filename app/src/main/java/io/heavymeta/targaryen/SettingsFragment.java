package io.heavymeta.targaryen;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class SettingsFragment extends DialogFragment {

    SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.debug, null);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        getDialog().setTitle(getString(R.string.title_settings));

        final TextView viewAddress = (TextView) view.findViewById(R.id.server_address);
        final TextView viewPort = (TextView) view.findViewById(R.id.server_port);

        viewAddress.setText(prefs.getString(TargaryenApplication.PREF_SERVER_ADDRESS, TargaryenApplication.DEFAULT_IP));
        viewPort.setText("" + prefs.getInt(TargaryenApplication.PREF_SERVER_PORT, TargaryenApplication.DEFAULT_PORT));

        Button button = (Button) view.findViewById(R.id.connect_button);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Editor editor = prefs.edit();
                int port = 0;
                try {
                    port = Integer.parseInt(viewPort.getText().toString());
                } catch (Exception e) {
                }

                editor.putString(TargaryenApplication.PREF_SERVER_ADDRESS, viewAddress.getText().toString());
                editor.putInt(TargaryenApplication.PREF_SERVER_PORT, port);

                editor.commit();
                getDialog().dismiss();
            }
        });

        return view;
    }
}