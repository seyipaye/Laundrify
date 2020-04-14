package ng.com.laundrify.laundrify;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class Items_Adapter extends RecyclerView.Adapter<Items_Adapter.MyViewHolder> {
    List<Items_Model> items_model;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        TextView itemPrice;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
        }
    }

    public Items_Adapter(List<Items_Model> data) {
        items_model = data;
    }

    @NonNull
    @Override
    public Items_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_row, viewGroup, false);

        Items_Adapter.MyViewHolder myViewHolder = new Items_Adapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull Items_Adapter.MyViewHolder myViewHolder, int i) {
        TextView itemName = myViewHolder.itemName;
        TextView itemPrice = myViewHolder.itemPrice;

        itemName.setText(items_model.get(i).getItemName());
        itemPrice.setText(items_model.get(i).getItemPrice());
    }

    @Override
    public int getItemCount() {
        return items_model.size();
    }


}
