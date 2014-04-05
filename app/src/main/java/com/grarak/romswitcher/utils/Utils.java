package com.grarak.romswitcher.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import com.grarak.romswitcher.R;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by grarak's kitten (meow) on 31.03.14.
 */

public class Utils implements Helpers, Constants {

    private String prefname = "settings";
    public String dataPath = "/data";

    RootUtils root = new RootUtils();

    public static ProgressDialog ProgressDialog;

    public Utils() {
        dataPath = existfile("/.firstrom") ? "/.firstrom" : "/data";
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
        ProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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
    public void checkReboot(Context context) {
        AlertDialog.Builder warning = new AlertDialog.Builder(context);
        warning.setMessage(context.getString(R.string.reboot_now))
                .setPositiveButton(context.getString(R.string.yes
                ), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        root.reboot();
                    }
                }).setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).show();
    }

    @Override
    public void reset(Activity activity) {
        if (activity == null)
            return;
        final int enter_anim = android.R.anim.fade_in;
        final int exit_anim = android.R.anim.fade_out;
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.finish();
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.startActivity(activity.getIntent());
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
    public int getSystemImageSize() {
        if (existfile(configurationFile))
            try {
                String configuration = readFile(configurationFile);
                if (configuration.contains("installrecovery"))
                    return Integer.parseInt(getDeviceConfig(configuration, "systemsize"));
            } catch (IOException e) {
                Log.e(TAG, "unable to read configuration file");
            }
        return 0;
    }

    @Override
    public boolean manualBoot() {
        if (existfile(configurationFile))
            try {
                return getDeviceConfig(readFile(configurationFile), "manualboot").equals("1");
            } catch (IOException e) {
                Log.e(TAG, "unable to read configuration file");
            }
        return false;
    }

    @Override
    public boolean installRecovery() {
        if (existfile(configurationFile))
            try {
                return getDeviceConfig(readFile(configurationFile), "installrecovery").equals("1");
            } catch (IOException e) {
                Log.e(TAG, "unable to read configuration file");
            }
        return false;
    }

    @Override
    public boolean rebootRecovery() {
        if (existfile(configurationFile))
            try {
                return getDeviceConfig(readFile(configurationFile), "rebootrecovery").equals("1");
            } catch (IOException e) {
                Log.e(TAG, "unable to read configuration file");
            }
        return false;
    }

    @Override
    public boolean oneKernel() {
        if (existfile(configurationFile))
            try {
                return getDeviceConfig(readFile(configurationFile), "onekernel").equals("1");
            } catch (IOException e) {
                Log.e(TAG, "unable to read configuration file");
            }
        return false;
    }

    @Override
    public String getPartition(String partition) {

        /*
         * Took me 1 hour! Stupid Sammy fstab (grrrr)
         * And still only supports 4.3 and 4.4 (die die die! meow)
         */

        File[] rootfiles = new File("/").listFiles();
        File fstab = null;
        if (rootfiles != null) {
            for (File file : rootfiles)
                if (file.getName().contains("fstab") && !file.getName().equals("fstab.goldfish"))
                    fstab = file;
            try {
                if (fstab != null) {
                    root.run("mount -o rw,remount /");
                    root.run("chmod 777 " + fstab.getAbsolutePath());

                    String[] fstabvalues = readFile(fstab.getAbsolutePath()).split("\\r?\\n");

                    String par = "";

                    for (String partitionline : fstabvalues)
                        if (partitionline.contains(partition))
                            par = partitionline.split(" ")[0];

                    if (!par.isEmpty()) return par;
                    else {
                        if (existfile("/file_contexts")) {
                            String[] filecontextvalues = readFile("file_contexts").split("\\r?\\n");

                            for (String partitionline : filecontextvalues)
                                if (partitionline.contains(partition + "blk"))
                                    return partitionline.split(" ")[0];
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "unable to read fstab file: " + fstab.getAbsolutePath());
                return fstab.getAbsolutePath();
            }
        }

        return "0";
    }

    @Override
    public int getRomNumber() {
        if (existfile(configurationFile))
            try {
                return Integer.parseInt(getDeviceConfig(readFile(configurationFile), "roms"));
            } catch (IOException e) {
                Log.e(TAG, "unable to read configuration file");
            }
        return 0;
    }

    @Override
    public String getCurrentVersion() {
        if (existfile(oneKernel() ? versionFileOneKernel : versionFile))
            try {
                return readFile(oneKernel() ? versionFileOneKernel : versionFile);
            } catch (IOException e) {
                Log.e(TAG, "unable to read current version file");
            }
        return "";
    }

    @Override
    public String getLastVersion() {
        if (existfile(configurationFile))
            try {
                return getDeviceConfig(readFile(configurationFile), "version");
            } catch (IOException e) {
                Log.e(TAG, "unable to read configuration file");
            }
        return "";
    }

    @Override
    public String getDownloadLink() {
        if (existfile(configurationFile))
            try {
                return getDeviceConfig(readFile(configurationFile), "download");
            } catch (IOException e) {
                Log.e(TAG, "unable to read configuration file");
            }
        return "";
    }

    private String getDeviceConfig(String configuration, String value) {
        if (configuration.contains("<devices>") && isSupported()) {
            String deviceConfig = configuration.split("<devices>")[1].split("</devices>")[0].split("<" + model(configuration))[1].split("/>")[0];
            if (deviceConfig.contains(value)) {
                String deviceValue = deviceConfig.split(value + "=\"")[1].split("\"")[0];
                if (!deviceConfig.isEmpty()) return deviceValue;
            }
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
        BufferedReader buffreader = new BufferedReader(
                new FileReader(filepath), 256);
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
        GetConntection.htmlstring = "";
        new GetConntection().execute(url);
    }

    @Override
    public String getString(String name, String defaults, Context context) {
        return context.getSharedPreferences(prefname, 0).getString(name,
                defaults);
    }

    @Override
    public void saveString(String name, String value, Context context) {
        context.getSharedPreferences(prefname, 0).edit().putString(name, value)
                .commit();
    }

    @Override
    public boolean getBoolean(String name, boolean defaults, Context context) {
        return context.getSharedPreferences(prefname, 0).getBoolean(name,
                defaults);
    }

    @Override
    public void saveBoolean(String name, boolean value, Context context) {
        context.getSharedPreferences(prefname, 0).edit().putBoolean(name, value)
                .commit();
    }
}
