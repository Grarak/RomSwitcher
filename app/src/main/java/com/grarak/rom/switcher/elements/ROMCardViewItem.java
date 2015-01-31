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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.internal.widget.TintEditText;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.grarak.cardview.BaseCardView;
import com.grarak.rom.switcher.R;
import com.grarak.rom.switcher.utils.Utils;
import com.grarak.rom.switcher.utils.root.RootUtils;

import java.util.Locale;

/**
 * Created by willi on 17.01.15.
 */
public class ROMCardViewItem extends BaseCardView {

    private TextView titleView;
    private ImageButton editButton;
    private ImageButton rebootButton;
    private ImageButton buildpropButton;

    private String name;
    private String size;

    private OnEditROMListener onEditROMListener;

    public ROMCardViewItem(Context context, final String path, boolean internal) {
        super(context, R.layout.rom_cardview);

        setUpTitle();
        if (Utils.isDefaultROM()) {
            editButton.setVisibility(VISIBLE);
            editButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    LinearLayoutCompat layoutCompat = new LinearLayoutCompat(getContext());
                    layoutCompat.setPadding(30, 10, 30, 10);

                    final TintEditText editText = new TintEditText(getContext());
                    layoutCompat.addView(editText);
                    editText.setGravity(Gravity.CENTER);
                    editText.setText(name);
                    editText.setTextColor(getContext().getResources().getColor(android.R.color.black));
                    editText.setMaxLines(1);

                    LinearLayoutCompat.LayoutParams params = (LinearLayoutCompat.LayoutParams) editText.getLayoutParams();
                    params.width = LinearLayoutCompat.LayoutParams.MATCH_PARENT;
                    editText.setLayoutParams(params);

                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog.setTitle(getContext().getString(R.string.rename)).setView(layoutCompat)
                            .setNegativeButton(getContext().getString(R.string.cancel),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                            .setPositiveButton(getContext().getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String name = editText.getText().toString().trim();

                                            if (ROMCardViewItem.this.name.equals(name)) return;

                                            if (name.isEmpty()) {
                                                Toast.makeText(getContext(), getContext().getString(R.string.name_empty),
                                                        Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            String pattern = "^[a-zA-Z0-9]*$";
                                            if (!name.matches(pattern)) {
                                                Toast.makeText(getContext(), getContext().getString(R.string.not_allowed_sign),
                                                        Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            if (name.toLowerCase(Locale.getDefault()).equals("default")) {
                                                Toast.makeText(getContext(), getContext().getString(R.string.default_not_allowed),
                                                        Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            if (name.length() < 3) {
                                                Toast.makeText(getContext(), getContext().getString(R.string.name_too_short),
                                                        Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            if (RootUtils.fileExist(path + "/" + name)) {
                                                Toast.makeText(getContext(), getContext().getString(R.string.name_duplicated),
                                                        Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            RootUtils.moveFile(path + "/" + ROMCardViewItem.this.name, path + "/" + name);
                                            if (onEditROMListener != null)
                                                onEditROMListener.onChange();
                                        }
                                    }).show();
                }
            });
        }

        rebootButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.confirmDialog(getContext().getString(R.string.reboot),
                        getContext().getString(R.string.reboot_to, name), new Utils.OnConfirmDialogListener() {
                            @Override
                            public void onDismiss() {
                            }

                            @Override
                            public void onConfirm() {
                                Utils.setROM(path, name);
                                Utils.reboot();
                            }
                        }, getContext());
            }
        });

        if (internal) {
            buildpropButton.setVisibility(VISIBLE);
            buildpropButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (RootUtils.fileExist(path + "/" + name + "/system/build.prop")) {
                        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_textview,
                                (ViewGroup) getParent(), false);
                        ((TextView) dialogView.findViewById(R.id.title_text)).setText(getContext().getString(R.string.build_prop));
                        ((TextView) dialogView.findViewById(R.id.text)).setText(RootUtils.readFile(path + "/"
                                + name + "/system/build.prop"));

                        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                        dialog.setView(dialogView).show();
                    } else
                        Toast.makeText(getContext(), getContext().getString(R.string.no_build_prop), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void setUpInnerLayout(View view) {
        super.setUpInnerLayout(view);

        titleView = (TextView) view.findViewById(R.id.title_view);
        editButton = (ImageButton) view.findViewById(R.id.edit_view);
        rebootButton = (ImageButton) view.findViewById(R.id.reboot_view);
        buildpropButton = (ImageButton) view.findViewById(R.id.buildprop_view);
    }

    public void setName(String name) {
        this.name = name;
        setUpTitle();
    }

    public void setSize(String size) {
        this.size = size;
        setUpTitle();
    }

    public void setOnEditROMListener(OnEditROMListener onEditROMListener) {
        this.onEditROMListener = onEditROMListener;
    }

    private void setUpTitle() {
        if (titleView != null && name != null) {
            String text = name;
            if (size != null) text += " (" + size + getContext().getString(R.string.mb) + ")";
            titleView.setText(text);
        }
    }

    public interface OnEditROMListener {
        public void onChange();
    }

    public static class DROMCardView implements RecyclerViewAdapter.ViewInterface {

        private final String path;
        private final boolean internal;

        private ROMCardViewItem romCardViewItem;

        private String name;
        private String size;

        private OnEditROMListener onEditROMListener;

        public DROMCardView(String path, boolean internal) {
            this.path = path;
            this.internal = internal;
        }

        @Override
        public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder) {
            romCardViewItem = (ROMCardViewItem) holder.itemView;

            if (name != null) romCardViewItem.setName(name);
            if (size != null) romCardViewItem.setSize(size);
            if (onEditROMListener != null) romCardViewItem.setOnEditROMListener(onEditROMListener);
        }

        @Override
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent) {
            return new RecyclerViewAdapter.ViewHolder(new ROMCardViewItem(parent.getContext(), path, internal));
        }

        public void setName(String name) {
            this.name = name;
            if (romCardViewItem != null) romCardViewItem.setName(name);
        }

        public void setSize(String size) {
            this.size = size;
            if (romCardViewItem != null) romCardViewItem.setSize(size);
        }

        public void setOnEditROMListener(OnEditROMListener onEditROMListener) {
            this.onEditROMListener = onEditROMListener;
            if (romCardViewItem != null) romCardViewItem.setOnEditROMListener(onEditROMListener);
        }

    }

}
