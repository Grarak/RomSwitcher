/*
 * Copyright (C) 2015 Willi Ye
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

package com.grarak.rom.switcher.fragments;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.grarak.rom.switcher.R;
import com.grarak.rom.switcher.elements.ROMCardViewItem;
import com.grarak.rom.switcher.elements.RecyclerViewFragment;
import com.grarak.rom.switcher.utils.ROM;
import com.grarak.rom.switcher.utils.Utils;
import com.grarak.rom.switcher.utils.root.RootUtils;

/**
 * Created by willi on 17.01.15.
 */
public class ROMFragment extends RecyclerViewFragment {

    public enum STORAGE {
        INTERNAL, EXTERNAL
    }

    private static final String ARG_STORAGE = "storage_path";

    private String PATH;
    private MenuItem rebootDefault;

    public static ROMFragment newInstance(STORAGE storage) {
        ROMFragment romFragment = new ROMFragment();
        Bundle args = new Bundle();
        String mPath = null;

        if (storage == STORAGE.EXTERNAL) {
            for (String path : Utils.getDevicesJson(null).getExternalStorages())
                if (RootUtils.fileExist(path)) {
                    mPath = path;
                    break;
                }
        } else mPath = "/data/media";

        args.putString(ARG_STORAGE, mPath);
        romFragment.setArguments(args);
        return romFragment;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        PATH = getArguments().getString(ARG_STORAGE) + "/.romswitcher";
        setTextTitle(PATH);
        create();
    }

    private void create() {
        removeAllViews();
        ROM roms = new ROM(PATH);
        for (int i = 0; i < roms.getLength(); i++) {
            ROMCardViewItem.DROMCardView mROMCard = new ROMCardViewItem.DROMCardView(PATH,
                    PATH.startsWith("/data/media"));
            mROMCard.setName(roms.getName(i));
            mROMCard.setSize(roms.getSize(i));
            mROMCard.setOnEditROMListener(new ROMCardViewItem.OnEditROMListener() {
                @Override
                public void onChange() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            create();
                        }
                    });
                }
            });

            addView(mROMCard);
        }

        if (roms.getLength() < 1) setTextTitle(getString(R.string.no_roms_found));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        rebootDefault = menu.add(getString(R.string.default_rom));
        rebootDefault.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == rebootDefault.getItemId()) {
            Utils.confirmDialog(getString(R.string.reboot), getString(R.string.reboot_to,
                    getString(R.string.default_rom)), new Utils.OnConfirmDialogListener() {
                @Override
                public void onDismiss() {
                }

                @Override
                public void onConfirm() {
                    Utils.setROM(null, "default");
                    Utils.reboot();
                }
            }, getActivity());
        }
        return super.onOptionsItemSelected(item);
    }
}
