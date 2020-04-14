package ng.com.laundrify.laundrify.RaveClasses.validators;

public class CardExpiryValidator {

    public boolean isCardExpiryValid(String cardExpiry) {
        return cardExpiry.length() == 5;
    }
}
