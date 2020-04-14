package ng.com.laundrify.laundrify.RaveClasses.ghmobilemoney;

import java.util.HashMap;

import ng.com.laundrify.laundrify.RaveClasses.Payload;
import ng.com.laundrify.laundrify.RaveClasses.RavePayInitializer;
import ng.com.laundrify.laundrify.RaveClasses.ViewObject;

/**
 * Created by hfetuga on 28/06/2018.
 */

public interface GhMobileMoneyContract {

    interface View {
        void showToast(String message);
        void showFetchFeeFailed(String s);
        void onPaymentError(String message);
        void showPollingIndicator(boolean active);
        void showProgressIndicator(boolean active);
        void onAmountValidationSuccessful(String valueOf);
        void displayFee(String charge_amount, Payload payload);
        void onPaymentFailed(String message, String responseAsJSONString);
        void showFieldError(int viewID, String message, Class<?> viewtype);
        void onValidationSuccessful(HashMap<String, ViewObject> dataHashMap);
        void onPollingRoundComplete(String flwRef, String txRef, String publicKey);
        void onPaymentSuccessful(String status, String flwRef, String responseAsString);

    }

    interface UserActionsListener {
        void fetchFee(Payload payload);
        void init(RavePayInitializer ravePayInitializer);
        void onDataCollected(HashMap<String, ViewObject> dataHashMap);
        void requeryTx(String flwRef, String txRef, String publicKey);
        void chargeGhMobileMoney(Payload payload, String encryptionKey);
        void processTransaction(HashMap<String, ViewObject> dataHashMap, RavePayInitializer ravePayInitializer);
    }
}
