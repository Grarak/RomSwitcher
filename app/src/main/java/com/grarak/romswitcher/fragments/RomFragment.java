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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.grarak.romswitcher.R;
import com.grarak.romswitcher.activities.FileBrowserActivity;
import com.grarak.romswitcher.activities.RomInformationActivity;
import com.grarak.romswitcher.utils.Backup;
import com.grarak.romswitcher.utils.Constants;
import com.grarak.romswitcher.utils.Delete;
import com.grarak.romswitcher.utils.RebootRom;
import com.grarak.romswitcher.utils.Restore;
import com.grarak.romswitcher.utils.RootUtils;
import com.grarak.romswitcher.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RomFragment extends PreferenceFragment implements Constants {

    private Utils utils = new Utils();
    private RootUtils root = new RootUtils();

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String ARG_FILTER = "filter";
    private final String ARG_RESULT = "result";
    private int currentFragment = 0;

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
    private final String KEY_CHOOSE_KERNEL = "choose_kernel";
    private final String KEY_REMOVE_ROM = "remove_rom";
    private final String KEY_BACKUP_ROM = "backup_rom";
    private final String KEY_RESTORE_ROM = "restore_rom";
    private final String KEY_DELETE_BACKUP = "delete_backup";
    private final String KEY_ROM_INFORMATION = "rom_information";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.rom_header);

        currentFragment = getArguments().getInt(ARG_SECTION_NUMBER);

        // Set titles of Preferences
        findPreference(KEY_ROM_CATEGORY).setTitle(getString(R.string.rom, currentFragment));
        findPreference(KEY_REBOOT_ROM).setTitle(getString(R.string.reboot_rom, currentFragment));
        findPreference(KEY_CHOOSE_KERNEL).setTitle(getString(R.string.choose_kernel, currentFragment));
        findPreference(KEY_CHOOSE_KERNEL).setSummary(getString(R.string.choose_kernel_summary, currentFragment));
        findPreference(KEY_REMOVE_ROM).setTitle(getString(R.string.remove_rom, currentFragment));
        findPreference(KEY_BACKUP_ROM).setTitle(getString(R.string.backup_rom, currentFragment));
        findPreference(KEY_RESTORE_ROM).setTitle(getString(R.string.restore_rom, currentFragment));
        findPreference(KEY_DELETE_BACKUP).setTitle(getString(R.string.delete_backup, currentFragment));
        findPreference(KEY_ROM_INFORMATION).setTitle(getString(R.string.rom_information, currentFragment));

        // Remove advanced settings in first tab
        if (currentFragment == 1)
            ((PreferenceScreen) findPreference(KEY_ROM_HEADER)).removePreference((PreferenceCategory) findPreference(KEY_ADVANCED_CATEGORY));
        else {
            // Create folders of roms
            if (!utils.existfile("/data/media/." + String.valueOf(currentFragment) + "rom"))
                root.run("mkdir -p /data/media/." + String.valueOf(currentFragment) + "rom");
            // Remove choose kernel option if device doesn't use kexec hardboot
            if (!utils.kexecHardboot())
                ((PreferenceCategory) findPreference(KEY_ADVANCED_CATEGORY)).removePreference((PreferenceScreen) findPreference(KEY_CHOOSE_KERNEL));
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
        else if (preference == findPreference(KEY_CHOOSE_KERNEL))
            startActivity(FileBrowserActivity.class);
        else if (preference == findPreference(KEY_REMOVE_ROM))
            remove();
        else if (preference == findPreference(KEY_BACKUP_ROM))
            backup();
        else if (preference == findPreference(KEY_RESTORE_ROM))
            showBackupList(true);
        else if (preference == findPreference(KEY_DELETE_BACKUP))
            showBackupList(false);
        else if (preference == findPreference(KEY_ROM_INFORMATION))
            startActivity(RomInformationActivity.class);

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void reboot() {
        AlertDialog.Builder warning = new AlertDialog.Builder(getActivity());
        if (utils.kexecHardboot() && !utils.isDefaultRom()) {
            warning.setMessage(getString(R.string.cannot_reboot, currentFragment))
                    .setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
        } else {
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
            });
        }
        warning.show();
    }

    private void remove() {
        AlertDialog.Builder warning = new AlertDialog.Builder(getActivity());
        warning.setMessage(getString(R.string.you_sure))
                .setPositiveButton(getString(R.string.yes
                ), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new Delete("/data/media/." + String.valueOf(currentFragment) + "rom" + "/*", getActivity()).execute();
                    }
                }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
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
                    File backupFolder = new File(backupPath + "/" + String.valueOf(currentFragment) + "rom/" + nameEdit.getText().toString() + ".tar");
                    if (!backupFolder.exists())
                        new Backup(getActivity(), nameEdit.getText().toString(), currentFragment).execute();
                    else deletebackupfirst();
                }
            }
        }).setView(layout).show();
    }

    private void deletebackupfirst() {
        AlertDialog.Builder warn = new AlertDialog.Builder(getActivity());
        warn.setMessage(getString(R.string.delete_backup_first))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).show();
    }

    private int selected = 0;
    private int buffKey = 0;

    private void showBackupList(final boolean restore) {
        File folder = new File(backupPath + "/" + String.valueOf(currentFragment) + "rom");

        List<String> listItems = new ArrayList<String>();

        // Check if folder exists
        if (folder.listFiles() != null) {
            // Check if folder contains anything
            if (folder.listFiles().length > 0) {
                for (File file : folder.listFiles())
                    listItems.add(file.getName().replace(".tar", ""));

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
                }).setPositiveButton(getString(restore ? R.string.restore : R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (restore)
                            new Restore(getActivity(), choiceList[buffKey].toString(), currentFragment).execute();
                        else
                            new Delete(backupPath + "/" + String.valueOf(currentFragment) + "rom/" + choiceList[buffKey].toString() + ".tar", getActivity()).execute();

                    }
                }).show();
            } else utils.toast(getString(R.string.no_backup_found, currentFragment), getActivity());
        } else utils.toast(getString(R.string.no_backup_found, currentFragment), getActivity());
    }

    private void startActivity(Class clas) {
        if (clas == RomInformationActivity.class)
            utils.showProgressDialog(getString(R.string.loading), true);
        Intent info = new Intent(getActivity(), clas);
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, currentFragment);
        if (clas == FileBrowserActivity.class) args.putString(ARG_FILTER, "img");
        info.putExtras(args);
        if (clas == FileBrowserActivity.class) startActivityForResult(info, 1);
        else startActivity(info);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check if the resultcode is correct
        if (requestCode == 1)
            if (resultCode == Activity.RESULT_OK) {
                try {
                    // Unpack kernel
                    String result = data.getStringExtra(ARG_RESULT);
                    new File(kexecPath + "/" + currentFragment + "rom").mkdirs();

                    root.run("rm -rf " + kexecPath + "/" + currentFragment + "rom/*");
                    root.run("cp -f " + result + " " + kexecPath + "/" + currentFragment + "rom/boot.img");
                    root.run(kexecPath + "/unpackbootimg -i " + kexecPath + "/" + currentFragment + "rom/boot.img -o " + kexecPath + "/" + currentFragment + "rom");

                    // If base flag is not defined to not check for compatibility of selected kernel
                    String kernelBase = utils.getKernelBase();
                    if (!kernelBase.equals("0")) {

                        // Sleep to make sure device is done with unpacking
                        Thread.sleep(1000);

                        // Check if unpack was successful
                        String bootimgBase = utils.readFile(kexecPath + "/" + currentFragment + "rom/boot.img-base").split("\r?\n")[0];
                        if (!utils.getKernelBase().replace("0x", "").equals(bootimgBase)) {
                            root.run("rm -rf " + kexecPath + "/" + currentFragment + "rom/*");
                            AlertDialog.Builder error = new AlertDialog.Builder(getActivity());
                            error.setMessage(getString(R.string.choose_kernel_error))
                                    .setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    }).show();
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, "unable to read " + kexecPath + "/" + currentFragment + "rom/boot.img-base");
                }
            }
    }
}