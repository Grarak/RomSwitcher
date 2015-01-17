package com.grarak.rom.switcher.elements;

import android.app.Fragment;
import android.app.WallpaperManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.grarak.rom.switcher.R;

import java.util.List;

/**
 * Created by willi on 10.01.15.
 */
public class ListAdapter {

    public interface ListItem {

        public String getTitle();

        public Fragment getFragment();

        public View getView(LayoutInflater inflater, ViewGroup parent);

    }

    public static class Adapter extends ArrayAdapter<ListItem> {

        public Adapter(Context context, List<ListItem> list) {
            super(context, 0, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).getView(LayoutInflater.from(getContext()), parent);
        }

    }

    public static class Item implements ListItem {

        private final String title;
        private final Fragment fragment;

        public Item(String title, Fragment fragment) {
            this.title = title;
            this.fragment = fragment;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public Fragment getFragment() {
            return fragment;
        }

        @Override
        public View getView(LayoutInflater inflater, ViewGroup parent) {
            TextView text = (TextView) inflater.inflate(R.layout.list_item, parent, false);
            text.setText(title);
            return text;
        }

    }

    public static class Header implements ListItem {

        private final String title;

        public Header(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public Fragment getFragment() {
            return null;
        }

        @Override
        public View getView(LayoutInflater inflater, ViewGroup parent) {
            View view = inflater.inflate(R.layout.list_header, parent, false);
            TextView text = (TextView) view.findViewById(R.id.text);
            text.setText(title);
            return view;
        }

    }

    public static class MainHeader implements ListItem {

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public Fragment getFragment() {
            return null;
        }

        @Override
        public View getView(final LayoutInflater inflater, ViewGroup parent) {
            View view = inflater.inflate(R.layout.header_main, parent, false);
            ImageView bg = (ImageView) view.findViewById(R.id.bg_image);
            bg.setImageDrawable(WallpaperManager.getInstance(inflater.getContext()).getDrawable());
            return view;
        }

    }

}
