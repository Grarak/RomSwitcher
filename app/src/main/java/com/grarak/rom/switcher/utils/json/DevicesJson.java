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

package com.grarak.rom.switcher.utils.json;

import android.util.Log;

import com.grarak.rom.switcher.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 12.01.15.
 */
public class DevicesJson implements Constants {

    private boolean supported;
    private JSONObject deviceObject;

    public DevicesJson(String json) {
        try {
            JSONArray devices = new JSONObject(json).getJSONArray("devices");

            for (int i = 0; i < devices.length(); i++) {
                JSONObject deviceObject = devices.getJSONObject(i);
                JSONArray names = devices.getJSONObject(i).getJSONArray("names");

                for (int x = 0; x < names.length(); x++)
                    if (names.getString(x).equals(DEVICE_MODEL)) {
                        this.deviceObject = deviceObject;
                        supported = true;
                        break;
                    }
            }

        } catch (JSONException e) {
            Log.e(TAG, "Failed to read devices JSON");
        }
    }

    public boolean isSupported() {
        return supported;
    }

    public boolean rebootRecovery() {
        return getBoolean("rebootrecovery");
    }

    public boolean installRecovery() {
        return getBoolean("installrecovery");
    }

    public String getBootPartition() {
        return getString("boot");
    }

    public String getRecoveryPartition() {
        return getString("recovery");
    }

    public boolean hasExternalStorage() {
        return deviceObject.has("externalstorages");
    }

    public List<String> getExternalStorages() {
        List<String> list = new ArrayList<>();
        try {
            JSONArray extStorages = deviceObject.getJSONArray("externalstorages");

            for (int i = 0; i < extStorages.length(); i++)
                list.add(extStorages.getString(i));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    public String getConfig() {
        return getString("config");
    }

    public boolean hasFaq() {
        return getFaq() != null;
    }

    public String getFaq() {
        return getString("faq");
    }

    public boolean hasDonation() {
        return getDonation() != null;
    }

    public String getDonation() {
        return getString("donate");
    }

    private boolean getBoolean(String name) {
        if (deviceObject != null)
            try {
                return deviceObject.getBoolean(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        return false;
    }

    private String getString(String name) {
        if (deviceObject != null)
            try {
                return deviceObject.getString(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        return null;
    }

}
