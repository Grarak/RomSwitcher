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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.grarak.cardview.BaseCardView;
import com.grarak.cardview.HeaderCardView;
import com.grarak.rom.switcher.R;
import com.grarak.rom.switcher.utils.Constants;
import com.grarak.rom.switcher.utils.Utils;
import com.grarak.rom.switcher.utils.task.DownloadTask;

/**
 * Created by willi on 16.01.15.
 */
public class DownloadCardViewItem extends BaseCardView implements Constants {

    private DownloadHeaderView downloadHeaderView;

    private TextView md5sumView;
    private TextView noteView;

    private String version;
    private String size;
    private String md5sum;
    private String note;
    private String download;
    private String changelog;

    private OnDownloadListener onDownloadListener;

    public DownloadCardViewItem(Context context) {
        super(context, R.layout.download_cardview);
    }

    @Override
    protected void setUpInnerLayout(final View view) {
        super.setUpInnerLayout(view);

        downloadHeaderView = new DownloadHeaderView(getContext());
        setUpTitle();

        md5sumView = (TextView) view.findViewById(R.id.md5sum_view);
        noteView = (TextView) view.findViewById(R.id.note_view);

        if (md5sum != null) md5sumView.setText(Html.fromHtml("<b>md5sum:</b> " + md5sum));
        setNoteView();

        downloadHeaderView.downloadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.confirmDialog(null, getContext().getString(R.string.install_confirm, version),
                        new Utils.OnConfirmDialogListener() {
                            @Override
                            public void onDismiss() {
                            }

                            @Override
                            public void onConfirm() {
                                new DownloadTask(ROMSWITCHER_DOWNLOAD_PATH, "download.img", new DownloadTask.DownloadListener() {
                                    @Override
                                    public void downloadFinish(DownloadTask.DownloadStatus status) {
                                        switch (status) {
                                            case SUCCESS:
                                                if (onDownloadListener != null)
                                                    onDownloadListener.onSuccess();
                                                break;
                                            case CANCELED:
                                                Toast.makeText(getContext(), getContext().getString(R.string.download_cancel),
                                                        Toast.LENGTH_SHORT).show();
                                                break;
                                            case FAILED:
                                                Toast.makeText(getContext(), getContext().getString(R.string.download_failed),
                                                        Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    }
                                }, getContext()).execute(download);
                            }
                        }, getContext());
            }
        });

        downloadHeaderView.changelogButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] changelogArray = changelog.split("\\r?\\n");
                StringBuilder message = new StringBuilder();

                for (String change : changelogArray)
                    if (change.startsWith("@")) {
                        if (!message.toString().isEmpty()) message.append("<br>");
                        message.append(change.replace("@", "- "));
                    } else {
                        if (!message.toString().isEmpty()) message.append("<br><br>");
                        message.append("<b>").append(change).append("</b>").append("<br>");
                    }

                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_textview,
                        (ViewGroup) getParent(), false);
                ((TextView) dialogView.findViewById(R.id.title_text)).setText(getContext().getString(R.string.changelog));
                ((TextView) dialogView.findViewById(R.id.text)).setText(Html.fromHtml(message.toString()));

                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setView(dialogView).show();
            }
        });
    }

    public void setVersion(String version) {
        this.version = version;
        setUpTitle();
    }

    public void setSize(String size) {
        this.size = size;
        setUpTitle();
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
        if (md5sumView != null) md5sumView.setText(Html.fromHtml("<b>md5sum:</b> " + md5sum));
    }

    public void setNote(String note) {
        this.note = note;
        setNoteView();
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
    }

    private void setUpTitle() {
        if (downloadHeaderView != null) {
            if (version == null) removeHeader();
            else addHeader(downloadHeaderView);
        }
        if (downloadHeaderView != null && version != null) {
            String text = getContext().getString(R.string.version) + " " + version;
            if (size != null) text += " (" + size + ")";
            downloadHeaderView.setText(text);
        }
    }

    private void setNoteView() {
        if (noteView != null)
            if (note != null) {
                noteView.setText(note);
                noteView.setVisibility(VISIBLE);
            } else noteView.setVisibility(GONE);
    }

    public interface OnDownloadListener {
        public void onSuccess();
    }

    private class DownloadHeaderView extends HeaderCardView {

        protected ImageButton downloadButton;
        protected ImageButton changelogButton;

        public DownloadHeaderView(Context context) {
            super(context, R.layout.header_downloadcardview);
        }

        @Override
        public void setUpHeaderLayout(View view) {
            super.setUpHeaderLayout(view);
            textView = (TextView) view.findViewById(R.id.header_view);
            downloadButton = (ImageButton) view.findViewById(R.id.download_view);
            changelogButton = (ImageButton) view.findViewById(R.id.changelog_view);
        }
    }

    public static class DDownloadCardView implements RecyclerViewAdapter.ViewInterface {

        private DownloadCardViewItem downloadCardViewItem;

        private String version;
        private String size;

        private String md5sum;
        private String note;
        private String download;
        private String changelog;

        private OnDownloadListener onDownloadListener;

        @Override
        public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder) {
            downloadCardViewItem = (DownloadCardViewItem) holder.itemView;

            if (version != null) downloadCardViewItem.setVersion(version);
            if (size != null) downloadCardViewItem.setSize(size);
            if (md5sum != null) downloadCardViewItem.setMd5sum(md5sum);
            if (note != null) downloadCardViewItem.setNote(note);
            if (download != null) downloadCardViewItem.setDownload(download);
            if (changelog != null) downloadCardViewItem.setChangelog(changelog);
            if (onDownloadListener != null)
                downloadCardViewItem.setOnDownloadListener(onDownloadListener);
        }

        @Override
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent) {
            return new RecyclerViewAdapter.ViewHolder(new DownloadCardViewItem(parent.getContext()));
        }

        public void setVersion(String version) {
            this.version = version;
            if (downloadCardViewItem != null) downloadCardViewItem.setVersion(version);
        }

        public void setSize(String size) {
            this.size = size;
            if (downloadCardViewItem != null) downloadCardViewItem.setSize(size);
        }

        public void setMd5sum(String md5sum) {
            this.md5sum = md5sum;
            if (downloadCardViewItem != null) downloadCardViewItem.setMd5sum(md5sum);
        }

        public void setNote(String note) {
            this.note = note;
            if (downloadCardViewItem != null) downloadCardViewItem.setNote(note);
        }

        public void setDownload(String download) {
            this.download = download;
            if (downloadCardViewItem != null) downloadCardViewItem.setDownload(download);
        }

        public void setChangelog(String changelog) {
            this.changelog = changelog;
            if (downloadCardViewItem != null) downloadCardViewItem.setChangelog(changelog);
        }

        public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
            this.onDownloadListener = onDownloadListener;
            if (downloadCardViewItem != null)
                downloadCardViewItem.setOnDownloadListener(onDownloadListener);
        }

    }

}
