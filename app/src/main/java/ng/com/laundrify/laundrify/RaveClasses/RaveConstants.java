package ng.com.laundrify.laundrify.RaveClasses;

/**
 * Created by hamzafetuga on 14/07/2017.
 */

public class RaveConstants {
    public static int PERMISSIONS_REQUEST_READ_PHONE_STATE = 419;

//    public static String PUBLIC_KEY = "";
//    public static String PRIVATE_KEY = "";
    public static String PUBLIC_KEY = "FLWPUBK-e634d14d9ded04eaf05d5b63a0a06d2f-X"; //test
//    public static String ENCRYPTION_KEY = "FLWSECK-bb971402072265fb156e90a3578fe5e6-X"; //test
    public static String ENCRYPTION_KEY = "bb9714020722eb4cf7a169f2";
    public static String STAGING_URL = "https://ravesandbox.azurewebsites.net";
    public static String LIVE_URL = "https://raveapi.azurewebsites.net";
    public static String VBV = "VBVSECURECODE";
    public static String GTB_OTP = "GTB_OTP";
    public static String ACCESS_OTP = "ACCESS_OTP";
    public static String NOAUTH = "NOAUTH";
    public static String PIN = "PIN";
    public static String AVS_VBVSECURECODE = "AVS_VBVSECURECODE";
    public static String NOAUTH_INTERNATIONAL = "NOAUTH_INTERNATIONAL";
    public static String RAVEPAY = "ravepay";
    public static String RAVE_PARAMS = "raveparams";
    public static String RAVE_3DS_CALLBACK = "https://rave-webhook.herokuapp.com/receivepayment";
    public static int RAVE_REQUEST_CODE = 4199;
    public static int MANUAL_CARD_CHARGE = 403;
    public static int TOKEN_CHARGE = 24;
    public static String fieldAmount = "amount";
    public static String fieldPhone = "phone";
    public static String fieldEmail = "email";
    public static String fieldAccount = "account";
    public static String fieldVoucher = "voucher";
    public static String fieldNetwork = "network";
    public static String fieldBVN = "bvn";
    public static String fieldDOB = "dob";
    public static String fieldBankCode = "bankcode";
    public static String fieldCvv = "cvv";
    public static String fieldCardExpiry = "cardExpiry";
    public static String fieldcardNoStripped = "cardNoStripped";
    public static String date_of_birth = "Date of Birth";
    public static String isInternetBanking = "bankcode";

    public static String success = "success";
    public static String noResponse = "No response data was returned";

    public static String response = "response";
    public static String mtn = "mtn";
    public static String tigo = "tigo";
    public static String vodafone = "vodafone";

    public static String tokenNotFound = "token not found";
    public static String expired = "expired";
    public static String tokenExpired = "Token expired";
    public static String cardNoStripped = "cardNoStripped";
    public static String validAmountPrompt = "Enter a valid amount";
    public static String validPhonePrompt = "Enter a valid number";
    public static String validEmailPrompt = "Enter a valid Email";
    public static String charge = "You will be charged a total of";
    public static String askToContinue = ". Do you want to continue?";
    public static String yes = "YES";
    public static String no = "NO";
    public static String cancel = "CANCEL";
    public static String checkStatus = "Checking transaction status.  Please wait";
    public static String transactionError = "An error occurred while retrieving transaction fee";
    public static String validCvvPrompt = "Enter a valid cvv";
    public static String validExpiryDatePrompt = "Enter a valid expiry date";
    public static String validCreditCardPrompt = "Enter a valid credit card number";
    public static String validVoucherPrompt = "Enter a valid voucher code";
    public static String validNetworkPrompt = "Select a network";
    public static String invalidChargeCode = "Invalid charge response code";
    public static String invalidCharge = "Invalid charge card response";
    public static String unknownAuthmsg = "Unknown Auth Model";
    public static String unknownResCodemsg = "Unknown charge response code";
    public static String no_authurl_was_returnedmsg = "No authUrl was returned";
    public static String no_response_data_was_returnedmsg = "No response data was returned";
    public static String wait = "Please wait...";
    public static String cancelPayment = "CANCEL PAYMENT";
}
