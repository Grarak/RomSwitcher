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

package com.grarak.rom.switcher.utils.task;

import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import com.grarak.rom.switcher.utils.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by willi on 16.01.15.
 */
public class WebpageReaderTask extends AsyncTask<String, Void, String> implements Constants {

    private final WebpageListener webpageListener;

    public WebpageReaderTask(WebpageListener webpageListener) {
        this.webpageListener = webpageListener;
    }

    @Override
    protected String doInBackground(String... params) {
        InputStream is = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        try {
            String line;
            URL url = new URL(params[0]);
            is = url.openStream();
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null)
                sb.append(line).append("\n");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "Failed to read url: " + params[0]);
        } finally {
            try {
                if (is != null) is.close();
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        webpageListener.onWebpageResult(s, Html.fromHtml(s).toString());
    }

    public interface WebpageListener {
        public void onWebpageResult(String raw, String html);
    }

}
