package ng.com.laundrify.laundrify.RaveClasses.card;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ng.com.laundrify.laundrify.R;
import ng.com.laundrify.laundrify.RaveClasses.Utils;
import ng.com.laundrify.laundrify.RaveClasses.data.Callbacks;
import ng.com.laundrify.laundrify.RaveClasses.data.SavedCard;

/**
 * Created by hamzafetuga on 25/07/2017.
 */

public class SavedCardRecyclerAdapter extends RecyclerView.Adapter<SavedCardRecyclerAdapter.ViewHolder>{

    private List<SavedCard> cards;
    private Callbacks.SavedCardSelectedListener savedCardSelectedListener;

    public void set(List<SavedCard> cards) {
        this.cards = cards;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View v = inflater.inflate(R.layout.select_bank_list_item, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(cards.get(position));
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public void setSavedCardSelectedListener(Callbacks.SavedCardSelectedListener savedCardSelectedListener) {
        this.savedCardSelectedListener = savedCardSelectedListener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView itemTv;
        SavedCard card;


        ViewHolder(View v) {
            super(v);
            itemTv = v.findViewById(R.id.bankNameTv);
            v.setOnClickListener(this);
        }

        public void bind(SavedCard card) {
            this.card = card;
            itemTv.setText(Utils.spacifyCardNumber(Utils.obfuscateCardNumber(card.getFirst6(), card.getLast4())));
        }


        @Override
        public void onClick(View v) {
            savedCardSelectedListener.onCardSelected(card);
        }
    }
}
