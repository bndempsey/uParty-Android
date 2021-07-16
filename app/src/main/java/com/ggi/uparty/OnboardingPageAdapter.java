package com.ggi.uparty;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ggi.uparty.loginFrags.OnboardingFragment;

import java.util.ArrayList;

public class OnboardingPageAdapter extends FragmentPagerAdapter {

    private ArrayList<OnboardingFragment> fragments;

    public OnboardingPageAdapter(FragmentManager fm, ArrayList<OnboardingFragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }
}
