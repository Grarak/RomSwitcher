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

public class RebootRom extends AsyncTask<String, Integer, String> implements Constants {

    private Utils utils = new Utils();
    private RootUtils root = new RootUtils();

    private Context context;

    private int currentFragment;

    private PowerManager.WakeLock mWakeLock;

    public RebootRom(Context context, int currentFragment) {
        this.context = context;
        this.currentFragment = currentFragment;
    }

    @Override
    protected String doInBackground(String... params) {
        root.run("echo " + String.valueOf(currentFragment) + " > " + romFile);
        root.run("echo 1 > " + nextbootFile);

        if (utils.kexecHardboot()) {
            String zImage = kexecPath + "/" + currentFragment + "rom/zImage";
            String kexeccommand = kexecPath + "/kexec --load-hardboot " + zImage + " --initrd=" + kexecRamdik + " --mem-min=" + utils.getMemmin() + " --command-line=\"$(cat /proc/cmdline)\"";
            if (utils.useDtb()) kexeccommand = kexeccommand + " --dtb";
            root.run(kexeccommand);
        }

        if (!utils.kexecHardboot() && !utils.oneKernel() && (utils.isDefaultRom() || currentFragment == 1))
            root.writePartition(currentFragment == 1 ? firstimage : secondimage, utils.getPartition("boot"));

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mWakeLock.release();
        utils.showProgressDialog("", false);

        if (utils.kexecHardboot()) root.run(kexecPath + "/kexec -e");
        else root.reboot();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        utils.showProgressDialog(context.getString(R.string.loading), true);

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context.getClass().getName());
        mWakeLock.acquire();
    }

}
