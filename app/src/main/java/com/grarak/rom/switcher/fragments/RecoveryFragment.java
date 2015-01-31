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

import com.grarak.rom.switcher.R;
import com.grarak.rom.switcher.elements.CardViewItem;
import com.grarak.rom.switcher.elements.RecyclerViewFragment;
import com.grarak.rom.switcher.utils.Utils;
import com.grarak.rom.switcher.utils.json.DevicesJson;
import com.grarak.rom.switcher.utils.root.RootUtils;

/**
 * Created by willi on 17.01.15.
 */
public class RecoveryFragment extends RecyclerViewFragment {

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        setTextTitle(getString(R.string.select_action));
        if (Utils.getDevicesJson(getActivity()).rebootRecovery()) {
            CardViewItem.DCardView mRebootRecoveryCard = new CardViewItem.DCardView();
            mRebootRecoveryCard.setTitle(getString(R.string.reboot_to_recovery));
            mRebootRecoveryCard.setDescription(getString(R.string.reboot_to_recovery_summary));
            mRebootRecoveryCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
                @Override
                public void onClick(CardViewItem.DCardView dCardView) {
                    Utils.confirmDialog(null, getString(R.string.confirm), new Utils.OnConfirmDialogListener() {
                        @Override
                        public void onDismiss() {
                        }

                        @Override
                        public void onConfirm() {
                            Utils.rebootRecovery();
                        }
                    }, getActivity());
                }
            });

            addView(mRebootRecoveryCard);
        }

        if (Utils.getDevicesJson(getActivity()).installRecovery()) {
            CardViewItem.DCardView mInstallRecoveryCard = new CardViewItem.DCardView();
            mInstallRecoveryCard.setTitle(getString(R.string.install_recovery));
            mInstallRecoveryCard.setDescription(getString(R.string.install_recovery_summary));
            mInstallRecoveryCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
                @Override
                public void onClick(CardViewItem.DCardView dCardView) {
                    Utils.confirmDialog(null, getString(R.string.confirm), new Utils.OnConfirmDialogListener() {

                        private boolean installing;

                        @Override
                        public void onDismiss() {
                        }

                        @Override
                        public void onConfirm() {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    int count = 0;
                                    installing = true;
                                    while (installing) {
                                        try {
                                            count++;
                                            String title = getString(R.string.installing);
                                            for (int i = 0; i < count; i++)
                                                title += " .";
                                            setTextTitle(title);
                                            if (count >= 5) count = 0;
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }).start();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    DevicesJson json = Utils.getDevicesJson(getActivity());
                                    RootUtils.writePartition(json.getBootPartition(), json.getRecoveryPartition());
                                    installing = false;
                                    setTextTitle(getString(R.string.select_action));
                                }
                            }).start();
                        }
                    }, getActivity());
                }
            });

            addView(mInstallRecoveryCard);
        }
    }
}
