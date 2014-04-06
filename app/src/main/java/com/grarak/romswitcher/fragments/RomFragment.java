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
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.grarak.romswitcher.R;
import com.grarak.romswitcher.utils.Backup;
import com.grarak.romswitcher.utils.Constants;
import com.grarak.romswitcher.utils.CreateImage;
import com.grarak.romswitcher.utils.RebootRom;
import com.grarak.romswitcher.utils.RootUtils;
import com.grarak.romswitcher.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.grarak.romswitcher.utils.Utils.ProgressDialog;

public class RomFragment extends PreferenceFragment implements Constants {

    private Context context;

    private Utils utils = new Utils();
    private RootUtils root = new RootUtils();

    private static final String ARG_SECTION_NUMBER = "section_number";
    private int currentFragment = 0;
    private int systemsize = utils.getSystemImageSize();

    public static RomFragment newInstance(int sectionNumber) {
        RomFragment fragment = new RomFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    private final String KEY_ROM_HEADER = "rom_header";
    private final String KEY_ROM_CATEGORY = "rom_category";
    private final String KEY_REBOOT_ROM = "reboot_rom";
    private final String KEY_ADVANCED_CATEGORY = "advanced_category";
    private final String KEY_REMOVE_ROM = "remove_rom";
    private final String KEY_SYSTEM_SIZE = "system_size";
    private final String KEY_BACKUP_ROM = "backup_rom";
    private final String KEY_RESTORE_ROM = "restore_rom";

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
        findPreference(KEY_BACKUP_ROM).setTitle(getString(R.string.backup_rom, currentFragment));
        findPreference(KEY_RESTORE_ROM).setTitle(getString(R.string.restore_rom, currentFragment));

        PreferenceScreen mRomHeader = (PreferenceScreen) findPreference(KEY_ROM_HEADER);
        PreferenceCategory mAdvancedCategory = (PreferenceCategory) findPreference(KEY_ADVANCED_CATEGORY);
        if (currentFragment == 1) mRomHeader.removePreference(mAdvancedCategory);
        else {

            if (!utils.existfile(utils.dataPath + "/media/." + currentFragment + "rom"))
                root.run("mkdir -p " + utils.dataPath + "/media/." + currentFragment + "rom");

        }

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
        else if (preference == findPreference(KEY_BACKUP_ROM))
            backup();
        else if (preference == findPreference(KEY_RESTORE_ROM))
            restore();

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void reboot() {
        AlertDialog.Builder warning = new AlertDialog.Builder(getActivity());
        warning.setMessage(getString(R.string.reboot_now))
                .setPositiveButton(getString(R.string.yes
                ), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new RebootRom(getActivity(), currentFragment).execute();
                    }
                }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).show();
    }

    private void remove() {
        AlertDialog.Builder warning = new AlertDialog.Builder(getActivity());
        warning.setMessage(getString(R.string.you_sure))
                .setPositiveButton(getString(R.string.yes
                ), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        root.run(String.valueOf("rm -rf " + utils.dataPath + "/media/." + currentFragment + "rom" + "/*"));
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
        picker.setValue(systemimage.exists() ? (int) (systemimage.length() / 1048576) : systemsize);
        picker.setMinValue(500);
        picker.setWrapSelectorWheel(false);

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
                systemsize = picker.getValue();
                new CreateImage(getActivity(), systemsize, currentFragment).execute();
            }
        }).show();
    }

    private void backup() {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(0, 10, 0, 0);

        final EditText nameEdit = new EditText(getActivity());
        nameEdit.setHint(getString(R.string.backup_name));

        layout.addView(nameEdit);

        AlertDialog.Builder namer = new AlertDialog.Builder(getActivity());
        namer.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).setPositiveButton(getString(R.string.apply), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (nameEdit.getText().toString().isEmpty()) {
                    utils.toast(getString(R.string.empty_name), getActivity());
                } else {
                    File backupFolder = new File(backupPath + "/" + String.valueOf(currentFragment) + "rom/" + nameEdit.getText().toString());
                    if (!backupFolder.exists())
                        new Backup(getActivity(), nameEdit.getText().toString(), currentFragment).execute();
                    else
                        overwritebackup(nameEdit.getText().toString());
                }
            }
        }).setView(layout).show();
    }

    private void overwritebackup(final String name) {
        AlertDialog.Builder warn = new AlertDialog.Builder(getActivity());
        warn.setMessage(getString(R.string.overwrite_backup))
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new Backup(getActivity(), name, currentFragment).execute();
            }
        }).show();
    }

    private int selected = 0;
    private int buffKey = 0;

    private void restore() {
        File backup = new File(backupPath + "/" + String.valueOf(currentFragment) + "rom");

        if (backup.exists() && backup.listFiles() != null) {
            List<String> listItems = new ArrayList<String>();

            for (File file : backup.listFiles())
                listItems.add(file.getName());

            final CharSequence[] choiceList = listItems.toArray(new CharSequence[listItems.size()]);

            AlertDialog.Builder listbackup = new AlertDialog.Builder(getActivity());
            listbackup.setSingleChoiceItems(choiceList, selected,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            buffKey = i;
                        }
                    }
            ).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).setPositiveButton(getString(R.string.apply), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new Restore(choiceList[buffKey].toString()).execute();
                }
            }).show();
        } else utils.toast(getString(R.string.no_backup_found, currentFragment), getActivity());
    }

    private class Restore extends AsyncTask<String, Integer, String> {

        private PowerManager.WakeLock mWakeLock;

        private String name;

        public Restore(String name) {
            this.name = name;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                File rom = new File(utils.dataPath + "/media/." + String.valueOf(currentFragment) + "rom");
                String path = backupPath + "/" + String.valueOf(currentFragment) + "rom/" + name;

                if (rom.exists()) root.run("rm -rf " + rom.toString());
                root.run("mkdir -p " + rom.toString());

                Thread.sleep(1000);

                root.run("cp -rf " + path + "/* " + rom.toString());

                long backupsize = utils.getFolderSize(path);

                while (backupsize != utils.getFolderSize(rom.toString()))
                    publishProgress((int) (utils.getFolderSize(rom.toString()) / (backupsize / 100)));

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            utils.showProgressDialog(context.getString(R.string.restoring), true);
            ProgressDialog.setIndeterminate(false);
            ProgressDialog.setMax(100);
            ProgressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            utils.showProgressDialog(context.getString(R.string.restoring), true);

            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mWakeLock.release();

            utils.showProgressDialog("", false);
        }
    }
}
