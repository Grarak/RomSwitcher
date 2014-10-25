package com.grarak.rom.switcher.utils;

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
 * Created by grarak's kitten (meow) on 23.04.14.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;

import com.grarak.rom.switcher.R;

public class Delete extends AsyncTask<String, Integer, Void> {

    private Utils utils = new Utils();
    private RootUtils root = new RootUtils();
    private PowerManager.WakeLock mWakeLock;

    private String path;
    private Context context;

    public Delete(String path, Context context) {
        this.path = path;
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... strings) {

        root.run("rm -rf " + path);

        while (!utils.existfile(path)) return null;

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        utils.showProgressDialog(context.getString(R.string.deleting), true);

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire();
    }

    @Override
    protected void onPostExecute(Void s) {
        super.onPostExecute(s);
        mWakeLock.release();

        utils.showProgressDialog("", false);

        utils.toast(context.getString(R.string.done), context);
    }

}
