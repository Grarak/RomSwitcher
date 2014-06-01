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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils implements Helpers, Constants {

    private String prefname = "settings";
    RootUtils root = new RootUtils();
    public static ProgressDialog ProgressDialog;

    @Override
    public void copyAssets(String path, String file, Context context) {
        AssetManager assetManager = context.getAssets();
        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open(file);
            File outFile = new File(path, file);
            out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (IOException e) {
            Log.e(TAG, "failed to copy asset file: " + file + " to " + path);
        }
    }

    @Override
    public long getFolderSize(String folder) {

        /*
         * Stupid if statement!
         * Why it just don't return 0 when the folder is empty?!
         */

        long size = 0;
        if (new File(folder).listFiles() != null)
            for (File file : new File(folder).listFiles())
                size += file.isDirectory() ? getFolderSize(file.toString()) : file.length();

        return size;
    }

    @Override
    public boolean isSupported() {
        if (existfile(configurationFile))
            try {
                String configuration = readFile(configurationFile);
                if (configuration.contains("<devices>")) {
                    String devices = configuration.split("<devices>")[1].split("</devices>")[0];
                    return devices.contains(deviceName) || devices.contains(deviceBoard);
                }
            } catch (IOException e) {
                Log.e(TAG, "unable to read configuration file");
            }
        return false;
    }

    @Override
    public void createProgressDialog(Context context) {
        ProgressDialog = new ProgressDialog(context);
        ProgressDialog.setIndeterminate(true);
        ProgressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
    }

    @Override
    public void showProgressDialog(String message, boolean show) {
        if (ProgressDialog != null) {
            ProgressDialog.setMessage(message);
            if (show)
                ProgressDialog.show();
            else
                ProgressDialog.hide();
        }
    }

    @Override
    public boolean isDefaultRom() {
        return !existfile("/.firstrom");
    }

    @Override
    public void reset(Activity activity) {
        if (activity != null) {
            final int enter_anim = android.R.anim.fade_in;
            final int exit_anim = android.R.anim.fade_out;
            activity.overridePendingTransition(enter_anim, exit_anim);
            activity.finish();
            activity.overridePendingTransition(enter_anim, exit_anim);
            activity.startActivity(activity.getIntent());
        }
    }

    @Override
    public boolean isRSInstalled() {
        return existfile(onekernelInstalledFile) || (existfile(firstimage) && existfile(secondimage));
    }

    @Override
    public boolean unZip(String path, String name) {
        try {
            String filename;
            InputStream is = new FileInputStream(path + name);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[4096];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();
                if (ze.isDirectory()) {
                    new File(path + filename).mkdirs();
                    continue;
                }
                FileOutputStream fout = new FileOutputStream(path + filename);
                while ((count = zis.read(buffer)) != -1)
                    fout.write(buffer, 0, count);
                fout.close();
                zis.closeEntry();
            }
            zis.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean useDtb() {
        return getDeviceConfig("dtb").equals("1");
    }

    @Override
    public String getKernelBase() {
        return getDeviceConfig("kernelbase").replace("\\n", "\n");
    }

    @Override
    public String getDevNote() {
        return getDeviceConfig("devnote").replace("\\n", "\n"); // Ugly hack to show next line
    }

    @Override
    public boolean manualBoot() {
        return getDeviceConfig("manualboot").equals("1");
    }

    @Override
    public boolean installRecovery() {
        return getDeviceConfig("installrecovery").equals("1");
    }

    @Override
    public boolean rebootRecovery() {
        return getDeviceConfig("rebootrecovery").equals("1");
    }

    @Override
    public String getMemmin() {
        return getDeviceConfig("memmin");
    }

    @Override
    public boolean kexecHardboot() {
        return getDeviceConfig("kexechardboot").equals("1");
    }

    @Override
    public boolean oneKernel() {
        return getDeviceConfig("onekernel").equals("1");
    }

    @Override
    public String getPartition(String partition) {

        /*
         * Took me 1 hour! Stupid Sammy fstab (grrrr)
         * And still only supports 4.3 and 4.4 (die die die! meow)
         */

        String output = "0";

        File[] rootfiles = new File("/").listFiles();
        String fstab = "";
        if (rootfiles != null) {
            for (File file : rootfiles)
                if (file.getName().contains("fstab") && !file.getName().equals("fstab.goldfish"))
                    fstab = file.getName();
            try {
                if (fstab != null) {
                    root.run("mount -o rw,remount /");
                    root.run("chmod 777 /" + fstab);

                    String[] fstabvalues = readFile("/" + fstab).split("\\r?\\n");

                    String par = "";

                    for (String partitionline : fstabvalues)
                        if (partitionline.contains(partition))
                            par = partitionline.split(" ")[0];

                    if (!par.isEmpty()) output = par;
                    else {
                        if (existfile("/file_contexts")) {
                            String[] filecontextvalues = readFile("file_contexts").split("\\r?\\n");

                            for (String partitionline : filecontextvalues)
                                if (partitionline.contains(partition + "blk"))
                                    output = partitionline.split(" ")[0];
                        }
                    }
                    root.run("mount -o ro,remount /");
                }
            } catch (IOException e) {
                Log.e(TAG, "unable to read fstab file: " + fstab);
                root.run("mount -o ro,remount /");
                return getPartition(partition);
            }
        }

        return output.equals("0") ? getPartition(partition) : output;
    }

    @Override
    public int getRomNumber() {
        return Integer.parseInt(getDeviceConfig("roms"));
    }

    @Override
    public String getCurrentVersion() {
        if (existfile(oneKernel() ? versionFileOneKernel : versionFile))
            try {
                if (oneKernel()) {
                    root.run("mount -o rw,remount /");
                    root.run("chmod 777 " + versionFileOneKernel);
                }
                return readFile(oneKernel() ? versionFileOneKernel : versionFile).split("\\r?\\n")[0];
            } catch (IOException e) {
                Log.e(TAG, "unable to read current version file");
            }
        return "";
    }

    @Override
    public String getLastVersion() {
        return getDeviceConfig("version");
    }

    @Override
    public String getDownloadLink() {
        if (existfile(configurationFile))
            return getDeviceConfig("download");
        return "";
    }

    private String getDeviceConfig(String value) {
        try {
            String configuration = readFile(configurationFile);
            if (configuration.contains("<devices>") && isSupported()) {
                String deviceConfig = configuration.split("<devices>")[1].split("</devices>")[0].split("<" + model(configuration))[1].split("/>")[0];
                if (deviceConfig.contains(value)) {
                    String deviceValue = deviceConfig.split(value + "=\"")[1].split("\"")[0];
                    if (!deviceConfig.isEmpty()) return deviceValue;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "unable to read configuration file");
        }
        return "0";
    }

    private String model(String configuration) {
        String supported = "";
        if (configuration.contains(deviceName))
            supported = deviceName;
        else if (configuration.contains(deviceBoard))
            supported = deviceBoard;
        return supported;
    }

    @Override
    public String readFile(String filepath) throws IOException {
        BufferedReader buffreader = new BufferedReader(new FileReader(filepath), 256);
        String line;
        StringBuilder text = new StringBuilder();
        while ((line = buffreader.readLine()) != null) {
            text.append(line);
            text.append("\n");
        }
        buffreader.close();
        return text.toString();
    }

    @Override
    public void deleteFile(String file) {
        new File(file).delete();
    }

    @Override
    public void writeFile(String file, String value) {
        if (!value.isEmpty()) {
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(file, true);
                FileWriter fWriter;
                try {
                    fWriter = new FileWriter(fos.getFD());
                    fWriter.write(value);
                    fWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    fos.getFD().sync();
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void toast(String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean existfile(String file) {
        return new File(file).exists();
    }

    @Override
    public void getConnection(String url) {
        Connection.htmlstring = "";
        new Connection().execute(url);
    }

    @Override
    public String getString(String name, String defaults, Context context) {
        return context.getSharedPreferences(prefname, 0).getString(name, defaults);
    }

    @Override
    public void saveString(String name, String value, Context context) {
        context.getSharedPreferences(prefname, 0).edit().putString(name, value).commit();
    }

    @Override
    public boolean getBoolean(String name, boolean defaults, Context context) {
        return context.getSharedPreferences(prefname, 0).getBoolean(name, defaults);
    }

    @Override
    public void saveBoolean(String name, boolean value, Context context) {
        context.getSharedPreferences(prefname, 0).edit().putBoolean(name, value).commit();
    }
}
