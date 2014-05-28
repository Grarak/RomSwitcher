package com.grarak.romswitcher.fragments;

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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import com.grarak.romswitcher.R;
import com.grarak.romswitcher.activities.RomSwitcherActivity;
import com.grarak.romswitcher.utils.Constants;
import com.grarak.romswitcher.utils.RootUtils;
import com.grarak.romswitcher.utils.Utils;

import static com.grarak.romswitcher.utils.Utils.ProgressDialog;

public class InstallationFragment extends PreferenceFragment implements Constants, Preference.OnPreferenceChangeListener {

    private Context context;

    private Utils utils = new Utils();
    private RootUtils root = new RootUtils();

    private final String dev_note = utils.getDevNote();

    private final String KEY_INSTALL_CATEGORY = "install_category";
    private final String KEY_DEV_NOTES = "dev_notes";
    private final String KEY_INSTALL_HEADER = "install_header";
    private final String KEY_INSTALL_TOOLS = "install_tools";
    private final String KEY_RECOVERY_CATEGORY = "recovery_category";
    private final String KEY_REBOOT_INTO_RECOVERY = "reboot_into_recovery";
    private final String KEY_INSTALL_RECOVERY = "install_recovery";
    private final String KEY_SETTINGS_CATEGORY = "settings_category";
    private final String KEY_MANUALBOOT = "manualboot";
    private final String KEY_APPSHARING = "appsharing";

    private PreferenceCategory mInstallCategory;
    private PreferenceScreen mDevNotesHeader;
    private PreferenceScreen mInstallHeader;
    private PreferenceCategory mRecoveryCategory;
    private PreferenceScreen mRebootIntoRecovery;
    private PreferenceScreen mInstallRecovery;
    private PreferenceCategory mSettingsCategory;
    private SwitchPreference mManualboot;
    private SwitchPreference mAppsharing;

    private boolean installTools = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.installation_header);
        context = getActivity();

        // Initialize all Preferences
        mInstallCategory = (PreferenceCategory) findPreference(KEY_INSTALL_CATEGORY);
        mDevNotesHeader = (PreferenceScreen) findPreference(KEY_DEV_NOTES);
        mInstallHeader = (PreferenceScreen) findPreference(KEY_INSTALL_HEADER);
        mRecoveryCategory = (PreferenceCategory) findPreference(KEY_RECOVERY_CATEGORY);
        mRebootIntoRecovery = (PreferenceScreen) findPreference(KEY_REBOOT_INTO_RECOVERY);
        mInstallRecovery = (PreferenceScreen) findPreference(KEY_INSTALL_RECOVERY);

        if (!dev_note.equals("0") && !dev_note.isEmpty())
            mDevNotesHeader.setSummary(dev_note);
        else
            mInstallCategory.removePreference(mDevNotesHeader);

        // Check if the devices support those features
        if (!(utils.rebootRecovery() || utils.installRecovery()))
            mInstallHeader.removePreference(mRecoveryCategory);
        if (!utils.rebootRecovery()) mRecoveryCategory.removePreference(mRebootIntoRecovery);
        if (!utils.installRecovery()) mRecoveryCategory.removePreference(mInstallRecovery);

        mSettingsCategory = (PreferenceCategory) findPreference(KEY_SETTINGS_CATEGORY);

        mManualboot = (SwitchPreference) findPreference(KEY_MANUALBOOT);
        mManualboot.setChecked(utils.existfile(manualbootFile));
        mManualboot.setOnPreferenceChangeListener(this);

        mAppsharing = (SwitchPreference) findPreference(KEY_APPSHARING);
        mAppsharing.setChecked(utils.existfile(appsharingFile));
        mAppsharing.setOnPreferenceChangeListener(this);

        if (!utils.manualBoot()) mSettingsCategory.removePreference(mManualboot);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference != mDevNotesHeader)
            if (!utils.existfile(configurationFile))
                utils.toast(getString(R.string.download_configuration_first), getActivity());
            else {
                if (preference == findPreference(KEY_INSTALL_TOOLS))
                    if (utils.existfile(toolfile)) {
                        if (utils.oneKernel())
                            checkPartition("boot", "tools");
                        else
                            installTools();
                    } else
                        utils.toast(getString(R.string.download_tools_first), getActivity());
                else if (!utils.isRSInstalled())
                    utils.toast(getString(R.string.install_tools_first), getActivity());
                else if (preference == findPreference(KEY_REBOOT_INTO_RECOVERY))
                    checkReboot(true);
                else if (preference == findPreference(KEY_INSTALL_RECOVERY))
                    checkPartition("recovery", "recovery");
            }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference == mManualboot) {
            if (!mManualboot.isChecked())
                utils.writeFile(manualbootFile, "1");
            else
                utils.deleteFile(manualbootFile);
            return true;
        } else if (preference == mAppsharing) {
            if (!mAppsharing.isChecked())
                utils.writeFile(appsharingFile, "1");
            else
                utils.deleteFile(appsharingFile);
            return true;
        }
        return false;
    }

    public class InstallationTask extends AsyncTask<String, Integer, String> {

        private PowerManager.WakeLock mWakeLock;

        @Override
        protected String doInBackground(String... sUrl) {
            if (installTools) {
                if (utils.unZip(downloadPath + "/", "tools.zip")) {
                    if (utils.oneKernel()) {
                        if (utils.existfile(onekernelImage)) {
                            root.writePartition(onekernelImage, utils.getPartition("boot"));
                        } else return "error";
                    } else root.readPartition(utils.getPartition("boot"), firstimage);
                    /*
                     * We don't do anything just let the user think that we are installing something
                     * I like to troll (meow)
                     */
                    for (int i = 0; i < 100; i++) {
                        try {
                            Thread.sleep(50);
                            publishProgress(i);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else return "error";
            } else
                root.writePartition(utils.oneKernel() ? utils.getPartition("boot") : secondimage, utils.getPartition("recovery"));
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (installTools)
                utils.showProgressDialog(context.getString(R.string.installing), true);
            else
                RomSwitcherActivity.showProgress(true);

            PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            if (installTools && (ProgressDialog != null)) {
                utils.showProgressDialog(context.getString(R.string.installing), true);
                ProgressDialog.setIndeterminate(false);
                ProgressDialog.setMax(100);
                ProgressDialog.setProgress(progress[0]);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mWakeLock.release();

            utils.toast(context.getString(result != null ? R.string.something_went_wrong : R.string.done), context);

            if (installTools && ProgressDialog != null) {
                utils.showProgressDialog("", false);
                if (utils.oneKernel() && result == null) checkReboot(false);
            } else
                RomSwitcherActivity.showProgress(false);
        }
    }

    private void installTools() {
        InstallationTask installTask = new InstallationTask();
        installTask.execute();
    }

    private void installRecovery() {
        installTools = false;
        InstallationTask installTask = new InstallationTask();
        installTask.execute();
    }

    private void checkPartition(String partition, final String installation) {
        AlertDialog.Builder warning = new AlertDialog.Builder(getActivity());
        warning.setMessage(getString(R.string.check_boot_partition, "\"" + utils.getPartition(partition) + "\"", partition))
                .setPositiveButton(getString(R.string.yes
                ), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (installation.equals("tools")) installTools();
                        else if (installation.equals("recovery")) installRecovery();
                    }
                }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).show();
    }

    private void checkReboot(final boolean recovery) {
        AlertDialog.Builder rebooter = new AlertDialog.Builder(getActivity());
        rebooter.setMessage(getString(R.string.reboot_now))
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (recovery) root.run("echo 1 > " + rebootRecoveryFile);
                root.reboot();
            }
        }).show();
    }

}
