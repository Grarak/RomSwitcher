package com.grarak.romswitcher.services;

/*
 * Copyright (C) 2014 The RomSwitcher Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Created by grarak's kitten (meow) on 30.04.14.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import com.grarak.romswitcher.R;
import com.grarak.romswitcher.utils.Connection;
import com.grarak.romswitcher.utils.Constants;
import com.grarak.romswitcher.utils.Utils;

public class AppUpdaterService extends Service implements Constants {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Utils().getConnection(applink);
        new Connect().execute();

        return Service.START_NOT_STICKY;
    }

    private class Connect extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return Connection.htmlstring;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                String output = Connection.htmlstring;
                if (!output.isEmpty()) {
                    // Pull the version code of html return
                    String[] outputarray = output.split("RomSwitcher <span class=\"version\">v");
                    // Check if the website is correct
                    if (outputarray.length >= 2) {
                        String lastversion = outputarray[1].split("</span>")[0];
                        String currentversion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                        if (!lastversion.equals(currentversion)) showNotification(lastversion);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "unable to read app version");
            } finally {
                stopSelf();
            }
        }
    }

    private void showNotification(String version) {
        try {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);
            Thread.sleep(50);
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);
        } catch (InterruptedException e) {
        }

        NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int count = 0;

        PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(Intent.ACTION_VIEW, Uri.parse(applink)), 0);

        Notification notifyObj = new Notification(R.drawable.ic_launcher, getString(R.string.new_app_version, version), System.currentTimeMillis());
        notifyObj.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name), getString(R.string.new_app_version, version), intent);
        notifyObj.number = ++count;
        notifyObj.flags |= Notification.FLAG_AUTO_CANCEL;
        notifyMgr.notify(2, notifyObj);
    }
}
