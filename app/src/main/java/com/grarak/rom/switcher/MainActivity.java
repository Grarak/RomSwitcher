package com.grarak.rom.switcher;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.LightingColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.grarak.rom.switcher.elements.ListAdapter;
import com.grarak.rom.switcher.elements.ScrimInsetsFrameLayout;
import com.grarak.rom.switcher.fragments.InformationFragment;
import com.grarak.rom.switcher.fragments.InstallationFragment;
import com.grarak.rom.switcher.fragments.ROMFragment;
import com.grarak.rom.switcher.fragments.RecoveryFragment;
import com.grarak.rom.switcher.utils.Constants;
import com.grarak.rom.switcher.utils.Utils;
import com.grarak.rom.switcher.utils.root.RootUtils;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements Constants {

    private boolean hasRoot;
    private boolean hasBusybox;

    private String mTitle;

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ScrimInsetsFrameLayout mScrimInsetsFrameLayout;
    private ListView mDrawerList;

    private List<ListAdapter.ListItem> mList = new ArrayList<>();

    private int cur_position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = new ProgressBar(this);
        progressBar.getIndeterminateDrawable().setColorFilter(new LightingColorFilter(0xFF000000,
                getResources().getColor(android.R.color.white)));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(progressBar, new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.END));

        new Task().execute();
    }

    private void setView() {
        mScrimInsetsFrameLayout = (ScrimInsetsFrameLayout) findViewById(R.id.scrimInsetsFrameLayout);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.color_primary_dark));
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList = (ListView) findViewById(R.id.listview_drawer);
    }

    private void setList() {
        boolean supported = Utils.getDevicesJson(this).isSupported();

        mList.clear();
        mList.add(new ListAdapter.MainHeader());
        mList.add(new ListAdapter.Item(getString(R.string.information), new InformationFragment()));

        if (supported) {
            mList.add(new ListAdapter.Item(getString(R.string.installation), new InstallationFragment()));

            if (Utils.isInstalled()) {
                mList.add(new ListAdapter.Item(getString(R.string.recovery), new RecoveryFragment()));
                mList.add(new ListAdapter.Header(getString(R.string.roms)));
                mList.add(new ListAdapter.Item(getString(R.string.internal_storage),
                        ROMFragment.newInstance(ROMFragment.STORAGE.INTERNAL)));
                if (Utils.getDevicesJson(this).hasExternalStorage())
                    mList.add(new ListAdapter.Item(getString(R.string.external_storage),
                            ROMFragment.newInstance(ROMFragment.STORAGE.EXTERNAL)));
            }
        }
    }

    private void setInterface() {
        mScrimInsetsFrameLayout.setLayoutParams(getDrawerParams());
        mDrawerList.setAdapter(new ListAdapter.Adapter(this, mList));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, mDrawerLayout, toolbar, 0, 0) {
            @Override
            public void onDrawerClosed(View drawerView) {
                getSupportActionBar().setTitle(mTitle);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(getString(R.string.app_name));
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mDrawerToggle != null) mDrawerToggle.syncState();
            }
        });

        selectItem(1);
    }

    private class Task extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setView();
        }

        @Override
        protected String doInBackground(Void... params) {
            if (RootUtils.rooted()) hasRoot = RootUtils.rootAccess();
            if (hasRoot) hasBusybox = RootUtils.busyboxInstalled();

            if (hasRoot && hasBusybox) {
                RootUtils.su = new RootUtils.SU();
                setList();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (!hasRoot || !hasBusybox) {
                Intent i = new Intent(MainActivity.this, TextActivity.class);
                Bundle args = new Bundle();
                args.putString(TextActivity.ARG_TEXT, !hasRoot ? getString(R.string.no_root)
                        : getString(R.string.no_busybox));
                Log.d(TAG, !hasRoot ? getString(R.string.no_root) : getString(R.string.no_busybox));
                i.putExtras(args);
                startActivity(i);

                cancel(true);
                finish();
                return;
            }

            setInterface();

            try {
                ((ViewGroup) progressBar.getParent()).removeView(progressBar);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        }

    }

    private void selectItem(int position) {
        Fragment fragment = mList.get(position).getFragment();

        if (fragment == null || cur_position == position) {
            mDrawerList.setItemChecked(cur_position, true);
            return;
        }

        mDrawerLayout.closeDrawer(mScrimInsetsFrameLayout);

        cur_position = position;

        try {
            Log.i(TAG, "Open postion " + position + ": " + mList.get(position).getTitle());
            getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle(mList.get(position).getTitle());
        mDrawerList.setItemChecked(position, true);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title.toString();
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mScrimInsetsFrameLayout)) super.onBackPressed();
        else mDrawerLayout.openDrawer(mScrimInsetsFrameLayout);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mScrimInsetsFrameLayout != null)
            mScrimInsetsFrameLayout.setLayoutParams(getDrawerParams());
        if (mDrawerToggle != null) mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (RootUtils.su != null) RootUtils.su.close();
    }

    private DrawerLayout.LayoutParams getDrawerParams() {
        boolean tablet = Utils.isTablet(this);

        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mScrimInsetsFrameLayout.getLayoutParams();
        int width = getResources().getDisplayMetrics().widthPixels;

        TypedArray ta = obtainStyledAttributes(new int[]{R.attr.actionBarSize});
        int actionBarSize = ta.getDimensionPixelSize(0, 100);
        if (Utils.getScreenOrientation(this) == Configuration.ORIENTATION_LANDSCAPE) {
            params.width = width / 2;
            if (tablet) params.width -= actionBarSize + 30;
        } else {
            params.width = tablet ? width / 2 : width - actionBarSize;
        }

        return params;
    }

}
