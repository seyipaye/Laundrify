package ng.com.laundrify.laundrify.RaveClasses.validators;

public class CvvValidator {

    public boolean isCvvValid(String cvv) {
        return cvv.length() >= 3;
    }
}
