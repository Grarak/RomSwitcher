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
import android.support.v4.widget.SwipeRefreshLayout;

import com.grarak.rom.switcher.R;
import com.grarak.rom.switcher.elements.DownloadCardViewItem;
import com.grarak.rom.switcher.elements.RecyclerViewFragment;
import com.grarak.rom.switcher.utils.Utils;
import com.grarak.rom.switcher.utils.json.DevicesJson;
import com.grarak.rom.switcher.utils.json.DownloadsJson;
import com.grarak.rom.switcher.utils.root.RootUtils;
import com.grarak.rom.switcher.utils.task.WebpageReaderTask;

/**
 * Created by willi on 16.01.15.
 */
public class InstallationFragment extends RecyclerViewFragment {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DownloadsJson mDownloadsJson;

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_view);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.color_primary));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        create();
                    }
                });
            }
        });

        create();
    }

    private void create() {
        readFromWebpage(Utils.getDevicesJson(getActivity()).getConfig(), new WebpageReaderTask.WebpageListener() {
            @Override
            public void onWebpageResult(final String raw, String html) {
                setTextTitle(getString(R.string.done) + "!");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                        refresh(raw);
                    }
                });
            }
        });
    }

    private void refresh(String json) {
        if (mDownloadsJson == null)
            mDownloadsJson = new DownloadsJson(json);
        else mDownloadsJson.refresh(json);

        removeAllViews();
        for (int i = 0; i < mDownloadsJson.getLength(); i++) {
            DownloadCardViewItem.DDownloadCardView mDownloadCard = new DownloadCardViewItem.DDownloadCardView();
            mDownloadCard.setVersion(mDownloadsJson.getVersion(i));
            mDownloadCard.setMd5sum(mDownloadsJson.getMd5sum(i));
            mDownloadCard.setDownload(mDownloadsJson.getLink(i));
            mDownloadCard.setChangelog(mDownloadsJson.getChangelog(i));
            mDownloadCard.setOnDownloadListener(new DownloadCardViewItem.OnDownloadListener() {

                private boolean installing;

                @Override
                public void onSuccess() {
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
                            RootUtils.writePartition("/sdcard/romswitcher/download.img", json.getBootPartition());
                            installing = false;
                            setTextTitle(getString(R.string.done) + "!");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.confirmDialog(null, getString(R.string.installation_success),
                                            new Utils.OnConfirmDialogListener() {
                                                @Override
                                                public void onDismiss() {
                                                }

                                                @Override
                                                public void onConfirm() {
                                                    Utils.reboot();
                                                }
                                            }, getActivity());
                                }
                            });
                        }
                    }).start();
                }
            });

            String text;
            if ((text = mDownloadsJson.getSize(i)) != null)
                mDownloadCard.setSize(text);
            if ((text = mDownloadsJson.getNote(i)) != null)
                mDownloadCard.setNote(text);

            addView(mDownloadCard);
        }

        animateRecyclerView();
    }

    @Override
    public int getMainViewId() {
        return R.layout.swiperefresh_fragment;
    }
}
