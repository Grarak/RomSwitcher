package com.grarak.rom.switcher.utils.json;

import android.util.Log;

import com.grarak.rom.switcher.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by willi on 16.01.15.
 */
public class DownloadsJson implements Constants {

    private JSONArray mDownloads;

    public DownloadsJson(String json) {
        try {
            mDownloads = new JSONObject(json).getJSONArray("downloads");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to read downloads JSON");
        }
    }

    public String getNote(int position) {
        return getString(position, "note");
    }

    public String getChangelog(int position) {
        return getString(position, "changelog");
    }

    public String getSize(int position) {
        return getString(position, "size");
    }

    public String getMd5sum(int position) {
        return getString(position, "md5sum");
    }

    public String getVersion(int position) {
        return getString(position, "version");
    }

    public String getLink(int position) {
        return getString(position, "link");
    }

    public int getLength() {
        return mDownloads.length();
    }

    private String getString(int position, String name) {
        try {
            return mDownloads.getJSONObject(position).getString(name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void refresh(String json) {
        try {
            mDownloads = new JSONObject(json).getJSONArray("downloads");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to read downloads JSON");
        }
    }

}
