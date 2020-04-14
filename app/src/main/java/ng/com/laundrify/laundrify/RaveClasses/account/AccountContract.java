package ng.com.laundrify.laundrify.RaveClasses.account;



import java.util.HashMap;
import java.util.List;

import ng.com.laundrify.laundrify.RaveClasses.Payload;
import ng.com.laundrify.laundrify.RaveClasses.RavePayInitializer;
import ng.com.laundrify.laundrify.RaveClasses.ViewObject;
import ng.com.laundrify.laundrify.RaveClasses.data.Bank;
import ng.com.laundrify.laundrify.RaveClasses.responses.RequeryResponse;

/**
 * Created by hamzafetuga on 20/07/2017.
 */

public interface AccountContract {

    interface View {
        void showToast(String message);
        void showBanks(List<Bank> banks);
        void showProgressIndicator(boolean active);

        void onGetBanksRequestFailed(String message);

        void validateAccountCharge(String pbfPubKey, String flwRef, String validateInstruction);

        void onDisplayInternetBankingPage(String authurl, String flwRef);

        void onChargeAccountFailed(String message, String responseAsJSONString);

        void onPaymentSuccessful(String status, String responseAsJSONString);

        void onPaymentFailed(String status, String responseAsJSONString);

        void onValidateSuccessful(String flwRef, String responseAsJSONString);

        void onValidateError(String message, String responseAsJSONString);

        void onPaymentError(String s);

        void displayFee(String charge_amount, Payload payload, boolean internetbanking);

        void showFetchFeeFailed(String s);

        void onRequerySuccessful(RequeryResponse response, String responseAsJSONString);

        void onValidationSuccessful(HashMap<String, ViewObject> dataHashMap);

        void showFieldError(int viewID, String message, Class<?> viewtype);

        void showGTBankAmountIssue();

        void onEmailValidated(String emailToSet, int visibility);

        void onAmountValidated(String amountToSet, int visibility);

        void showDateOfBirth(int whatToShow);

        void showBVN(int whatToShow);

        void showInternetBankingSelected(int whatToShow);
    }

    interface UserActionsListener {
        void getBanks();

        void chargeAccount(Payload body, String encryptionKey, boolean internetBanking);

        void validateAccountCharge(String flwRef, String otp, String publicKey);

        void fetchFee(Payload body, boolean internetbanking);

        void onAttachView(AccountContract.View view);

        void onDetachView();

        void verifyRequeryResponseStatus(RequeryResponse response, String responseAsJSONString, RavePayInitializer ravePayInitializer);

        void onDataCollected(HashMap<String, ViewObject> dataHashMap);

        void processTransaction(HashMap<String, ViewObject> dataHashMap, RavePayInitializer ravePayInitializer);

        void init(RavePayInitializer ravePayInitializer);

        void onInternetBankingValidated(Bank bank);
    }

}
