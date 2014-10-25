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
 * Created by grarak's kitten (meow) on 10.06.14.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.grarak.rom.switcher.R;
import com.grarak.rom.switcher.utils.Constants;
import com.grarak.rom.switcher.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

public class AppsharingActivity extends Activity implements Constants {

    private final Utils utils = new Utils();
    private ListView list;
    private TextView nosharingsText;
    private final ArrayList<String> sharingList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);

        list = new ListView(getApplicationContext());
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);
                AlertDialog.Builder items = new AlertDialog.Builder(AppsharingActivity.this);
                items.setItems(new String[]{getString(R.string.delete), getString(R.string.edit)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int choice) {
                        switch (choice) {
                            case 0:
                                delete(i);
                                break;
                            case 1:
                                addSharing(true, i, Integer.parseInt(sharingList.get(i).split(" ")[0]), Integer.parseInt(sharingList.get(i).split(" ")[1]));
                                break;
                        }
                        setAdapter();
                    }
                }).show();
                return false;
            }
        });
        nosharingsText = new TextView(getApplicationContext());
        nosharingsText.setTextColor(getResources().getColor(android.R.color.black));
        nosharingsText.setGravity(Gravity.CENTER);
        nosharingsText.setText(getString(R.string.no_sharings));
        layout.addView(list);
        layout.addView(nosharingsText);

        setAdapter();
    }

    private void setAdapter() {
        final ArrayList<String> sharingListName = new ArrayList<String>();
        sharingList.clear();
        String sharings = null;
        try {
            sharings = utils.readFile(appsharingFile);
        } catch (IOException e) {
            Log.e(TAG, "unable to read " + appsharingFile);
        }
        if (sharings == null) sharings = "";
        if (sharings.isEmpty()) {
            list.setVisibility(View.GONE);
            nosharingsText.setVisibility(View.VISIBLE);
        } else {
            nosharingsText.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);

            for (String sharing : sharings.split("\\r?\\n"))
                if (!sharing.isEmpty()) {
                    sharingList.add(sharing);
                    sharingListName.add(getString(R.string.sharing_between, sharing.split(" ")[0], sharing.split(" ")[1]));
                }

            list.setAdapter(new CustomArrayAdapter(getApplicationContext(), sharingListName));
        }
    }

    private class CustomArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final ArrayList<String> values;

        public CustomArrayAdapter(Context context, ArrayList<String> values) {
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
            textView.setText(values.get(position));
            rowView.findViewById(R.id.logo).setVisibility(View.GONE);

            return rowView;
        }

    }

    private void addSharing(final boolean editmode, final int i, int choice, int choice2) {
        final ArrayList<String> roms = new ArrayList<String>();
        for (int x = 1; x <= utils.getRomNumber(); x++)
            roms.add(getString(R.string.rom, x));

        LinearLayout layout = new LinearLayout(this);
        layout.setGravity(Gravity.CENTER);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, roms);

        final Spinner romslist = new Spinner(this);
        romslist.setAdapter(adapter);

        TextView betweenText = new TextView(this);
        betweenText.setText(getString(R.string.between));

        final Spinner romslist2 = new Spinner(this);
        romslist2.setAdapter(adapter);

        layout.addView(romslist);
        layout.addView(betweenText);
        layout.addView(romslist2);

        if (editmode) {
            romslist.setSelection(choice - 1);
            romslist2.setSelection(choice2 - 1);
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.sharing)).setView(layout).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int choice) {
                if (editmode)
                    edit(i, String.valueOf(romslist.getSelectedItemPosition() + 1), String.valueOf(romslist2.getSelectedItemPosition() + 1));
                else
                    writeAppsharing(String.valueOf(romslist.getSelectedItemPosition() + 1), String.valueOf(romslist2.getSelectedItemPosition() + 1));
                setAdapter();
            }
        }).show();
    }

    private void delete(int i) {
        sharingList.remove(i);
        refresh();
    }

    private void edit(int i, String rom, String rom2) {
        sharingList.set(i, rom + " " + rom2);
        refresh();
    }

    private void refresh() {
        utils.deleteFile(appsharingFile);
        for (String sharing : sharingList)
            writeAppsharing(sharing.split(" ")[0], sharing.split(" ")[1]);
    }

    private void writeAppsharing(String rom, String rom2) {
        utils.writeFile(appsharingFile, utils.existfile(appsharingFile) ? "\n" + rom + " " + rom2 : rom + " " + rom2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appsharing, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_add:
                addSharing(false, 0, 0, 0);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
