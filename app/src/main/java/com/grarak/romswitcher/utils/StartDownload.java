package com.grarak.romswitcher.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.grarak.romswitcher.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.grarak.romswitcher.utils.Utils.ProgressDialog;

/*
 * Copyright (C) 2013 The RomSwitcher Project
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
 * Created by grarak's kitten (meow) on 01.04.14.
 */

public class StartDownload extends AsyncTask<String, Integer, String> implements Constants {

    private Context context;
    private PowerManager.WakeLock mWakeLock;
    private String path;
    private String name;
    private Utils utils;

    public StartDownload(Context context, String path, String name) {
        this.context = context;
        this.path = path;
        this.name = name;
        utils = new Utils();
    }

    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();

            int fileLength = connection.getContentLength();

            input = connection.getInputStream();
            output = new FileOutputStream(path + "/tmp");

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                if (fileLength > 0)
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        utils.showProgressDialog(context.getString(R.string.downloading), true);

        if (!utils.existfile(path))
            new File(path).mkdir();

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context.getClass().getName());
        mWakeLock.acquire();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        if (ProgressDialog != null) {
            utils.showProgressDialog(context.getString(R.string.downloading), true);
            ProgressDialog.setIndeterminate(false);
            ProgressDialog.setMax(100);
            ProgressDialog.setProgress(progress[0]);
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (utils.existfile(path + "/tmp")) {
            utils.deleteFile(path + "/" + name);
            new File(path + "/tmp").renameTo(new File(path + "/" + name));
        }

        mWakeLock.release();
        if (ProgressDialog != null) utils.showProgressDialog("", false);
        utils.toast(result != null ? context.getString(R.string.error) + ": " + result : context.getString(R.string.done), context);
        if (result != null) {
            utils.deleteFile(path + "/" + name);
            Log.e(TAG, result);
        }
    }
}
