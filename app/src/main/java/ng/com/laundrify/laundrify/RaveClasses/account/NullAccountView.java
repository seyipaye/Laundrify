package ng.com.laundrify.laundrify.RaveClasses.account;

import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.List;

import ng.com.laundrify.laundrify.RaveClasses.Payload;
import ng.com.laundrify.laundrify.RaveClasses.ViewObject;
import ng.com.laundrify.laundrify.RaveClasses.data.Bank;
import ng.com.laundrify.laundrify.RaveClasses.responses.RequeryResponse;

/**
 * Created by hfetuga on 06/02/2018.
 */

public class NullAccountView extends Fragment implements AccountContract.View {

    @Override
    public void showToast(String message) {

    }

    @Override
    public void showBanks(List<Bank> banks) {

    }

    @Override
    public void showProgressIndicator(boolean active) {

    }

    @Override
    public void onGetBanksRequestFailed(String message) {

    }

    @Override
    public void validateAccountCharge(String pbfPubKey, String flwRef, String validateInstruction) {

    }

    @Override
    public void onDisplayInternetBankingPage(String authurl, String flwRef) {

    }

    @Override
    public void onChargeAccountFailed(String message, String responseAsJSONString) {

    }

    @Override
    public void onPaymentSuccessful(String status, String responseAsJSONString) {

    }

    @Override
    public void onPaymentFailed(String status, String responseAsJSONString) {

    }

    @Override
    public void onValidateSuccessful(String flwRef, String responseAsJSONString) {

    }

    @Override
    public void onValidateError(String message, String responseAsJSONString) {

    }

    @Override
    public void onPaymentError(String s) {

    }

    @Override
    public void displayFee(String charge_amount, Payload payload, boolean internetbanking) {

    }

    @Override
    public void showFetchFeeFailed(String s) {

    }

    @Override
    public void onRequerySuccessful(RequeryResponse response, String responseAsJSONString) {

    }

    @Override
    public void onValidationSuccessful(HashMap<String, ViewObject> dataHashMap) {

    }

    @Override
    public void showFieldError(int viewID, String message, Class<?> viewtype) {

    }

    @Override
    public void showGTBankAmountIssue() {

    }

    @Override
    public void onEmailValidated(String emailToSet, int visibility) {

    }

    @Override
    public void onAmountValidated(String amountToSet, int visibility) {

    }

    @Override
    public void showDateOfBirth(int whatToShow) {

    }

    @Override
    public void showBVN(int whatToShow) {

    }

    @Override
    public void showInternetBankingSelected(int whatToShow) {

    }
}
