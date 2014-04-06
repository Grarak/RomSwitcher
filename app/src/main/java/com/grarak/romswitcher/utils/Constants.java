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
 * Created by grarak's kitten (meow) on 31.03.14.
 */

import android.os.Build;
import android.os.Environment;

public interface Constants {

    public String externalStorage = Environment.getExternalStorageDirectory().getPath();
    public String romswitcherPath = externalStorage + "/romswitcher";
    public String downloadPath = romswitcherPath + "/downloads";
    public String toolfile = downloadPath + "/tools.zip";
    public String backupPath = romswitcherPath + "/backup";

    public String firstimage = downloadPath + "/first.img";
    public String secondimage = downloadPath + "/second.img";
    public String onekernelInstalledFile = "/sbin/create_system.sh";
    public String onekernelImage = downloadPath + "/boot.img";
    public String rebootRecoveryFile = "/rebootrs";
    public String romFile = "/.rom";
    public String nextbootFile = "/.nextboot";

    public String manualbootFile = romswitcherPath + "/manualboot";
    public String appsharingFile = romswitcherPath + "/appsharing";

    public String versionFile = downloadPath + "/version";
    public String versionFileOneKernel = "/sbin/rsversion";

    public String configurationFile = romswitcherPath + "/configuration.xml";
    public String configurationFileLink = "https://raw.githubusercontent.com/Grarak/grarak.github.io/master/romswitcher/configuration/devices";

    public String TAG = "RomSwitcher";

    public String deviceName = Build.DEVICE;
    public String deviceBoard = Build.BOARD;

}
