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

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.grarak.romswitcher.R;
import com.grarak.romswitcher.activities.RomSwitcherActivity;
import com.grarak.romswitcher.utils.Constants;
import com.grarak.romswitcher.utils.Download;
import com.grarak.romswitcher.utils.Utils;

public class DownloadFragment extends PreferenceFragment implements Constants {

    private Utils utils = new Utils();

    private final String KEY_APP_VERSION = "app_version";
    private final String KEY_CURRENT_VERSION = "current_version";
    private final String KEY_LAST_VERSION = "last_version";
    private final String KEY_DOWNLOAD_CONFIGURATION_FILE = "download_configuration_file";
    private final String KEY_DOWNLOAD_TOOLS = "download_tools";

    private boolean getTools = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.download_header);

        // Get application version
        try {
            findPreference(KEY_APP_VERSION).setSummary(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "unable to read app version");
        }
        findPreference(KEY_CURRENT_VERSION).setSummary(getCurrentVersion());
        findPreference(KEY_LAST_VERSION).setSummary(getLastVersion());
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == findPreference(KEY_DOWNLOAD_CONFIGURATION_FILE))
            getConntect(configurationFileLink);
        else if (preference == findPreference(KEY_DOWNLOAD_TOOLS))
            if (utils.existfile(configurationFile)) {
                getTools = true;
                getConntect(configurationFileLink);
            } else
                utils.toast(getString(R.string.download_configuration_first), getActivity());

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void getConntect(String url) {
        utils.getConnection(url);
        new Connection().execute();
    }

    private class Connection extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return com.grarak.romswitcher.utils.Connection.htmlstring;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            RomSwitcherActivity.showProgress(true);
        }

        @Override
        protected void onPostExecute(String result) {
            RomSwitcherActivity.showProgress(false);
            if (com.grarak.romswitcher.utils.Connection.htmlstring.isEmpty() && !com.grarak.romswitcher.utils.Connection.htmlstring.contains("<devices>"))
                utils.toast(getString(R.string.noconnection), getActivity());
            else {
                if (getTools) {
                    if (!utils.isSupported())
                        utils.toast(getString(R.string.no_support), getActivity());
                    else startDownload(getDownloadLink());

                    getTools = false;
                } else {
                    utils.deleteFile(configurationFile);
                    utils.writeFile(configurationFile, com.grarak.romswitcher.utils.Connection.htmlstring);

                    if (!utils.existfile(configurationFile))
                        utils.toast(getString(R.string.something_went_wrong), getActivity());

                    /*
                     * Restart application after downloading the configuration file,
                     * thus the other fragments will appear without to add them again.
                     */
                    utils.reset(getActivity());
                }
            }
        }
    }

    private String getCurrentVersion() {
        return utils.getCurrentVersion().isEmpty() ? getString(R.string.unknown) : utils.getCurrentVersion();
    }

    private String getLastVersion() {
        return utils.getLastVersion().isEmpty() ? getString(R.string.unknown) : utils.getLastVersion();
    }

    private String getDownloadLink() {
        return utils.getDownloadLink();
    }

    private void startDownload(String url) {
        Download downloadTask = new Download(getActivity(), downloadPath, "tools.zip");
        downloadTask.execute(url);
    }

}
