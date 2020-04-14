package ng.com.laundrify.laundrify.Adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import ng.com.laundrify.laundrify.MainActivity;
import ng.com.laundrify.laundrify.Model;
import ng.com.laundrify.laundrify.R;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private List<Model> modelsData;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView title, desc;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.title1);
            this.desc = itemView.findViewById(R.id.description);
            this.imageView = itemView.findViewById(R.id.cardImage);
        }
    }

    public CustomAdapter(List<Model> data) {
        this.modelsData = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.carditem, parent, false);

       view.setOnClickListener(MainActivity.myOnClickListener);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        TextView title = holder.title;
        TextView desc = holder.desc;
        ImageView imageView = holder.imageView;

        title.setText(modelsData.get(listPosition).getTitle());
        desc.setText(modelsData.get(listPosition).getDesc());
        Glide.with(holder.itemView)
                .load(modelsData.get(listPosition).getImage())
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        return modelsData.size();
    }
}
