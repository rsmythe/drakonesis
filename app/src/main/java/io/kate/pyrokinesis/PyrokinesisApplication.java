package io.kate.pyrokinesis;

import android.app.Application;

public class PyrokinesisApplication extends Application {
    public static final String PREF_SERVER_ADDRESS = "server_address";
    public static final String PREF_SERVER_PORT = "server_port";

    public static final int DEFAULT_PORT = 2000;
    public static final String DEFAULT_IP = "192.168.43.212";
}