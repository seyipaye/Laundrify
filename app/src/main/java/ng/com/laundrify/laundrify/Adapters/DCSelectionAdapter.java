package ng.com.laundrify.laundrify.Adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

import ng.com.laundrify.laundrify.MainActivity;
import ng.com.laundrify.laundrify.Models.AvailableDCsModel;
import ng.com.laundrify.laundrify.R;
import ng.com.laundrify.laundrify.utils.RequestPickupFragment;

public class DCSelectionAdapter extends RecyclerView.Adapter<DCSelectionAdapter.MyViewHolder> {
    ArrayList<AvailableDCsModel> availableDCsModels;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dcselection_item, viewGroup, false);

        view.setOnClickListener(RequestPickupFragment.dcSelectionClickListener);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    public DCSelectionAdapter(ArrayList<AvailableDCsModel> availableDCsModels) {
        this.availableDCsModels = availableDCsModels;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        TextView ratingText = myViewHolder.ratingText;
        TextView dcCompanyName = myViewHolder.dcCompanyName;
        TextView reviews = myViewHolder.reviews;
        TextView distance = myViewHolder.distance;
        RatingBar ratingBar = myViewHolder.ratingBar;


        dcCompanyName.setText(availableDCsModels.get(i).getName());
        distance.setText(String.format("%s KM", availableDCsModels.get(i).getDistance()));
        ratingText.setText(String.valueOf(availableDCsModels.get(i).getAvgRating()));
        try {
            ratingBar.setRating(availableDCsModels.get(i).getAvgRating());
        } catch (Exception e) {
            e.printStackTrace();
            ratingBar.setRating(0);
        }
        /*
        Glide.with(holder.itemView)
                .load(modelsData.get(listPosition).getImage())
                .into(imageView);
                */
    }

    @Override
    public int getItemCount() {
        return availableDCsModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        RatingBar ratingBar;
        TextView ratingText, dcCompanyName, reviews, distance;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.ratingText = itemView.findViewById(R.id.ratingText);
            this.dcCompanyName = itemView.findViewById(R.id.dcCompanyName);
            this.reviews = itemView.findViewById(R.id.reviews);
            this.distance = itemView.findViewById(R.id.distance);
            this.ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
