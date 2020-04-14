package ng.com.laundrify.laundrify.RaveClasses.validators;

public class PhoneValidator {

    public boolean isPhoneValid(String phone) {
        return phone.length() >= 1;
    }
}
