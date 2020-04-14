package ng.com.laundrify.laundrify.RaveClasses.card;

import androidx.fragment.app.Fragment;
import android.view.View;

import java.util.HashMap;
import java.util.List;

import ng.com.laundrify.laundrify.RaveClasses.Payload;
import ng.com.laundrify.laundrify.RaveClasses.ViewObject;
import ng.com.laundrify.laundrify.RaveClasses.data.SavedCard;
import ng.com.laundrify.laundrify.RaveClasses.responses.ChargeResponse;
import ng.com.laundrify.laundrify.RaveClasses.responses.RequeryResponse;

/**
 * Created by hfetuga on 06/02/2018.
 */

public class NullCardView extends Fragment implements View.OnClickListener, CardContract.View {

    @Override
    public void onClick(View v) {

    }

    @Override
    public void showProgressIndicator(boolean active) {

    }

    @Override
    public void onNoAuthUsed(String flwRef, String secretKey) {

    }

    @Override
    public void onValidationSuccessful(HashMap<String, ViewObject> dataHashMap) {

    }

    @Override
    public void showFieldError(int viewID, String message, Class<?> viewtype) {

    }

    @Override
    public void onNoAuthInternationalSuggested(Payload payload) {

    }

    @Override
    public void onPaymentError(String message) {

    }

    @Override
    public void onPinAuthModelSuggested(Payload payload) {

    }

    @Override
    public void showToast(String message) {

    }

    @Override
    public void showOTPLayout(String flwRef, String chargeResponseMessage) {

    }

    @Override
    public void onValidateSuccessful(String message, String responseAsString) {

    }

    @Override
    public void onValidateError(String message) {

    }

    @Override
    public void onVBVAuthModelUsed(String authUrlCrude, String flwRef) {

    }

    @Override
    public void onPaymentSuccessful(String status, String flwRef, String responseAsString) {

    }

    @Override
    public void onPaymentFailed(String status, String responseAsString) {

    }


    @Override
    public void showSavedCards(List<SavedCard> cards) {

    }

    @Override
    public void onTokenRetrieved(String secretKey, String cardBIN, String token) {

    }

    @Override
    public void onTokenRetrievalError(String s) {

    }

    @Override
    public void onEmailValidated(String emailToSet, int visibility) {

    }

    @Override
    public void onAmountValidated(String amountToSet, int visibility) {

    }

    @Override
    public void displayFee(String charge_amount, Payload payload, int why) {

    }

    @Override
    public void showFetchFeeFailed(String s) {

    }

    @Override
    public void onChargeTokenComplete(ChargeResponse response) {

    }

    @Override
    public void onChargeCardSuccessful(ChargeResponse response) {

    }

    @Override
    public void onAVS_VBVSECURECODEModelSuggested(Payload payload) {

    }

    @Override
    public void onAVSVBVSecureCodeModelUsed(String authurl, String flwRef) {

    }

    @Override
    public void onValidateCardChargeFailed(String flwRef, String responseAsJSON) {

    }

    @Override
    public void onRequerySuccessful(RequeryResponse response, String responseAsJSONString, String flwRef) {

    }
}
