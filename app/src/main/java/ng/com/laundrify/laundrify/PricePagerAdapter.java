package ng.com.laundrify.laundrify;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class PricePagerAdapter extends FragmentPagerAdapter {

    List<PriceFragment> fragments;
    public PricePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragments.get(position).getTitle();
    }

    @Override
    public Fragment getItem(int i) {

        Bundle bundle = new Bundle();
        bundle.putInt("Position", i);

        Fragment thisFrag = fragments.get(i).getFragment();
        thisFrag.setArguments(bundle);

        return thisFrag;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public void setFragments(List<PriceFragment> fragments) {
        this.fragments = fragments;
    }
}
