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

package com.wkzf.library.component.player.dlna.engine;


import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.device.DeviceChangeListener;

/**
 * A thread to search the devices all the time.
 *
 * @author CharonChui
 */
public class SearchThread extends Thread {
    private boolean flag = true;
    private ControlPoint mControlPoint;
    private boolean mStartComplete;
    private int mSearchTimes;
    private static final int mFastInternalTime = 15000;
    private static final int mNormalInternalTime = 3600000;
    private static final String TAG = "SearchThread";

    public SearchThread(ControlPoint mControlPoint) {
        super();
        this.mControlPoint = mControlPoint;
        this.mControlPoint.addDeviceChangeListener(mDeviceChangeListener);
    }

    @Override
    public void run() {
        while (flag) {
            if (mControlPoint == null) {
                break;
            }
            searchDevices();
        }
    }

    /**
     * Search for the DLNA devices.
     */
    private void searchDevices() {
        try {
            if (mStartComplete) {
                mControlPoint.search();
            } else {
                mControlPoint.stop();
                boolean startRet = mControlPoint.start();
                if (startRet) {
                    mStartComplete = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Search the devices five times fast, and after that we can make it
        // search lowly to save the power.
        synchronized (this) {
            try {
                mSearchTimes++;
                if (mSearchTimes >= 5) {
                    wait(mNormalInternalTime);
                } else {
                    wait(mFastInternalTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Set the search times, set this to 0 to make it search fast.
     *
     * @param searchTimes The times we have searched.
     */
    public synchronized void setSearchTimes(int searchTimes) {
        this.mSearchTimes = searchTimes;
    }

    /**
     * Notify all the thread.
     */
    public void awake() {
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Stop the thread, if quit this application we should use this method to
     * stop the thread.
     */
    public void stopThread() {
        flag = false;
        awake();
    }

    private DeviceChangeListener mDeviceChangeListener = new DeviceChangeListener() {

        @Override
        public void deviceRemoved(Device dev) {
            DLNAContainer.getInstance().removeDevice(dev);
        }

        @Override
        public void deviceAdded(Device dev) {
            DLNAContainer.getInstance().addDevice(dev);
        }
    };
}