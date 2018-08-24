package com.knstech.friendsapp2;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class SectionPagerAdapter extends FragmentPagerAdapter {
    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
       switch (position){

           case 0:
                    RequestsFragment requestsFragment=new RequestsFragment();
                    return requestsFragment;

           case 1:  ChatFragment chatFragment=new ChatFragment();
                     return chatFragment;

           case 2:   FriendsFragment friendsFragment=new FriendsFragment();
                        return friendsFragment;

           default: return null;
       }

    }

    @Override
    public int getCount() {
        // since three tabs we write 3
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){

            case 0:

                return "BLOG";

            case 1:
                return "CHATS";

            case 2:
                return "FRIENDS";

            default: return null;
        }

    }
}
