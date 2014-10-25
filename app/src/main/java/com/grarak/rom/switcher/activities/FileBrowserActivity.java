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
 * Created by grarak's kitten (meow) on 30.05.14.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.grarak.rom.switcher.R;
import com.grarak.rom.switcher.utils.Constants;
import com.grarak.rom.switcher.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class FileBrowserActivity extends Activity implements Constants {

    private final String ARG_SECTION_NUMBER = "section_number";
    private final String ARG_FILTER = "filter";
    private final String ARG_RESULT = "result";
    private int currentRom = 0;
    private String filter = "";
    private boolean showAll = false;

    private ListView filesList;
    private final ArrayList<File> list = new ArrayList<File>();
    private String currentPath = externalStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);

        currentRom = getIntent().getExtras().getInt(ARG_SECTION_NUMBER);
        filter = getIntent().getExtras().getString(ARG_FILTER);

        LinearLayout layout2 = new LinearLayout(getApplicationContext());
        layout.addView(layout2);

        LayoutParams lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        if (!filter.isEmpty()) {
            String[] filters = new String[]{
                    filter.toUpperCase() + " " + getString(R.string.files), getString(R.string.show_all)
            };

            ArrayAdapter<String> adapterfilter = new ArrayAdapter<String>(
                    getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, filters);

            Spinner filterlist = new Spinner(getApplicationContext());
            filterlist.setAdapter(adapterfilter);
            filterlist.setLayoutParams(lp);
            filterlist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    showAll = i == 1;
                    setList(currentPath.toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            layout2.addView(filterlist);
        }

        Button backButton = new Button(getApplicationContext());
        backButton.setText(getString(R.string.back));
        backButton.setLayoutParams(lp);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setList(currentPath + "/..");
            }
        });
        layout2.addView(backButton);

        filesList = new ListView(getApplicationContext());
        filesList.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        filesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final File file = list.get(i);
                if (file.isDirectory())
                    setList(file.getAbsolutePath());
                else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(FileBrowserActivity.this);
                    alert.setMessage(getString(R.string.make_sure, file.getName())).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            /*
                             * This is an Activity which should return something,
                             * thus we set result as the selected file.
                             */
                            Intent intent = new Intent();
                            intent.putExtra(ARG_RESULT, file.getAbsolutePath());
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
                    }).show();
                }
            }
        });
        layout.addView(filesList);

        setList(currentPath);
    }

    private void setList(String path) {
        currentPath = path;

        File[] values = new File(path).listFiles();

        // Sort files alphabetically
        if (values != null) Arrays.sort(values);

        /*
         * Split directories and files
         * show directories first then files
         */
        ArrayList<File> directories = new ArrayList<File>();
        ArrayList<File> files = new ArrayList<File>();
        if (values != null)
            for (File file : values)
                if (file.isDirectory())
                    directories.add(file);
                else
                    files.add(file);

        list.clear();
        if (values != null) {
            for (File directory : directories)
                list.add(directory);
            for (File file : files)
                if (!filter.isEmpty())
                    if ((file.getName().endsWith("." + filter) || file.getName().endsWith("." + filter.toUpperCase())) || showAll)
                        list.add(file);
        }

        if (list.size() == 0)
            new Utils().toast(getString(R.string.directory_empty), getApplicationContext());

        filesList.setAdapter(new CustomArrayAdapter(getApplicationContext(), list));

    }

    private class CustomArrayAdapter extends ArrayAdapter<File> {
        private final Context context;
        private final ArrayList<File> values;

        public CustomArrayAdapter(Context context, ArrayList<File> values) {
            super(context, R.layout.simple_list_item_1, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.simple_list_item_1, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.label);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
            textView.setText(values.get(position).getName());

            imageView.setImageDrawable(getResources().getDrawable(values.get(position).isDirectory() ? R.drawable.ic_directory : R.drawable.ic_file));

            return rowView;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return true;
    }

}
