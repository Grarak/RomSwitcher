package com.grarak.rom.switcher.elements;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.grarak.rom.switcher.R;
import com.grarak.rom.switcher.utils.Utils;
import com.grarak.rom.switcher.utils.task.WebpageReaderTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 12.01.15.
 */
public class RecyclerViewFragment extends Fragment {

    protected LayoutInflater inflater;
    protected ViewGroup container;
    protected View view;
    protected FrameLayout mBackgroundView;
    protected TextView mTitleView;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
    protected Handler handler;

    private ProgressBar progressBar;
    private RecyclerViewAdapter.Adapter mAdapter;
    private List<RecyclerViewAdapter.ViewInterface> views = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        this.inflater = inflater;
        this.container = container;

        view = inflater.inflate(getMainViewId(), container, false);
        Drawable actionBarDrawable = getResources().getDrawable(R.color.color_primary);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(actionBarDrawable);

        mBackgroundView = (FrameLayout) view.findViewById(getBackgroundViewId());
        mTitleView = (TextView) view.findViewById(getTextViewId());
        mRecyclerView = (RecyclerView) view.findViewById(getRecyclerViewId());
        mLayoutManager = setRecyclerLayoutManager();
        setLayouts();

        mAdapter = new RecyclerViewAdapter.Adapter(views);
        mRecyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 0;
            }
        });
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final int currentFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
                if (currentFirstVisibleItem > 1) {
                    actionBar.hide();
                } else if (currentFirstVisibleItem == 0) {
                    actionBar.show();
                }
            }
        });

        progressBar = new ProgressBar(getActivity());
        setProgressBar(progressBar);

        new Task().execute(savedInstanceState);

        return view;
    }

    public void setProgressBar(ProgressBar progressBar) {
        progressBar.getIndeterminateDrawable().setColorFilter(new LightingColorFilter(0xFF000000,
                getResources().getColor(android.R.color.white)));
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(progressBar, new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.END));
    }

    private class Task extends AsyncTask<Bundle, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((ActionBarActivity) getActivity()).getSupportActionBar().show();
            views.clear();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handler = new Handler();
                }
            });
        }

        @Override
        protected String doInBackground(Bundle... params) {
            try {
                if (isAdded()) init(params[0]);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            mRecyclerView.setAdapter(mAdapter);
            animateRecyclerView();

            try {
                ((ViewGroup) progressBar.getParent()).removeView(progressBar);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

    }

    public void init(Bundle savedInstanceState) {
    }

    public void addView(RecyclerViewAdapter.ViewInterface view) {
        if (views.indexOf(view) < 0) {
            views.add(view);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void removeView(RecyclerViewAdapter.ViewInterface view) {
        int position = views.indexOf(view);
        if (position > -1) {
            views.remove(view);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void removeAllViews() {
        views.clear();
        mAdapter.notifyDataSetChanged();
    }

    public int getCount() {
        return views.size();
    }

    public int getMainViewId() {
        return R.layout.recycler_view;
    }

    public int getRecyclerViewId() {
        return R.id.recycler_view;
    }

    public int getTextViewId() {
        return R.id.title_view;
    }

    public int getBackgroundViewId() {
        return R.id.background_view;
    }

    public void setTextTitle(final String title) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTitleView != null) mTitleView.setText(title);
            }
        });
    }

    public LinearLayoutManager setRecyclerLayoutManager() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setSmoothScrollbarEnabled(true);
        mRecyclerView.setLayoutManager(layoutManager);
        return layoutManager;
    }

    public void setLayouts() {
        int padding = getSidePadding();
        if (mRecyclerView != null) mRecyclerView.setPadding(padding, 0, padding, 0);

        if (mBackgroundView != null) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBackgroundView.getLayoutParams();
            params.height = getBackgroundHeight();
            mBackgroundView.setLayoutParams(params);
            mBackgroundView.setPadding(padding, 0, padding, 0);
        }
    }

    public void animateRecyclerView() {
        Context context = getActivity();
        if (context != null)
            mRecyclerView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.bottom_to_top));
    }

    private int getSidePadding() {
        double padding = getResources().getDisplayMetrics().widthPixels * 0.08361204013;
        return Utils.getScreenOrientation(getActivity()) == Configuration.ORIENTATION_LANDSCAPE ? (int) padding : 0;
    }

    public int getBackgroundHeight() {
        TypedArray ta = getActivity().obtainStyledAttributes(new int[]{R.attr.actionBarSize});
        int actionBarSize = ta.getDimensionPixelSize(0, 100);
        ta.recycle();
        int orientation = Utils.getScreenOrientation(getActivity());
        int ret = getResources().getDisplayMetrics().heightPixels / 3 - actionBarSize;
        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            ret -= Utils.getNavigationBarHeight(getActivity());
        return ret;
    }

    protected void toast(final String toast) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLayouts();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) handler.removeCallbacksAndMessages(null);
    }

    public void readFromWebpage(final String web, final WebpageReaderTask.WebpageListener webpageListener) {
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
                                loading = false;
                                if (raw == null || raw.isEmpty()) {
                                    setTextTitle(getString(R.string.no_connection));
                                    return;
                                }

                                webpageListener.onWebpageResult(raw, html);
                            }
                        }).start();
                    }
                }).execute(web);

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

}
