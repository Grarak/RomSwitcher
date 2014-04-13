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

import android.app.Activity;
import android.content.Context;

import java.io.IOException;

public interface Helpers {

    // All my helpers, I like it (meow)

    public long getFolderSize(String folder);

    public boolean isSupported();

    public void createProgressDialog(Context context);

    public void showProgressDialog(String message, boolean show);

    public boolean isDefaultRom();

    public void reset(Activity activity);

    public boolean isRSInstalled();

    public boolean unZip(String path, String name);

    public boolean manualBoot();

    public boolean installRecovery();

    public boolean rebootRecovery();

    public boolean oneKernel();

    public String getPartition(String partition);

    public int getRomNumber();

    public String getCurrentVersion();

    public String getLastVersion();

    public String getDownloadLink();

    public String readFile(String filepath) throws IOException;

    public void deleteFile(String file);

    public void writeFile(String file, String value);

    public void toast(String message, Context context);

    public boolean existfile(String file);

    public void getConnection(String url);

    public String getString(String name, String defaults, Context context);

    public void saveString(String name, String value, Context context);

    public boolean getBoolean(String name, boolean defaults, Context context);

    public void saveBoolean(String name, boolean value, Context context);

}
