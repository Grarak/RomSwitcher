package com.grarak.romswitcher.utils;

import android.os.Build;

/**
 * Created by grarak's kitten (meow) on 31.03.14.
 */

public interface Constants {

    public String externalStorage = "/sdcard";
    public String romswitcherPath = externalStorage + "/romswitcher";
    public String downloadPath = romswitcherPath + "/downloads";
    public String toolfile = downloadPath + "/tools.zip";

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
