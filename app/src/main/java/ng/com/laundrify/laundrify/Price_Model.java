package ng.com.laundrify.laundrify;

import androidx.fragment.app.Fragment;

import java.util.List;

public class Price_Model {

    private String priceBG;
    private String priceHeader;
    private int priceBonus;
    private String priceDesc;
    private int pricePrice;
    private String priceKey;

    public Price_Model(String priceBG, int priceBonus, int pricePrice, String priceHeader, String priceDesc, String priceKey) {
        this.priceBG = priceBG;
        this.priceHeader = priceHeader;
        this.pricePrice = pricePrice;
        this.priceBonus = priceBonus;
        this.priceDesc = priceDesc;
        this.priceKey = priceKey;
    }

    public String getPriceBG() {
        return priceBG;
    }

    public void setPriceBG(String priceBG) {
        this.priceBG = priceBG;
    }

    public String getPriceHeader() {
        return priceHeader;
    }

    public void setPriceHeader(String priceHeader) {
        this.priceHeader = priceHeader;
    }

    public int getPriceBonus() {
        return priceBonus;
    }

    public void setPriceBonus(int priceBonus) {
        this.priceBonus = priceBonus;
    }

    public String getPriceDesc() {
        return priceDesc;
    }

    public void setPriceDesc(String priceDesc) {
        this.priceDesc = priceDesc;
    }

    public int getPricePrice() {
        return pricePrice;
    }

    public void setPricePrice(int pricePrice) {
        this.pricePrice = pricePrice;
    }

    public String getPriceKey() {
        return priceKey;
    }

    public void setPriceKey(String priceKey) {
        this.priceKey = priceKey;
    }
}

 class PriceFragment {
    List<Price_Model> price_models;
    Fragment fragment;
    String title;

    public PriceFragment(List<Price_Model> price_models, Fragment fragment, String title) {
        this.price_models = price_models;
        this.fragment = fragment;
        this.title = title;
    }

    public List<Price_Model> getPrice_models() {
        return price_models;
    }

    public void setPrice_models(List<Price_Model> price_models) {
        this.price_models = price_models;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
