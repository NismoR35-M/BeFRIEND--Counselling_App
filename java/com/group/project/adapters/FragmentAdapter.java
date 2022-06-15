package com.group.project.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.group.project.fragments.ArticlesFragment;
import com.group.project.fragments.ArticlesFragments;
import com.group.project.fragments.CallsFragment;
import com.group.project.fragments.ChatCounsellorFragment;
import com.group.project.fragments.ChatRoomsFragment;

public class FragmentAdapter extends FragmentPagerAdapter {
    public FragmentAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0: return new ChatCounsellorFragment();
            case 1: return new ChatRoomsFragment();
            case 2: return new CallsFragment();
            case 3: return new ArticlesFragments();
            default: return new ChatCounsellorFragment();
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 0) {
            title = "CHATS";
        }
        if (position == 1) {
            title = "SPACE";
        }
        if (position == 2) {
            title = "CALLS";
        }
        if (position == 3) {
            title = "ARTICLES";
        }
        return title;
    }
}
