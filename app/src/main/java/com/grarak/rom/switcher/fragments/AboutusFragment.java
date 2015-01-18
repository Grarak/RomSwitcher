package com.grarak.rom.switcher.fragments;

import android.os.Bundle;

import com.grarak.rom.switcher.R;
import com.grarak.rom.switcher.elements.CardViewItem;
import com.grarak.rom.switcher.elements.RecyclerViewFragment;
import com.grarak.rom.switcher.utils.Utils;

/**
 * Created by willi on 18.01.15.
 */
public class AboutusFragment extends RecyclerViewFragment {

    private final String APP_SOURCE = "https://github.com/Grarak/RomSwitcher";

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        setTextTitle(getString(R.string.know_us));

        CardViewItem.DCardView mAppLicenseCard = new CardViewItem.DCardView();
        mAppLicenseCard.setView(inflater.inflate(R.layout.app_license_view, container, false));

        addView(mAppLicenseCard);

        CardViewItem.DCardView mOpenSourceCard = new CardViewItem.DCardView();
        mOpenSourceCard.setTitle(getString(R.string.open_source));
        mOpenSourceCard.setDescription(getString(R.string.open_source_summary));
        mOpenSourceCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                Utils.launchUrl(getActivity(), APP_SOURCE);
            }
        });

        addView(mOpenSourceCard);

        if (Utils.getDevicesJson(getActivity()).hasDonation()) {
            CardViewItem.DCardView mDonationCard = new CardViewItem.DCardView();
            mDonationCard.setTitle(getString(R.string.donate));
            mDonationCard.setDescription(getString(R.string.donate_summary));
            mDonationCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
                @Override
                public void onClick(CardViewItem.DCardView dCardView) {
                    Utils.launchUrl(getActivity(), Utils.getDevicesJson(getActivity()).getDonation());
                }
            });

            addView(mDonationCard);
        }
    }
}
