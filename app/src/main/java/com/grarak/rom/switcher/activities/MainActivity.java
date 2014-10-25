package com.grarak.rom.switcher.activities;

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
 * Created by grarak's kitten (meow) on 30.03.14.
 */

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.grarak.rom.switcher.R;
import com.grarak.rom.switcher.utils.Constants;
import com.grarak.rom.switcher.utils.Utils;

import java.io.File;

public class MainActivity extends Activity implements Constants {

    private static ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Code has been approved by AndreiLux!
         * Let's meow together.
         */

        // Create romswitcher path
        if (!new Utils().existfile(romswitcherPath)) new File(romswitcherPath).mkdir();

        // Animation
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);

        // Check if the user opens the app the first time
        if (new Utils().getBoolean("firstuse", true, getApplicationContext()) && new Utils().isDefaultRom()) {

            // Let's begin the tutorial
            setContentView(R.layout.activity_main);

            SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.setOffscreenPageLimit(3);

            // Viewpager Animation
            mViewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
                @Override
                public void transformPage(View page, float position) {
                    float normalizedposition = Math.abs(Math.abs(position) - 1);
                    page.setScaleX(normalizedposition / 2 + 0.5f);
                    page.setScaleY(normalizedposition / 2 + 0.5f);
                }
            });

            mViewPager.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

        } else {
            startActivity(new Intent(getApplicationContext(), RomSwitcherActivity.class));
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return WelcomeFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }

    public static class WelcomeFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public static WelcomeFragment newInstance(int sectionNumber) {
            WelcomeFragment fragment = new WelcomeFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Bundle bundle = getArguments();
            if (bundle == null) throw new AssertionError();
            final int position = bundle.getInt(ARG_SECTION_NUMBER, 0);

            // Each fragment has a different layout
            int layout = R.layout.fragment_welcome;
            switch (position) {
                case 1:
                    layout = R.layout.fragment_welcome;
                    break;
                case 2:
                    layout = R.layout.fragments_tutorial;
                    break;
                case 3:
                    layout = R.layout.fragment_finish;
                    break;
            }

            View rootView = inflater.inflate(layout, container, false);
            if (rootView != null) {
                Button next = (Button) rootView.findViewById(R.id.button_next);
                if (next != null)
                    next.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mViewPager.setCurrentItem(position);
                        }
                    });
                Button back = (Button) rootView.findViewById(R.id.button_back);
                if (back != null)
                    back.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mViewPager.setCurrentItem(position - 2);
                        }
                    });
            }

            if (position == 3) {
                assert rootView != null;
                Button useRomSwitcher = (Button) rootView.findViewById(R.id.useromswitcher);
                useRomSwitcher.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Utils().saveBoolean("firstuse", false, getActivity());
                        final int enter_anim = android.R.anim.fade_in;
                        final int exit_anim = android.R.anim.fade_out;
                        getActivity().overridePendingTransition(enter_anim, exit_anim);
                        getActivity().finish();
                        getActivity().overridePendingTransition(enter_anim, exit_anim);
                        getActivity().startActivity(new Intent(getActivity(), RomSwitcherActivity.class));
                    }
                });
            }

            return rootView;
        }

    }

}
