package ng.com.laundrify.laundrify.utils;

public class PromotionsModel {
    private String header;
    private String description;
    private int percentage;
    private int usage;
    private String promoKey;
    private int maxValue;

    public PromotionsModel(String header, String description, int percentage,int maxValue, int usage, String promoKey) {
        this.header = header;
        this.description = description;
        this.percentage = percentage;
        this.usage = usage;
        this.promoKey = promoKey;
        this.maxValue = maxValue;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public int getUsage() {
        return usage;
    }

    public void setUsage(int usage) {
        this.usage = usage;
    }

    public String getPromoKey() {
        return promoKey;
    }

    public void setPromoKey(String promoKey) {
        this.promoKey = promoKey;
    }


    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }
}
