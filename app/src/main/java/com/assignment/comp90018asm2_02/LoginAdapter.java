package com.assignment.comp90018asm2_02;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class LoginAdapter extends FragmentPagerAdapter {

    private Context context;
    int nTabs;

    public LoginAdapter(FragmentManager manager, Context context, int nTabs) {
        super(manager);
        this.context = context;
        this.nTabs = nTabs;
    }

    @Override
    public int getCount() {
        return nTabs;
    }

    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                LoginFragment loginFragment = new LoginFragment();
                return loginFragment;
            case 1:
                RegisterFragment registerFragment = new RegisterFragment();
                return registerFragment;
            default:
                return null;
        }
    }
}
