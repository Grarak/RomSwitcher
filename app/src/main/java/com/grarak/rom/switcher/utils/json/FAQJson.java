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
