package com.grarak.rom.switcher.fragments;

import android.os.Bundle;

import com.grarak.rom.switcher.R;
import com.grarak.rom.switcher.elements.CardViewItem;
import com.grarak.rom.switcher.elements.RecyclerViewFragment;
import com.grarak.rom.switcher.utils.Constants;
import com.grarak.rom.switcher.utils.Utils;

/**
 * Created by willi on 10.01.15.
 */
public class InformationFragment extends RecyclerViewFragment {

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        boolean supported = Utils.getDevicesJson(getActivity()).isSupported();

        setTextTitle(getString(supported ? R.string.supported : R.string.not_supported));

        CardViewItem.DCardView mStatusCard = new CardViewItem.DCardView();
        mStatusCard.setTitle(getString(R.string.status));
        mStatusCard.setDescription(getString(supported ? R.string.supported_summary
                : R.string.not_supported_summary, Constants.DEVICE_MODEL));

        addView(mStatusCard);

        if (supported) {
            boolean installed = Utils.isInstalled();
            CardViewItem.DCardView mInstalledCard = new CardViewItem.DCardView();
            mInstalledCard.setDescription(getString(installed ? R.string.installed_summary : R.string.not_installed_summary));
            mInstalledCard.setIcon(getResources().getDrawable(installed ? R.drawable.ic_ok : R.drawable.ic_error));

            addView(mInstalledCard);

            if (installed) {
                String version = Utils.getVersion();
                CardViewItem.DCardView mVersionCard = new CardViewItem.DCardView();
                mVersionCard.setTitle(getString(R.string.version));
                mVersionCard.setDescription(version != null ? version : getString(R.string.unknown));

                addView(mVersionCard);
            }
        }
    }
}
