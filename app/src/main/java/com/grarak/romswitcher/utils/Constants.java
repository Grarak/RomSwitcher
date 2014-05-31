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

public interface Constants {

    public final String externalStorage = "/sdcard";
    public final String romswitcherPath = externalStorage + "/romswitcher";
    public final String downloadPath = romswitcherPath + "/downloads";
    public final String toolfile = downloadPath + "/tools.zip";
    public final String backupPath = "/data/media/.romswitcher/backup";

    public final String firstimage = downloadPath + "/first.img";
    public final String secondimage = downloadPath + "/second.img";
    public final String onekernelInstalledFile = "/sbin/mount_recovery.sh";
    public final String onekernelImage = downloadPath + "/boot.img";
    public final String rebootRecoveryFile = "/data/media/rebootrs";
    public final String romFile = "/data/media/.rom";
    public final String nextbootFile = "/data/media/.nextboot";

    public final String manualbootFile = romswitcherPath + "/manualboot";
    public final String appsharingFile = romswitcherPath + "/appsharing";

    public final String versionFile = downloadPath + "/version";
    public final String versionFileOneKernel = "/sbin/version";

    public final String[] assets = new String[]{"kexec", "unpackbootimg"};
    public final String kexecPath = romswitcherPath + "/kexec";
    public final String kexecRamdik = downloadPath + "boot.img-ramdisk.gz";

    public final String configurationFile = romswitcherPath + "/configuration.xml";
    public final String configurationFileLink = "https://raw.githubusercontent.com/Grarak/grarak.github.io/master/romswitcher/configuration/devices";
    public final String applink = "http://slideme.org/application/romswitcher";

    public final String TAG = "RomSwitcher";

    public final String deviceName = Build.DEVICE;
    public final String deviceBoard = Build.BOARD;

}
