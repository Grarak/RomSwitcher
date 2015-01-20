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
        readFromWebpage(Utils.getDevicesJson(getActivity()).getFaq(), new WebpageReaderTask.WebpageListener() {
            @Override
            public void onWebpageResult(final String raw, final String html) {
                setTextTitle(getString(R.string.faq_full));

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
