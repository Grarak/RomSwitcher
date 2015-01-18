package com.grarak.rom.switcher.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;

import com.grarak.rom.switcher.R;
import com.grarak.rom.switcher.elements.CardViewItem;
import com.grarak.rom.switcher.elements.RecyclerViewFragment;
import com.grarak.rom.switcher.utils.Utils;
import com.grarak.rom.switcher.utils.json.FAQJson;
import com.grarak.rom.switcher.utils.task.WebpageReaderTask;

/**
 * Created by willi on 18.01.15.
 */
public class FAQFragment extends RecyclerViewFragment {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FAQJson mFAQJson;

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

    @Override
    public int getMainViewId() {
        return R.layout.swiperefresh_fragment;
    }

    private void create() {
        new Thread(new Runnable() {

            private boolean loading;

            @Override
            public void run() {
                new WebpageReaderTask(new WebpageReaderTask.WebpageListener() {
                    @Override
                    public void onWebpageResult(final String raw, final String html) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }
                                });
                                loading = false;
                                if (raw == null || raw.isEmpty()) {
                                    setTextTitle(getString(R.string.no_connection));
                                    return;
                                }

                                setTextTitle(getString(R.string.faq_full));

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        refresh(raw);
                                    }
                                });
                            }
                        }).start();
                    }
                }).execute(Utils.getDevicesJson(getActivity()).getFaq());

                int count = 0;
                loading = true;
                while (loading) {
                    try {
                        count++;
                        String title = getString(R.string.loading);
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
    }

    private void refresh(String json) {
        if (mFAQJson == null)
            mFAQJson = new FAQJson(json);
        else mFAQJson.refresh(json);

        removeAllViews();

        for (int i = 0; i < mFAQJson.getLength(); i++) {
            CardViewItem.DCardView mFAQCard = new CardViewItem.DCardView();
            mFAQCard.setTitle(mFAQJson.getQuestion(i));
            mFAQCard.setDescription(mFAQJson.getAnswer(i));

            addView(mFAQCard);
        }

        animateRecyclerView();
    }

}
