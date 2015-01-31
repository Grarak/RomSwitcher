/*
 * Copyright (C) 2015 Willi Ye
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

package com.grarak.rom.switcher.utils.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.widget.LinearLayout;

import com.grarak.rom.switcher.R;
import com.grarak.rom.switcher.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by willi on 17.01.15.
 */
public class DownloadTask extends AsyncTask<String, Integer, String> implements Constants {

    public enum DownloadStatus {
        SUCCESS, CANCELED, FAILED
    }

    private Context context;
    private PowerManager.WakeLock mWakeLock;
    private ProgressDialog mProgressDialog;
    private final DownloadListener listener;

    private final String path;
    private final String file;

    public DownloadTask(String path, String file, DownloadListener listener, Context context) {
        this.path = path;
        this.file = file;
        this.listener = listener;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context.getClass().getName());
        mWakeLock.acquire();

        if (!new File(path).exists()) new File(path).mkdirs();

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage(context.getString(R.string.downloading));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel();
            }
        });
        mProgressDialog.show();
    }

    private void cancel() {
        cancel(true);
        new File(path + "/" + file).delete();
        listener.downloadFinish(DownloadStatus.CANCELED);
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        mProgressDialog.dismiss();

        if (result != null) new File(path + "/" + file).delete();
        listener.downloadFinish(result == null ? DownloadStatus.SUCCESS : DownloadStatus.FAILED);
    }

    @Override
    protected String doInBackground(String... urls) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            Log.i(TAG, "downloading " + urls[0] + " to " + path + "/" + file);
            URL url = new URL(urls[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();

            int fileLength = connection.getContentLength();

            input = connection.getInputStream();
            output = new FileOutputStream(path + "/" + file);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                if (fileLength > 0) publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (connection != null) connection.disconnect();
        }
        return null;
    }

    public interface DownloadListener {
        public void downloadFinish(DownloadStatus status);
    }

}
