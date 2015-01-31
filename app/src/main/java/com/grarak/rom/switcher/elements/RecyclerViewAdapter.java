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

package com.grarak.rom.switcher.elements;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by willi on 13.01.15.
 */
public class RecyclerViewAdapter {

    public interface ViewInterface {
        public void onBindViewHolder(ViewHolder holder);

        public ViewHolder onCreateViewHolder(ViewGroup parent);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View view) {
            super(view);
        }

    }

    public static class Adapter extends RecyclerView.Adapter<ViewHolder> {

        private final List<ViewInterface> list;

        public Adapter(List<ViewInterface> list) {
            this.list = list;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            list.get(position).onBindViewHolder(holder);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return list.get(viewType).onCreateViewHolder(parent);
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

    }

}
