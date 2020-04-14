package ng.com.laundrify.laundrify.RaveClasses.mpesa;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;

import ng.com.laundrify.laundrify.RaveClasses.FeeCheckRequestBody;
import ng.com.laundrify.laundrify.RaveClasses.Payload;
import ng.com.laundrify.laundrify.RaveClasses.PayloadBuilder;
import ng.com.laundrify.laundrify.RaveClasses.RaveConstants;
import ng.com.laundrify.laundrify.RaveClasses.RavePayInitializer;
import ng.com.laundrify.laundrify.RaveClasses.Utils;
import ng.com.laundrify.laundrify.RaveClasses.ViewObject;
import ng.com.laundrify.laundrify.RaveClasses.card.ChargeRequestBody;
import ng.com.laundrify.laundrify.RaveClasses.data.Callbacks;
import ng.com.laundrify.laundrify.RaveClasses.data.NetworkRequestImpl;
import ng.com.laundrify.laundrify.RaveClasses.data.RequeryRequestBody;
import ng.com.laundrify.laundrify.RaveClasses.responses.ChargeResponse;
import ng.com.laundrify.laundrify.RaveClasses.responses.FeeCheckResponse;
import ng.com.laundrify.laundrify.RaveClasses.responses.RequeryResponse;
import ng.com.laundrify.laundrify.RaveClasses.validators.AmountValidator;
import ng.com.laundrify.laundrify.RaveClasses.validators.PhoneValidator;

/**
 * Created by hfetuga on 27/06/2018.
 */

public class MpesaPresenter implements MpesaContract.UserActionsListener {
    private Context context;
    private MpesaContract.View mView;
    private AmountValidator amountValidator = new AmountValidator();
    private PhoneValidator phoneValidator = new PhoneValidator();

    public MpesaPresenter(Context context, MpesaContract.View mView) {
        this.context = context;
        this.mView = mView;
    }

    @Override
    public void fetchFee(final Payload payload) {
        FeeCheckRequestBody body = new FeeCheckRequestBody();
        body.setAmount(payload.getAmount());
        body.setCurrency(payload.getCurrency());
        body.setPtype("3");
        body.setPBFPubKey(payload.getPBFPubKey());

        mView.showProgressIndicator(true);

        new NetworkRequestImpl().getFee(body, new Callbacks.OnGetFeeRequestComplete() {
            @Override
            public void onSuccess(FeeCheckResponse response) {
                mView.showProgressIndicator(false);

                try {
                    mView.displayFee(response.getData().getCharge_amount(), payload);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    mView.showFetchFeeFailed(RaveConstants.transactionError);
                }
            }

            @Override
            public void onError(String message) {
                mView.showProgressIndicator(false);
                Log.e(RaveConstants.RAVEPAY, message);
                mView.showFetchFeeFailed(RaveConstants.transactionError);
            }
        });
    }

    @Override
    public void chargeMpesa(final Payload payload, final String encryptionKey) {
        String cardRequestBodyAsString = Utils.convertChargeRequestPayloadToJson(payload);
        String encryptedCardRequestBody = Utils.getEncryptedData(cardRequestBodyAsString, encryptionKey).trim().replaceAll("\\n", "");

//        Log.d("encrypted", encryptedCardRequestBody);

        ChargeRequestBody body = new ChargeRequestBody();
        body.setAlg("3DES-24");
        body.setPBFPubKey(payload.getPBFPubKey());
        body.setClient(encryptedCardRequestBody);

        mView.showProgressIndicator(true);

        new NetworkRequestImpl().chargeCard(body, new Callbacks.OnChargeRequestComplete() {
            @Override
            public void onSuccess(ChargeResponse response, String responseAsJSONString) {

                mView.showProgressIndicator(false);

                if (response.getData() != null) {
                    Log.d("resp", responseAsJSONString);

                    String flwRef = response.getData().getFlwRef();
                    String txRef = response.getData().getTx_ref();
                    requeryTx(flwRef, txRef, payload.getPBFPubKey());
                }
                else {
                    mView.onPaymentError(RaveConstants.noResponse);
                }

            }

            @Override
            public void onError(String message, String responseAsJSONString) {
                mView.showProgressIndicator(false);
                mView.onPaymentError(message);
            }
        });
    }



