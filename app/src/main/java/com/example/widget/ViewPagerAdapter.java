package com.example.widget;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private List<FragmentPage> documentsList = new ArrayList<>();

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager,@NonNull Lifecycle lifecycle, List<FragmentPage> fragments) {
        super(fragmentManager, lifecycle);
        documentsList = fragments;
    }

    @Override
    public int getItemCount() {
        return documentsList.size();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return documentsList.get(position);
    }
}
