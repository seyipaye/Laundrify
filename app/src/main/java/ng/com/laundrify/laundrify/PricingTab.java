package ng.com.laundrify.laundrify;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

public class PricingTab extends Fragment {


    public static Fragment newPriceFrag(ArrayList<Price_Model> gottenModels) {
        return new PricingTab();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.tab_1frag,null);
        RecyclerView recyclerView = v.findViewById(R.id.tab1Rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));


        Bundle bundle = getArguments();
        if (bundle!= null) {

            int position;
            position = bundle.getInt("Position", 0);
            recyclerView.setAdapter(new Price_Adapter(PriceActivity.price_fragments.get(position).getPrice_models()));
        } else {
            Toast.makeText(getContext(), "Uexpected error 111", Toast.LENGTH_SHORT).show();
        }



        return v;
    }

}
