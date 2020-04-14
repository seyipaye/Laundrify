package ng.com.laundrify.laundrify.RaveClasses.validators;

import ng.com.laundrify.laundrify.RaveClasses.Utils;

public class CardNoValidator {

    public boolean isCardNoStrippedValid(String cardNoStripped) {
        return !(cardNoStripped.length() < 12 | !Utils.isValidLuhnNumber(cardNoStripped));
    }
}
