package com.grarak.romswitcher.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.grarak.romswitcher.R;
import com.grarak.romswitcher.utils.Constants;
import com.grarak.romswitcher.utils.RootUtils;
import com.grarak.romswitcher.utils.Utils;

import java.io.File;

/**
 * Created by grarak's kitten (meow) on 31.03.14.
 */

public class RomFragment extends PreferenceFragment implements Constants {

    private Context context;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private int currentFragment = 0;
    private int systemsize;

    private Utils utils = new Utils();
    private RootUtils root = new RootUtils();

    public static RomFragment newInstance(int sectionNumber) {
        RomFragment fragment = new RomFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    private final String KEY_ROM_CATEGORY = "rom_category";
    private final String KEY_REBOOT_ROM = "reboot_rom";
    private final String KEY_REMOVE_ROM = "remove_rom";
    private final String KEY_SYSTEM_SIZE = "system_size";

    private boolean showProgressDialog = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.rom_header);
        context = getActivity();

        currentFragment = getArguments().getInt(ARG_SECTION_NUMBER);

        findPreference(KEY_ROM_CATEGORY).setTitle(getString(R.string.rom, currentFragment));
        findPreference(KEY_REBOOT_ROM).setTitle(getString(R.string.reboot_rom, currentFragment));
        findPreference(KEY_REMOVE_ROM).setTitle(getString(R.string.remove_rom, currentFragment));
        findPreference(KEY_SYSTEM_SIZE).setTitle(getString(R.string.system_image_size, currentFragment));

        if (!utils.existfile(utils.dataPath + "/media/." + currentFragment + "rom"))
            root.run("mkdir -p " + utils.dataPath + "/media/." + currentFragment + "rom");

        if (showProgressDialog)
            utils.showProgressDialog(context.getString(R.string.creating), true);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (!utils.existfile(configurationFile))
            utils.toast(getString(R.string.download_configuration_first), getActivity());
        else if (!utils.isRSInstalled())
            utils.toast(getString(R.string.install_tools_first), getActivity());
        else if (preference == findPreference(KEY_REBOOT_ROM))
            reboot();
        else if (preference == findPreference(KEY_REMOVE_ROM))
            remove();
        else if (preference == findPreference(KEY_SYSTEM_SIZE))
            systemsize();

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void reboot() {
        AlertDialog.Builder warning = new AlertDialog.Builder(getActivity());
        warning.setMessage(getString(R.string.reboot_now))
                .setPositiveButton(getString(R.string.yes
                ), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new RebootTask().execute();
                    }
                }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).show();
    }

    private class RebootTask extends AsyncTask<String, Integer, String> {

        private PowerManager.WakeLock mWakeLock;

        @Override
        protected String doInBackground(String... params) {
            root.run("echo " + String.valueOf(currentFragment - 1) + " > " + utils.dataPath + "/media" + romFile);
            root.run("echo 1 > " + utils.dataPath + "/media" + nextbootFile);

            if (!utils.oneKernel() && (utils.isDefaultRom() || currentFragment == 1))
                root.writePartition(currentFragment == 1 ? firstimage : secondimage, utils.getPartition("boot"));

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mWakeLock.release();
            utils.showProgressDialog("", false);

            root.reboot();
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

    private void remove() {
        AlertDialog.Builder warning = new AlertDialog.Builder(getActivity());
        warning.setMessage(getString(R.string.you_sure))
                .setPositiveButton(getString(R.string.yes
                ), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        root.run(String.valueOf("rm -rf " + utils.dataPath + "/media/." + currentFragment + "rom"));
                    }
                }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).show();
    }

    private void systemsize() {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setGravity(Gravity.CENTER);

        File systemimage = new File(utils.dataPath + "/media/." + currentFragment + "rom/system.img");

        final NumberPicker picker = new NumberPicker(getActivity());
        picker.setMaxValue(3000);
        picker.setValue(systemimage.exists() ? (int) (systemimage.length() / 1048576) : utils.getSystemImageSize());
        picker.setMinValue(500);
        picker.setWrapSelectorWheel(false);
        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                systemsize = newVal;
            }
        });

        TextView unit = new TextView(getActivity());
        unit.setText(" " + getString(R.string.mb));
        unit.setTextSize(20);

        layout.addView(picker);
        layout.addView(unit);

        AlertDialog.Builder sizer = new AlertDialog.Builder(getActivity());
        sizer.setView(layout).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setPositiveButton(getString(R.string.apply), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new ImageTask().execute();
            }
        }).show();
    }

    private class ImageTask extends AsyncTask<String, Integer, String> {

        private PowerManager.WakeLock mWakeLock;

        @Override
        protected String doInBackground(String... params) {
            File systemimage = new File(utils.dataPath + "/media/." + currentFragment + "rom/system.img");
            if (systemsize != (int) (systemimage.length() / 1048576)) {
                if (systemimage.exists()) systemimage.delete();
                root.createImage(systemimage.toString(), systemsize);

                while (systemsize != (int) (systemimage.length() / 1048576))
                    publishProgress((int) (systemimage.length() / 1048576));
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            utils.showProgressDialog(context.getString(R.string.creating), true);
            utils.ProgressDialog.setIndeterminate(false);
            utils.ProgressDialog.setMax(systemsize);
            utils.ProgressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            utils.showProgressDialog(context.getString(R.string.creating), true);
            showProgressDialog = true;

            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mWakeLock.release();

            utils.showProgressDialog("", false);
            showProgressDialog = false;
        }
    }
}