    @Override
    public void requeryTx(final String flwRef, final String txRef, final String publicKey) {

        RequeryRequestBody body = new RequeryRequestBody();
        body.setFlw_ref(flwRef);
        body.setPBFPubKey(publicKey);

        mView.showPollingIndicator(true);

        new NetworkRequestImpl().requeryTx(body, new Callbacks.OnRequeryRequestComplete() {
            @Override
            public void onSuccess(RequeryResponse response, String responseAsJSONString) {
                if (response.getData() == null) {
                    mView.onPaymentFailed(response.getStatus(), responseAsJSONString);
                }
                else if (response.getData().getChargeResponseCode().equals("02")){
                    mView.onPollingRoundComplete(flwRef, txRef, publicKey);
                }
                else if (response.getData().getChargeResponseCode().equals("00")) {
                    mView.showPollingIndicator(false);
                    mView.onPaymentSuccessful(flwRef, txRef, responseAsJSONString);
                }
                else {
                    mView.showProgressIndicator(false);
                    mView.onPaymentFailed(response.getData().getStatus(), responseAsJSONString);
                }
            }

            @Override
            public void onError(String message, String responseAsJSONString) {
                mView.onPaymentFailed(message, responseAsJSONString);
            }
        });
    }

    @Override
    public void onDataCollected(HashMap<String, ViewObject> dataHashMap) {

        boolean valid = true;

        int amountID = dataHashMap.get(RaveConstants.fieldAmount).getViewId();
        String amount = dataHashMap.get(RaveConstants.fieldAmount).getData();
        Class amountViewType = dataHashMap.get(RaveConstants.fieldAmount).getViewType();

        int phoneID = dataHashMap.get(RaveConstants.fieldPhone).getViewId();
        String phone = dataHashMap.get(RaveConstants.fieldPhone).getData();
        Class phoneViewType = dataHashMap.get(RaveConstants.fieldPhone).getViewType();


        if (!amountValidator.isAmountValid(amount)) {
            valid = false;
            mView.showFieldError(amountID, RaveConstants.validAmountPrompt, amountViewType);
        }

        if (!phoneValidator.isPhoneValid(phone)) {
            valid = false;
            mView.showFieldError(phoneID, RaveConstants.validPhonePrompt, phoneViewType);
        }

        if (valid) {
            mView.onValidationSuccessful(dataHashMap);
        }

    }

    @Override
    public void processTransaction(HashMap<String, ViewObject> dataHashMap, RavePayInitializer ravePayInitializer) {

        if (ravePayInitializer!=null) {

            PayloadBuilder builder = new PayloadBuilder();
            builder.setAmount(ravePayInitializer.getAmount() + "")
                    .setCountry(ravePayInitializer.getCountry())
                    .setCurrency(ravePayInitializer.getCurrency())
                    .setEmail(ravePayInitializer.getEmail())
                    .setFirstname(ravePayInitializer.getfName())
                    .setLastname(ravePayInitializer.getlName())
                    .setIP(Utils.getDeviceImei(context))
                    .setTxRef(ravePayInitializer.getTxRef())
                    .setMeta(ravePayInitializer.getMeta())
                    .setSubAccount(ravePayInitializer.getSubAccount())
                    .setPhonenumber(dataHashMap.get(RaveConstants.fieldAmount).getData())
                    .setPBFPubKey(ravePayInitializer.getPublicKey())
                    .setIsPreAuth(ravePayInitializer.getIsPreAuth())
                    .setDevice_fingerprint(Utils.getDeviceImei(context));

            if (ravePayInitializer.getPayment_plan() != null) {
                builder.setPaymentPlan(ravePayInitializer.getPayment_plan());
            }

            Payload body = builder.createMpesaPayload();

            if (ravePayInitializer.getIsDisplayFee()) {
                fetchFee(body);
            } else {
                chargeMpesa(body, ravePayInitializer.getEncryptionKey());
            }
        }
    }

    @Override
    public void init(RavePayInitializer ravePayInitializer) {

        if (ravePayInitializer!=null) {

            boolean isAmountValid = amountValidator.isAmountValid(ravePayInitializer.getAmount());
            if (isAmountValid) {
                mView.onAmountValidationSuccessful(String.valueOf(ravePayInitializer.getAmount()));
            }
        }
    }
}
