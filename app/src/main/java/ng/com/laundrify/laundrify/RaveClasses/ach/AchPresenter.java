package ng.com.laundrify.laundrify.RaveClasses.ach;

import android.content.Context;

import ng.com.laundrify.laundrify.R;
import ng.com.laundrify.laundrify.RaveClasses.Payload;
import ng.com.laundrify.laundrify.RaveClasses.PayloadBuilder;
import ng.com.laundrify.laundrify.RaveClasses.RaveConstants;
import ng.com.laundrify.laundrify.RaveClasses.RavePayInitializer;
import ng.com.laundrify.laundrify.RaveClasses.Utils;
import ng.com.laundrify.laundrify.RaveClasses.card.ChargeRequestBody;
import ng.com.laundrify.laundrify.RaveClasses.data.Callbacks;
import ng.com.laundrify.laundrify.RaveClasses.data.NetworkRequestImpl;
import ng.com.laundrify.laundrify.RaveClasses.data.RequeryRequestBody;
import ng.com.laundrify.laundrify.RaveClasses.data.SharedPrefsRequestImpl;
import ng.com.laundrify.laundrify.RaveClasses.responses.ChargeResponse;
import ng.com.laundrify.laundrify.RaveClasses.responses.RequeryResponse;


public class AchPresenter implements AchContract.UserActionsListener {

    private Context context;
    private AchContract.View mView;
    private SharedPrefsRequestImpl sharedMgr;

    public AchPresenter(Context context, AchContract.View mView) {
        this.context = context;
        this.mView = mView;
        sharedMgr = new SharedPrefsRequestImpl(context);
    }

    @Override
    public void onStartAchPayment(RavePayInitializer ravePayInitializer) {

        if (ravePayInitializer.getAmount() > 0 || ravePayInitializer.toString().isEmpty()) {
            mView.showAmountField(false);
            mView.showRedirectMessage(true);
        }
        else {
            mView.showAmountField(true);
            mView.showRedirectMessage(false);
        }

    }

    @Override
    public void onPayButtonClicked(RavePayInitializer ravePayInitializer, String amount) {

        mView.showAmountError(null);

        if (ravePayInitializer.getAmount() > 0) {
            initiatePayment(ravePayInitializer);
        }
        else {
            try {
                double amnt = Double.parseDouble(amount);

                if (amnt <= 0) {
                    mView.showAmountError(context.getResources().getString(R.string.validAmountPrompt));
                }
                else {
                    ravePayInitializer.setAmount(amnt);
                    initiatePayment(ravePayInitializer);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                mView.showAmountError(context.getResources().getString(R.string.validAmountPrompt));
            }
        }

    }

    private void initiatePayment(RavePayInitializer ravePayInitializer) {

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
                .setPBFPubKey(ravePayInitializer.getPublicKey())
                .setIsUsBankCharge(ravePayInitializer.isWithAch())
                .setDevice_fingerprint(Utils.getDeviceImei(context));

        if (ravePayInitializer.getPayment_plan() != null) {
            builder.setPaymentPlan(ravePayInitializer.getPayment_plan());
        }

        Payload body = builder.createBankPayload();

        chargeAccount(body, ravePayInitializer.getEncryptionKey(), ravePayInitializer.getIsDisplayFee());
    }

    @Override
    public void chargeAccount(Payload payload, String encryptionKey, final boolean isDisplayFee) {

        String requestBodyAsString = Utils.convertChargeRequestPayloadToJson(payload);
        String accountRequestBody = Utils.getEncryptedData(requestBodyAsString, encryptionKey);

        final ChargeRequestBody body = new ChargeRequestBody();
        body.setAlg("3DES-24");
        body.setPBFPubKey(payload.getPBFPubKey());
        body.setClient(accountRequestBody);

        mView.showProgressIndicator(true);

        new NetworkRequestImpl().chargeCard(body, new Callbacks.OnChargeRequestComplete() {
            @Override
            public void onSuccess(ChargeResponse response, String responseAsJSONString) {

                mView.showProgressIndicator(false);

                if (response.getData() != null) {

                    if (response.getData().getAuthurl() != null) {
                        String authUrl = response.getData().getAuthurl();
                        String flwRef = response.getData().getFlwRef();
                        String chargedAmount = response.getData().getChargedAmount();
                        String currency = response.getData().getCurrency();
                        sharedMgr.saveFlwRef(flwRef);

                        if (isDisplayFee) {
                            mView.showFee(authUrl, flwRef, chargedAmount, currency);
                        }
                        else {
                            mView.showWebView(authUrl, flwRef);
                        }
                    }
                    else {
                        mView.onPaymentError(context.getResources().getString(R.string.no_authurl_was_returnedmsg));
                    }

                }
                else {
                    mView.onPaymentError(RaveConstants.no_response_data_was_returnedmsg);
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
    public void onFeeConfirmed(String authUrl, String flwRef) {
        mView.showWebView(authUrl, flwRef);
    }

    public void requeryTx(String publicKey) {
        final String flwRef = sharedMgr.fetchFlwRef();
        //todo call requery

        RequeryRequestBody body = new RequeryRequestBody();
        body.setFlw_ref(flwRef);
        body.setPBFPubKey(publicKey);

        mView.showProgressIndicator(true);

        new NetworkRequestImpl().requeryTx(body, new Callbacks.OnRequeryRequestComplete() {
            @Override
            public void onSuccess(RequeryResponse response, String responseAsJSONString) {
                mView.showProgressIndicator(false);
                mView.onRequerySuccessful(response, responseAsJSONString, flwRef);
            }

            @Override
            public void onError(String message, String responseAsJSONString) {
                mView.onPaymentFailed(message, responseAsJSONString);
            }
        });
    }

    @Override
    public void verifyRequeryResponse(RequeryResponse response, String responseAsJSONString, RavePayInitializer ravePayInitializer, String flwRef) {
        boolean wasTxSuccessful = Utils.wasTxSuccessful(ravePayInitializer, responseAsJSONString);

        if (wasTxSuccessful) {
            mView.onPaymentSuccessful(response.getStatus(), flwRef, responseAsJSONString);
        }
        else {
            mView.onPaymentFailed(response.getStatus(), responseAsJSONString);
        }
    }
}
