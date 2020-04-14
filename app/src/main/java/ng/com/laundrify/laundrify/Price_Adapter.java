package ng.com.laundrify.laundrify;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import ng.com.laundrify.laundrify.utils.Utils;

import static com.android.volley.VolleyLog.TAG;

public class Price_Adapter extends RecyclerView.Adapter<Price_Adapter.MyViewHolder> {

    private List<Price_Model> price_models;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView priceDesc, priceHeader, priceBonus, pricePrice;
        View strikeThrough;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.strikeThrough = itemView.findViewById(R.id.strikeThrough);
            this.imageView = itemView.findViewById(R.id.priceBG);
            this.priceHeader = itemView.findViewById(R.id.priceHeader);
            this.pricePrice = itemView.findViewById(R.id.pricePrice);
            this.priceBonus = itemView.findViewById(R.id.priceBonus);
            this.priceDesc = itemView.findViewById(R.id.priceDesc);

        }
    }

    public Price_Adapter(List<Price_Model> data) {
        this.price_models = data;
    }


    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.price_row , parent, false);

        //view.setOnClickListener(PickupActivity.myOnClickListener);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int listPosition) {

        ImageView imageView = holder.imageView;
        TextView priceHeader = holder.priceHeader;
        TextView pricePrice = holder.pricePrice;
        TextView priceDesc = holder.priceDesc;
        TextView priceBonus = holder.priceBonus;
        View strikeThrough = holder.strikeThrough;
        String priceBG= price_models.get(listPosition).getPriceBG();
        Log.i(TAG, "onBindViewHolder: " + priceBG + "first");

        if (priceBG == null || priceBG.matches("null")) {

            int resource = Utils.tryGetingResource(price_models.get(listPosition).getPriceKey());
            Glide.with(holder.itemView)
                    .load(resource)
                    .into(imageView);
            Log.i(TAG, "whatIs: " + resource);

        } else {
            Glide.with(holder.itemView)
                    .load(price_models.get(listPosition).getPriceBG())
                    .into(imageView);
            Log.i(TAG, "onBindViewHolder: " + price_models.get(listPosition).getPriceBG());
        }

        priceHeader.setText(price_models.get(listPosition).getPriceHeader());
        pricePrice.setText("₦" + price_models.get(listPosition).getPricePrice());
        priceDesc.setText(price_models.get(listPosition).getPriceDesc());
        if (price_models.get(listPosition).getPriceBonus() != 0) {

            // If, there is bonus
            double promoPrice = changePrice(price_models.get(listPosition).getPricePrice(), price_models.get(listPosition).getPriceBonus(),
                    1000);
            String promoString = "₦" + promoPrice;
            priceBonus.setText(promoString.substring(0, promoString.indexOf(".")));
            Log.i(TAG, "onBindViewHolder: " + price_models.get(listPosition).getPricePrice() + price_models.get(listPosition).getPriceBonus()
            + promoPrice);

            strikeThrough.setVisibility(View.VISIBLE);
        } else {
            strikeThrough.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        if (price_models == null) {
            return 0;
        } else {
            return price_models.size();
        }
    }

    private double changePrice(double originalPrice, double percentage, int maxValue) {

        double promoCut = percentage / 100 * originalPrice;
        double promoPrice;
        if (promoCut >= maxValue) {
            promoPrice = originalPrice - maxValue;
        } else {
            promoPrice = originalPrice - promoCut;
        }
        promoPrice = Math.round(promoPrice * 10) / 10;

        return promoPrice;
    }
}
