package ng.com.laundrify.laundrify.utils;

public class CardsModel {
    public int Drawable;
    String First6digits;
    String Last4digits;
    String Token;
    String CardKey;

    public CardsModel(String first6digits, String last4digits, int ic_credit_card_black_24dp, String token, String cardKey) {
        First6digits = first6digits;
        Last4digits = last4digits;
        Drawable = ic_credit_card_black_24dp;
        Token = token;
        CardKey = cardKey;
    }

    public String getFirst6digits() {
        return First6digits;
    }

    public void setFirst6digits(String first6digits) {
        First6digits = first6digits;
    }

    public String getLast4digits() {
        return Last4digits;
    }

    public void setLast4digits(String last4digits) {
        Last4digits = last4digits;
    }

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }

    public int getDrawable() {
        return Drawable;
    }

    public void setDrawable(int drawable) {
        Drawable = drawable;
    }

    public String getCardKey() {
        return CardKey;
    }

    public void setCardKey(String cardKey) {
        CardKey = cardKey;
    }
}
