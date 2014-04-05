package com.grarak.romswitcher.utils;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by grarak's kitten (meow) on 31.03.14.
 */

public class GetConntection extends AsyncTask<String, Void, String> {

    public static String htmlstring = "";

    @Override
    protected String doInBackground(String... urls) {
        HttpResponse response;
        HttpGet httpGet;
        HttpClient mHttpClient;
        String s = "";
        try {
            mHttpClient = new DefaultHttpClient();
            httpGet = new HttpGet(urls[0]);

            response = mHttpClient.execute(httpGet);
            s = EntityUtils.toString(response.getEntity(), "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    @Override
    protected void onPostExecute(String result) {
        htmlstring = result;
    }
}
