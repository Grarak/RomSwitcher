package com.grarak.rom.switcher.utils.json;

import android.util.Log;

import com.grarak.rom.switcher.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by willi on 18.01.15.
 */
public class FAQJson implements Constants {

    private JSONArray mFaqs;

    public FAQJson(String json) {
        try {
            mFaqs = new JSONObject(json).getJSONArray("faqs");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to read faq JSON");
        }
    }

    public String getAnswer(int position) {
        return getString(position, "answer");
    }

    public String getQuestion(int position) {
        return getString(position, "question");
    }

    public int getLength() {
        return mFaqs.length();
    }

    private String getString(int position, String name) {
        try {
            return mFaqs.getJSONObject(position).getString(name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void refresh(String json) {
        try {
            mFaqs = new JSONObject(json).getJSONArray("faqs");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to refresh faq JSON");
        }
    }

}
