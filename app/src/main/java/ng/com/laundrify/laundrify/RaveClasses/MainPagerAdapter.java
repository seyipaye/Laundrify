package ng.com.laundrify.laundrify.RaveClasses;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hamzafetuga on 05/07/2017.
 */

public class MainPagerAdapter extends FragmentPagerAdapter {

    List<RaveFragment> fragments = new ArrayList<>();

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    // Return a different fragment for position based on additional state tracked in a member variable
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position).getFragment();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragments.get(position).getTitle();
    }

    public List<RaveFragment> getFragments() {
        return fragments;
    }

    //Set Fragments for Adapter
    public void setFragments(List<RaveFragment> fragments) {
        this.fragments = fragments;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

}