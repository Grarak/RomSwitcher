package com.grarak.rom.switcher.utils;

import android.os.Build;
import android.os.Environment;

/**
 * Created by willi on 10.01.15.
 */
public interface Constants {

    public final String TAG = "RomSwitcher";
    public final String DEVICE_MODEL = Build.DEVICE;

    public final String[] ROMSWITCHER_FILES = new String[]{
            "/sbin/mod_zip.sh", "/sbin/mount_back.sh"
    };

    public final String ROMSWITCHER_VERSION_FILE = "/res/rs_version";
    public final String ROMSWITCHER_ROM_FILE = "/data/media/.rom";
    public final String ROMSWITCHER_RECOVERY_FILE = "/data/media/.reboot_recovery";
    public final String ROMSWITCHER_DOWNLOAD_PATH = Environment.getExternalStorageDirectory() + "/romswitcher";

}
