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

package com.grarak.cardview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.grarak.rom.switcher.R;

/**
 * Created by willi on 23.12.14.
 */
public class BaseCardView extends CardView {

    private static final int DEFAULT_LAYOUT = R.layout.inner_cardview;

    private HeaderCardView headerCardView;
    private LinearLayout headerLayout;

    private ImageView imageView;
    private Drawable d;

    private TextView innerView;
    private String mTitle;

    private LinearLayout customLayout;
    private View customView;

    public BaseCardView(Context context) {
        this(context, DEFAULT_LAYOUT);
    }

    public BaseCardView(Context context, int layout) {
        this(context, null, layout);
    }

    public BaseCardView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, DEFAULT_LAYOUT);
    }

    public BaseCardView(Context context, AttributeSet attributeSet, int layout) {
        super(context, attributeSet);

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(20, 10, 20, 10);
        setLayoutParams(layoutParams);
        setRadius(5);

        TypedArray ta = getContext().obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground});
        Drawable d = ta.getDrawable(0);
        ta.recycle();
        setForeground(d);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.base_cardview, null, false);
        addView(view);

        imageView = (ImageView) view.findViewById(R.id.image_icon);
        headerLayout = (LinearLayout) view.findViewById(R.id.header_layout);

        setUpImageView();
        setUpHeader();

        LinearLayout innerLayout = (LinearLayout) view.findViewById(R.id.inner_layout);
        customLayout = (LinearLayout) view.findViewById(R.id.custom_layout);

        View layoutView = LayoutInflater.from(getContext()).inflate(layout, null, false);
        if (layout == DEFAULT_LAYOUT) {
            innerView = (TextView) layoutView.findViewById(R.id.inner_view);
            if (mTitle != null) innerView.setText(mTitle);
        } else setUpInnerLayout(layoutView);

        innerLayout.addView(layoutView);
    }

    protected void setUpInnerLayout(View view) {
    }

    public final void setIcon(Drawable d) {
        this.d = d;
        setUpImageView();
    }

    public final void setText(String mTitle) {
        this.mTitle = mTitle;
        if (innerView != null) innerView.setText(mTitle);
    }

    public void setView(View view) {
        customView = view;
        setUpCustomLayout();
    }

    public void addHeader(HeaderCardView headerCardView) {
        this.headerCardView = headerCardView;
        setUpHeader();
    }

    private void setUpImageView() {
        if (imageView != null && d != null) {
            imageView.setImageDrawable(d);
            imageView.setVisibility(VISIBLE);
        }
    }

    private void setUpHeader() {
        if (headerCardView != null && headerLayout != null) {
            headerLayout.removeAllViews();
            headerLayout.addView(headerCardView.getView());
            headerLayout.setVisibility(VISIBLE);
        }
    }

    public void removeHeader() {
        headerCardView = null;
        if (headerLayout != null) {
            headerLayout.removeAllViews();
            headerLayout.setVisibility(GONE);
        }
    }

    private void setUpCustomLayout() {
        if (customLayout != null && customView != null) {
            innerView.setVisibility(GONE);
            customLayout.setVisibility(VISIBLE);
            customLayout.removeAllViews();
            customLayout.addView(customView);
        }
    }

}