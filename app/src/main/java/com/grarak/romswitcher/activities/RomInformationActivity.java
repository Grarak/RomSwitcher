package com.grarak.romswitcher.activities;

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
 * Created by grarak's kitten (meow) on 14.04.14.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.grarak.romswitcher.R;
import com.grarak.romswitcher.utils.Constants;
import com.grarak.romswitcher.utils.RootUtils;
import com.grarak.romswitcher.utils.Utils;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RomInformationActivity extends Activity implements Constants {

    private final String ARG_SECTION_NUMBER = "section_number";
    private int currentRom = 0;

    private Utils utils = new Utils();
    private RootUtils root = new RootUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_information);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        currentRom = getIntent().getExtras().getInt(ARG_SECTION_NUMBER);

        root.run("rm -f " + romswitcherPath + "/build.prop");
        root.run("cat " + utils.dataPath + "/media/." + String.valueOf(currentRom) + "rom/system/build.prop > " + romswitcherPath + "/build.prop");

        ((TextView) findViewById(R.id.rom_size)).setText(getString(R.string.rom_size, currentRom));
        setSize();
    }

    private void setSize() {
        Command command = new Command(0, "du -ms " + utils.dataPath + "/media/." + String.valueOf(currentRom) + "rom") {
            @Override
            public void commandCompleted(int arg0, int arg1) {
            }

            @Override
            public void commandOutput(int arg0, String arg1) {
                ((TextView) findViewById(R.id.size)).setText(arg1.replace(utils.dataPath + "/media/." + String.valueOf(currentRom) + "rom", "") + getString(R.string.mb));
            }

            @Override
            public void commandTerminated(int arg0, String arg1) {
            }
        };
        try {
            RootTools.getShell(true).add(command);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (RootDeniedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            ((LinearLayout) findViewById(R.id.buildproplayout)).removeAllViews();
            Thread.sleep(1000);
            utils.showProgressDialog("", false);
            String props = utils.readFile(romswitcherPath + "/build.prop");
            if (!props.isEmpty() && utils.existfile(romswitcherPath + "/build.prop")) {

                TextView buildproptitle = new TextView(this);
                buildproptitle.setText(getString(R.string.build_prop, currentRom));
                buildproptitle.setBackgroundColor(getResources().getColor(android.R.color.white));
                buildproptitle.setGravity(Gravity.CENTER);
                buildproptitle.setTypeface(null, Typeface.BOLD);

                ((LinearLayout) findViewById(R.id.buildproplayout)).addView(buildproptitle);

                TextView dummy = new TextView(this);
                ((LinearLayout) findViewById(R.id.buildproplayout)).addView(dummy);

                String[] proplist = props.split("\\r?\\n");
                String[] requestprops = new String[]{"ro.build.id", "ro.build.display", "ro.build.version.sdk",
                        "ro.build.version.release", "ro.build.date", "ro.product.model", "ro.product.device",
                        "ro.build.PDA", "ro.pac.version", "ro.cm.version", "ro.pa.version"};

                for (String prop : proplist)
                    for (String requestprop : requestprops)
                        if (prop.startsWith(requestprop)) {
                            TextView text = new TextView(this);
                            text.setText(prop);
                            text.setTextColor(getResources().getColor(android.R.color.white));
                            ((LinearLayout) findViewById(R.id.buildproplayout)).addView(text);
                        }

            } else error();
        } catch (IOException e) {
            Log.e(TAG, "unable to read build.prop of ROM " + currentRom);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void error() {
        AlertDialog.Builder error = new AlertDialog.Builder(this);
        error.setMessage(getString(R.string.rom_error, currentRom))
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                })
                .setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return true;
    }
}
