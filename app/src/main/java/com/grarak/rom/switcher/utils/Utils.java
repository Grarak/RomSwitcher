package com.grarak.rom.switcher.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import com.grarak.rom.switcher.R;
import com.grarak.rom.switcher.utils.json.DevicesJson;
import com.grarak.rom.switcher.utils.root.RootUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by willi on 10.01.15.
 */
public class Utils implements Constants {

    private static DevicesJson mDevicesJson;

    public static void rebootRecovery() {
        RootUtils.runCommand("touch " + ROMSWITCHER_RECOVERY_FILE);
        reboot();
    }

    public static boolean isDefaultROM() {
        return !RootUtils.fileExist("/.firstdata");
    }

    public static void reboot() {
        RootUtils.runCommand("reboot");
    }

    public static void setROM(String path, String name) {
        String value = null;
        if (path == null) path = "";
        if (name.equals("default") || path.startsWith("/data/media")) {
            value = name;
        } else value = "/external_sd/.romswitcher/" + name;
        RootUtils.runCommand("echo " + value + " > " + ROMSWITCHER_ROM_FILE);
    }

    public static void confirmDialog(String title, String message, final OnConfirmDialogListener onConfirmDialogListener,
                                     Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        if (title != null) dialog.setTitle(title);
        if (message != null) dialog.setMessage(message);
        dialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onConfirmDialogListener.onDismiss();
            }
        }).setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onConfirmDialogListener.onConfirm();
            }
        }).show();
    }

    public interface OnConfirmDialogListener {
        public void onDismiss();

        public void onConfirm();
    }

    public static String getVersion() {
        if (RootUtils.fileExist(ROMSWITCHER_VERSION_FILE))
            return RootUtils.readFile(ROMSWITCHER_VERSION_FILE);
        return null;
    }

    public static boolean isInstalled() {
        for (String file : ROMSWITCHER_FILES)
            if (RootUtils.fileExist(file)) return true;
        return false;
    }

    public static DevicesJson getDevicesJson(Context context) {
        if (mDevicesJson == null)
            mDevicesJson = new DevicesJson(readAssetFile(context, "devices.json"));
        return mDevicesJson;
    }

    private static String readAssetFile(Context context, String file) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(file);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ((str = in.readLine()) != null) {
                if (isFirst) isFirst = false;
                else buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Log.e(TAG, "unable to read " + file);
        } finally {
            if (in != null) try {
                in.close();
            } catch (IOException e) {
                Log.e(TAG, "unable to close Reader " + file);
            }
        }
        return null;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int id = resources.getIdentifier(getScreenOrientation(context) == Configuration.ORIENTATION_PORTRAIT ?
                "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
        if (id > 0) return resources.getDimensionPixelSize(id);
        return 0;
    }

    public static int getScreenOrientation(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels <
                context.getResources().getDisplayMetrics().heightPixels ?
                Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE;
    }

}
