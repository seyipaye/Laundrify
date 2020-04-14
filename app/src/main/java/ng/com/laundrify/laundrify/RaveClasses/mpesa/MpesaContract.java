package ng.com.laundrify.laundrify.RaveClasses.mpesa;

import java.util.HashMap;

import ng.com.laundrify.laundrify.RaveClasses.Payload;
import ng.com.laundrify.laundrify.RaveClasses.RavePayInitializer;
import ng.com.laundrify.laundrify.RaveClasses.ViewObject;

/**
 * Created by hfetuga on 27/06/2018.
 */

public interface MpesaContract {

    interface View {
        void showToast(String message);
        void showFetchFeeFailed(String s);
        void onPaymentError(String message);
        void showPollingIndicator(boolean active);
        void showProgressIndicator(boolean active);
        void onAmountValidationSuccessful(String amountToPay);
        void displayFee(String charge_amount, Payload payload);
        void onPaymentFailed(String message, String responseAsJSONString);
        void onValidationSuccessful(HashMap<String, ViewObject> dataHashMap);
        void showFieldError(int viewID, String message, Class<?> viewType);
        void onPollingRoundComplete(String flwRef, String txRef, String encryptionKey);
        void onPaymentSuccessful(String status, String flwRef, String responseAsString);
    }

    interface UserActionsListener {
        void fetchFee(Payload payload);
        void init(RavePayInitializer ravePayInitializer);
        void onDataCollected(HashMap<String, ViewObject> dataHashMap);
        void chargeMpesa(Payload payload, String encryptionKey);
        void requeryTx(String flwRef, String txRef, String publicKey);
        void processTransaction(HashMap<String, ViewObject> dataHashMap, RavePayInitializer ravePayInitializer);
    }
}
