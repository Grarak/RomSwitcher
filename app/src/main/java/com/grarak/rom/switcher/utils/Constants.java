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
