package ng.com.laundrify.laundrify.utils;

import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class PromoLayoutModel {
    LinearLayout promoLayout;
    TextView promoTitle;
    TextView promoDesc;
    private String descText;

    public PromoLayoutModel(LinearLayout promoLayout, TextView promoTitle, TextView promoDesc, String descText) {
        this.promoLayout = promoLayout;
        this.promoTitle = promoTitle;
        this.promoDesc = promoDesc;
        this.descText = descText;
    }

    public LinearLayout getPromoLayout() {
        return promoLayout;
    }

    public void setPromoLayout(LinearLayout promoLayout) {
        this.promoLayout = promoLayout;
    }

    public TextView getPromoTitle() {
        return promoTitle;
    }

    public void setPromoTitle(TextView promoTitle) {
        this.promoTitle = promoTitle;
    }

    public TextView getPromoDesc() {
        return promoDesc;
    }

    public void setPromoDesc(TextView promoDesc) {
        this.promoDesc = promoDesc;
    }

    public String getDescText() {
        return descText;
    }

    public void setDescText(String descText) {
        this.descText = descText;
    }
}

