package com.grarak.romswitcher.fragments;

/**
 * Created by grarak's kitten (meow) on 31.03.14.
 */

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.grarak.romswitcher.R;
import com.grarak.romswitcher.activities.RomSwitcherActivity;
import com.grarak.romswitcher.utils.Constants;
import com.grarak.romswitcher.utils.GetConntection;
import com.grarak.romswitcher.utils.StartDownload;
import com.grarak.romswitcher.utils.Utils;

public class DownloadFragment extends PreferenceFragment implements Constants {

    private Utils utils = new Utils();

    private final String KEY_CURRENT_VERSION = "current_version";
    private final String KEY_LAST_VERSION = "last_version";
    private final String KEY_DOWNLOAD_CONFIGURATION_FILE = "download_configuration_file";
    private final String KEY_DOWNLOAD_TOOLS = "download_tools";

    private boolean getConfigurationFile = false;
    private boolean getKernel = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.download_header);

        findPreference(KEY_CURRENT_VERSION).setSummary(getCurrentVersion());
        findPreference(KEY_LAST_VERSION).setSummary(getLastVersion());
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == findPreference(KEY_DOWNLOAD_CONFIGURATION_FILE))
            getConfigurationFile();
        else if (preference == findPreference(KEY_DOWNLOAD_TOOLS))
            if (utils.existfile(configurationFile)) {
                getKernel = true;
                getConfigurationFile();
            } else
                utils.toast(getString(R.string.download_configuration_first), getActivity());

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void getConfigurationFile() {
        getConfigurationFile = true;
        getConntect(configurationFileLink);
    }

    private void getConntect(String url) {
        utils.getConnection(url);
        new Connection().execute();
    }

    private class Connection extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return GetConntection.htmlstring;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            RomSwitcherActivity.showProgress(true);
        }

        @Override
        protected void onPostExecute(String result) {
            if (GetConntection.htmlstring.isEmpty() && !GetConntection.htmlstring.contains("<devices>"))
                utils.toast(getString(R.string.noconnection), getActivity());
            else {
                if (getConfigurationFile) {
                    utils.deleteFile(configurationFile);
                    utils.writeFile(configurationFile, GetConntection.htmlstring);
                    getConfigurationFile = false;

                    if (!utils.existfile(configurationFile))
                        utils.toast(getString(R.string.something_went_wrong), getActivity());

                    RomSwitcherActivity.showProgress(false);

                    if (getKernel) {
                        if (getDownloadLink().isEmpty())
                            utils.toast(getString(R.string.no_support), getActivity());
                        else
                            startDownload(getDownloadLink());

                        getKernel = false;
                    } else
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
        StartDownload downloadTask = new StartDownload(getActivity(), downloadPath, "tools.zip");
        downloadTask.execute(url);
    }

}
