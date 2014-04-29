package com.grarak.romswitcher.utils;

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
 * Created by grarak's kitten (meow) on 06.04.14.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;

import com.grarak.romswitcher.R;

import java.io.File;

import static com.grarak.romswitcher.utils.Utils.ProgressDialog;

public class Backup extends AsyncTask<String, Integer, String> implements Constants {

    private Utils utils = new Utils();
    private RootUtils root = new RootUtils();

    private PowerManager.WakeLock mWakeLock;

    private String name;
    private int currentFragment;
    private Context context;

    public Backup(Context context, String name, int currentFragment) {
        this.name = name;
        this.currentFragment = currentFragment;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {

        File backup = new File("/data/media/backup.tar");

        if (backup.exists()) root.run("rm -rf " + backup.toString());

        root.run("tar cf /data/media/backup.tar /data/media/." + currentFragment + "rom/*");
        root.run("mkdir -p " + backupPath + "/" + currentFragment + "rom");
        root.run("mv -f /data/media/backup.tar " + backupPath + "/" + currentFragment + "rom/" + name + ".tar");

        long romsize = utils.getFolderSize("/data/media/." + currentFragment + "rom");

        while (!utils.existfile(backupPath + "/" + currentFragment + "rom/" + name + ".tar"))
            publishProgress((int) (backup.length() / (romsize / 100)));

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        utils.showProgressDialog(context.getString(R.string.backing_up), true);

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        utils.showProgressDialog(context.getString(R.string.backing_up), true);
        ProgressDialog.setIndeterminate(false);
        ProgressDialog.setMax(100);
        ProgressDialog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mWakeLock.release();

        utils.showProgressDialog("", false);

        utils.toast(context.getString(R.string.done), context);
    }

}
