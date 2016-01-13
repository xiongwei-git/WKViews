/*
 *
 * Copyright 2015 TedXiong xiong-wei@hotmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wkzf.library.component.player.dlna.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;

import com.wkzf.library.component.player.dlna.engine.SearchThread;

import org.cybergarage.upnp.ControlPoint;

/**
 * The service to search the DLNA Device in background all the time.
 *
 * @author Ted
 */
public class DLNAService extends Service {
    private static final String TAG = "DLNAService";
    private ControlPoint mControlPoint;
    private SearchThread mSearchThread;
    private WifiStateReceiver mWifiStateReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unInit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startThread();
        return super.onStartCommand(intent, flags, startId);
    }

    private void init() {
        mControlPoint = new ControlPoint();
        mSearchThread = new SearchThread(mControlPoint);
        registerWifiStateReceiver();
    }

    private void unInit() {
        stopThread();
        unregisterWifiStateReceiver();
    }

    /**
     * Make the thread start to search devices.
     */
    private void startThread() {
        if (mSearchThread != null) {
            mSearchThread.setSearchTimes(0);
        } else {
            mSearchThread = new SearchThread(mControlPoint);
        }

        if (mSearchThread.isAlive()) {
            mSearchThread.awake();
        } else {
            mSearchThread.start();
        }
    }

    private void stopThread() {
        if (mSearchThread != null) {
            mSearchThread.stopThread();
            mControlPoint.stop();
            mSearchThread = null;
            mControlPoint = null;
        }
    }

    private void registerWifiStateReceiver() {
        if (mWifiStateReceiver == null) {
            mWifiStateReceiver = new WifiStateReceiver();
            registerReceiver(mWifiStateReceiver, new IntentFilter(
                    ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    private void unregisterWifiStateReceiver() {
        if (mWifiStateReceiver != null) {
            unregisterReceiver(mWifiStateReceiver);
            mWifiStateReceiver = null;
        }
    }

    private class WifiStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
            Bundle bundle = intent.getExtras();
            int statusInt = bundle.getInt("wifi_state");
            switch (statusInt) {
                case WifiManager.WIFI_STATE_UNKNOWN:
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    startThread();
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    break;
                default:
                    break;
            }
        }
    }

}