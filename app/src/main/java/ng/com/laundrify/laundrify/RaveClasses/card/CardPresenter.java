package ng.com.laundrify.laundrify.RaveClasses.card;

import android.content.Context;
import android.util.Log;
import android.view.View;

import java.util.HashMap;

import ng.com.laundrify.laundrify.PaymentActivity;
import ng.com.laundrify.laundrify.RaveClasses.FeeCheckRequestBody;
import ng.com.laundrify.laundrify.RaveClasses.Payload;
import ng.com.laundrify.laundrify.RaveClasses.PayloadBuilder;
import ng.com.laundrify.laundrify.RaveClasses.RaveConstants;
import ng.com.laundrify.laundrify.RaveClasses.RavePayInitializer;
import ng.com.laundrify.laundrify.RaveClasses.Utils;
import ng.com.laundrify.laundrify.RaveClasses.ViewObject;
import ng.com.laundrify.laundrify.RaveClasses.data.Callbacks;
import ng.com.laundrify.laundrify.RaveClasses.data.NetworkRequestImpl;
import ng.com.laundrify.laundrify.RaveClasses.data.RequeryRequestBody;
import ng.com.laundrify.laundrify.RaveClasses.data.RequeryRequestBodyv2;
import ng.com.laundrify.laundrify.RaveClasses.data.ValidateChargeBody;
import ng.com.laundrify.laundrify.RaveClasses.responses.ChargeResponse;
import ng.com.laundrify.laundrify.RaveClasses.responses.FeeCheckResponse;
import ng.com.laundrify.laundrify.RaveClasses.responses.RequeryResponse;
import ng.com.laundrify.laundrify.RaveClasses.responses.RequeryResponsev2;
import ng.com.laundrify.laundrify.RaveClasses.validators.AmountValidator;
import ng.com.laundrify.laundrify.RaveClasses.validators.CardExpiryValidator;
import ng.com.laundrify.laundrify.RaveClasses.validators.CardNoValidator;
import ng.com.laundrify.laundrify.RaveClasses.validators.CvvValidator;
import ng.com.laundrify.laundrify.RaveClasses.validators.EmailValidator;

import static ng.com.laundrify.laundrify.RaveClasses.RaveConstants.AVS_VBVSECURECODE;
import static ng.com.laundrify.laundrify.RaveClasses.RaveConstants.PIN;
import static ng.com.laundrify.laundrify.RaveClasses.card.CardFragment.ravePayInitializer;

/**
 * Created by hamzafetuga on 18/07/2017.
 */

public class CardPresenter implements CardContract.UserActionsListener {

    private Context context;
    private CardContract.View mView;
    private EmailValidator emailValidator = new EmailValidator();
    private AmountValidator amountValidator = new AmountValidator();
    private CvvValidator cvvValidator = new CvvValidator();
    private CardExpiryValidator cardExpiryValidator = new CardExpiryValidator();
    private CardNoValidator cardNoValidator = new CardNoValidator();

     CardPresenter(Context context, CardContract.View mView) {
        this.context = context;
        this.mView = mView;
    }

    //5.1
    //Initiate Payment
    @Override
    public void chargeCard(final Payload payload, final String encryptionKey) {

        String cardRequestBodyAsString = Utils.convertChargeRequestPayloadToJson(payload);
        String encryptedCardRequestBody = Utils.getEncryptedData(cardRequestBodyAsString, encryptionKey);

        final ChargeRequestBody body = new ChargeRequestBody();
        body.setAlg("3DES-24");
        body.setPBFPubKey(payload.getPBFPubKey());
        body.setClient(encryptedCardRequestBody);

        mView.showProgressIndicator(true);

        //5.2
        new NetworkRequestImpl().chargeCard(body, new Callbacks.OnChargeRequestComplete() {
            @Override
            public void onSuccess(ChargeResponse response, String responseAsJSONString) {

                mView.showProgressIndicator(false);

                if (response.getData() != null) {

                    //5.3
                    //If suggested auth
                    if (response.getData().getSuggested_auth() != null) {
                        String suggested_auth = response.getData().getSuggested_auth();

                        //6.1
                        //Get the suggested Auth
                        if (suggested_auth.equals(PIN)) {

                            //if suggested Pin
                            mView.onPinAuthModelSuggested(payload);
                        } else if (suggested_auth.equals(AVS_VBVSECURECODE)) {

                            // If suggested Address verification then verification by visa
                            mView.onAVS_VBVSECURECODEModelSuggested(payload);
                        } else if (suggested_auth.equalsIgnoreCase(RaveConstants.NOAUTH_INTERNATIONAL)) {

                            // If no Authentication for international card
                            mView.onNoAuthInternationalSuggested(payload);
                        } else {
                            mView.onPaymentError("Unknown auth model");
                        }
                    } else {

                        //Second request after Authenticating with suggetedAuth
                        // let's get the charge response and roll!
                        String authModelUsed = response.getData().getAuthModelUsed();

                        Log.i("test", "freshdata " + response.getData().toString());
                        if (authModelUsed != null) {
                            if (authModelUsed.equalsIgnoreCase(RaveConstants.VBV)) {

                                //Load lame Uri
                                String authUrlCrude = response.getData().getAuthurl();
                                String flwRef = response.getData().getFlwRef();
                                mView.onVBVAuthModelUsed(authUrlCrude, flwRef);

                                // If requires OTP deal with it again
                            } else if (authModelUsed.equalsIgnoreCase(RaveConstants.GTB_OTP)
                                    ||  authModelUsed.equalsIgnoreCase(RaveConstants.ACCESS_OTP)
                                    || authModelUsed.toLowerCase().contains("otp")) {
                                String flwRef = response.getData().getFlwRef();
                                String chargeResponseMessage = response.getData().getChargeResponseMessage();
                                chargeResponseMessage = chargeResponseMessage == null ? "Enter your one  time password (OTP)" : chargeResponseMessage;
                                mView.showOTPLayout(flwRef, chargeResponseMessage);

                                // No Auth required yay!
                            } else if (authModelUsed.equalsIgnoreCase(RaveConstants.NOAUTH)) {
                                String flwRef = response.getData().getFlwRef();
                                mView.onNoAuthUsed(flwRef, payload.getPBFPubKey());
                            }
                        }
                    }
                } else {
                    mView.onPaymentError("No response data was returned");
                }
            }

            @Override
            public void onError(String message, String responseAsJSONString) {
                //Log.i("test", message);
                mView.showProgressIndicator(false);
                mView.onPaymentError(message);
            }
        });
    }

    //6.4
    @Override
    public void chargeCardWithAVSModel(Payload payload, String address, String city, String zipCode, String country, String state, String authModel, String encryptionKey) {

        payload.setSuggestedAuth(authModel);
        payload.setBillingaddress(address);
        payload.setBillingcity(city);
        payload.setBillingzip(zipCode);
        payload.setBillingcountry(country);
        payload.setBillingstate(state);

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

                if (response.getData() != null && response.getData().getChargeResponseCode() != null) {
                    String chargeResponseCode = response.getData().getChargeResponseCode();

                    if (chargeResponseCode.equalsIgnoreCase("00")) {
//                        mView.showToast("Payment successful");
                        mView.onChargeCardSuccessful(response);
                    } else if (chargeResponseCode.equalsIgnoreCase("02")) {
                        String authModelUsed = response.getData().getAuthModelUsed();
                        if (authModelUsed.equalsIgnoreCase(PIN)) {
                            String flwRef = response.getData().getFlwRef();
                            String chargeResponseMessage = response.getData().getChargeResponseMessage();
                            chargeResponseMessage = (chargeResponseMessage == null || chargeResponseMessage.length() == 0) ? "Enter your one  time password (OTP)" : chargeResponseMessage;
                            mView.showOTPLayout(flwRef, chargeResponseMessage);
                        } else if (authModelUsed.equalsIgnoreCase(RaveConstants.VBV)){
                            String flwRef = response.getData().getFlwRef();
                            mView.onAVSVBVSecureCodeModelUsed(response.getData().getAuthurl(), flwRef);
                        } else {
                            mView.onPaymentError("Unknown Auth Model");
                        }
                    } else {
                        mView.onPaymentError("Unknown charge response code");
                    }
                } else {
                    mView.onPaymentError("Invalid charge response code");
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
    public void
    onDataCollected(HashMap<String, ViewObject> dataHashMap) {

        boolean valid = true;

        int amountID = dataHashMap.get(RaveConstants.fieldAmount).getViewId();
        String amount = dataHashMap.get(RaveConstants.fieldAmount).getData();
        Class amountViewType = dataHashMap.get(RaveConstants.fieldAmount).getViewType();

        int emailID = dataHashMap.get(RaveConstants.fieldEmail).getViewId();
        String email = dataHashMap.get(RaveConstants.fieldEmail).getData();
        Class emailViewType = dataHashMap.get(RaveConstants.fieldEmail).getViewType();

        int cvvID = dataHashMap.get(RaveConstants.fieldCvv).getViewId();
        String cvv = dataHashMap.get(RaveConstants.fieldCvv).getData();
        Class cvvViewType = dataHashMap.get(RaveConstants.fieldCvv).getViewType();

        int cardExpiryID = dataHashMap.get(RaveConstants.fieldCardExpiry).getViewId();
        String cardExpiry = dataHashMap.get(RaveConstants.fieldCardExpiry).getData();
        Class cardExpiryViewType = dataHashMap.get(RaveConstants.fieldCardExpiry).getViewType();

        int cardNoStrippedID = dataHashMap.get(RaveConstants.fieldcardNoStripped).getViewId();
        String cardNoStripped = dataHashMap.get(RaveConstants.fieldcardNoStripped).getData().replaceAll(" ", "");
        dataHashMap.get(RaveConstants.fieldcardNoStripped).setData(cardNoStripped);

        Class cardNoStrippedViewType = dataHashMap.get(RaveConstants.fieldcardNoStripped).getViewType();

             try{
                 boolean isAmountValidated = amountValidator.isAmountValid(amount);
                 if (!isAmountValidated) {
                        valid = false; mView.showFieldError(amountID, RaveConstants.validAmountPrompt, amountViewType);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    valid = false; mView.showFieldError(amountID, RaveConstants.validAmountPrompt, amountViewType);
                }

        boolean isEmailValidated = emailValidator.isEmailValid(email);
                if (!isEmailValidated) {
                    valid = false; mView.showFieldError(emailID, RaveConstants.validPhonePrompt, emailViewType);
                }

        boolean isCVVValidated = cvvValidator.isCvvValid(cvv);
                if (!isCVVValidated) {
                    valid = false; mView.showFieldError(cvvID, RaveConstants.validCvvPrompt, cvvViewType);
                }

        boolean isCardExpiryValidated = cardExpiryValidator.isCardExpiryValid(cardExpiry);

                if (!isCardExpiryValidated) {
                    valid = false;  mView.showFieldError(cardExpiryID, RaveConstants.validExpiryDatePrompt, cardExpiryViewType);
                }

        boolean isCardNoValidator = cardNoValidator.isCardNoStrippedValid(cardNoStripped);
                if (!isCardNoValidator) {
                    valid = false; mView.showFieldError(cardNoStrippedID, RaveConstants.validCreditCardPrompt, cardNoStrippedViewType);
                }

                if (valid) {
                    mView.onValidationSuccessful(dataHashMap);
                }

    }

    //4.2
    @Override
    public void processTransaction(HashMap<String, ViewObject> dataHashMap, RavePayInitializer ravePayInitializer) {

        if (ravePayInitializer!=null) {

            ravePayInitializer.setAmount(Double.parseDouble(dataHashMap.get(RaveConstants.fieldAmount).getData()));

            PayloadBuilder builder = new PayloadBuilder();
            builder.setAmount(ravePayInitializer.getAmount() + "")
                    .setCardno(dataHashMap.get(RaveConstants.fieldcardNoStripped).getData())
                    .setCountry(ravePayInitializer.getCountry())
                    .setCurrency(ravePayInitializer.getCurrency())
                    .setCvv(dataHashMap.get(RaveConstants.fieldCvv).getData())
                    .setEmail(dataHashMap.get(RaveConstants.fieldEmail).getData())
                    .setFirstname(ravePayInitializer.getfName())
                    .setLastname(ravePayInitializer.getlName())
                    .setIP(Utils.getDeviceImei(context))
                    .setTxRef(ravePayInitializer.getTxRef())
                    .setExpiryyear(dataHashMap.get(RaveConstants.fieldCardExpiry).getData().substring(3, 5))
                    .setExpirymonth(dataHashMap.get(RaveConstants.fieldCardExpiry).getData().substring(0, 2))
                    .setMeta(ravePayInitializer.getMeta())
                    .setSubAccount(ravePayInitializer.getSubAccount())
                    .setIsPreAuth(ravePayInitializer.getIsPreAuth())
                    .setPBFPubKey(ravePayInitializer.getPublicKey())
                    .setDevice_fingerprint(Utils.getDeviceImei(context));


            if (ravePayInitializer.getPayment_plan() != null) {
                builder.setPaymentPlan(ravePayInitializer.getPayment_plan());
            }

            Payload body = builder.createPayload();

            Log.i("test", "Body is..." + (body));

            //4.3
            if (ravePayInitializer.getIsDisplayFee()) {

                //Fetch display fee, and a reason for the charges so it can be processed into Token request or Card Request
                fetchFee(body, RaveConstants.MANUAL_CARD_CHARGE);
                //fetchFee(body, RaveConstants.TOKEN_CHARGE);
            } else {
                chargeCard(body, ravePayInitializer.getEncryptionKey());
            }
        }
    }

    //6.2
    //Make second request
    @Override
    public void chargeCardWithSuggestedAuthModel(Payload payload, String zipOrPin, String authModel, String encryptionKey) {

        if (authModel.equalsIgnoreCase(AVS_VBVSECURECODE)) {
            payload.setBillingzip(zipOrPin);
        } else if (authModel.equalsIgnoreCase(PIN)){
            payload.setPin(zipOrPin);
        }

        payload.setSuggestedAuth(authModel);

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

                if (response.getData() != null && response.getData().getChargeResponseCode() != null) {
                    String chargeResponseCode = response.getData().getChargeResponseCode();

                    if (chargeResponseCode.equalsIgnoreCase("00")) {
//                        mView.showToast("Payment successful");
                        mView.onChargeCardSuccessful(response);
                    } else if (chargeResponseCode.equalsIgnoreCase("02")) {
                        String authModelUsed = response.getData().getAuthModelUsed();
                        if (authModelUsed.equalsIgnoreCase(PIN)) {
                            String flwRef = response.getData().getFlwRef();
                            String chargeResponseMessage = response.getData().getChargeResponseMessage();
                            chargeResponseMessage = (chargeResponseMessage == null || chargeResponseMessage.length() == 0) ? "Enter your one  time password (OTP)" : chargeResponseMessage;
                            mView.showOTPLayout(flwRef, chargeResponseMessage);
                        } else if (authModelUsed.equalsIgnoreCase(AVS_VBVSECURECODE)){
                            String flwRef = response.getData().getFlwRef();
                            mView.onAVSVBVSecureCodeModelUsed(response.getData().getAuthurl(), flwRef);
                        } else {
                            mView.onPaymentError(RaveConstants.unknownAuthmsg);
                        }
                    } else {
                        mView.onPaymentError(RaveConstants.unknownResCodemsg);
                    }
                } else {
                    mView.onPaymentError(RaveConstants.invalidChargeCode);
                }

            }

            @Override
            public void onError(String message, String responseAsJSONString) {
                mView.showProgressIndicator(false);
                mView.onPaymentError(message);
            }
        });

    }

    //6.6
    //Validate the payment
    @Override
    public void validateCardCharge(final String flwRef, String otp, String PBFPubKey) {

        ValidateChargeBody body = new ValidateChargeBody();
        body.setPBFPubKey(PBFPubKey);
        body.setOtp(otp);
        body.setTransaction_reference(flwRef);
        body.setTransactionreference(flwRef);
        Log.i("test", "validatecharge: " + flwRef);
        Log.i("test", "validatecharge: " + otp);
        Log.i("test", "validatecharge: " + PBFPubKey);

        mView.showProgressIndicator(true);

        new NetworkRequestImpl().validateChargeCard(body, new Callbacks.OnValidateChargeCardRequestComplete() {
            @Override
            public void onSuccess(ChargeResponse response, String responseAsJSONString) {
                mView.showProgressIndicator(false);

                if (response.getStatus() != null) {
                    String status = response.getStatus();
                    String message = response.getMessage();

                    if (status.equalsIgnoreCase(RaveConstants.success)) {
                        mView.onValidateSuccessful(status, responseAsJSONString);
                    } else {
                        mView.onValidateError(message);
                    }
                } else {
                    mView.onValidateCardChargeFailed(flwRef, responseAsJSONString);
                }
            }

            @Override
            public void onError(String message, String responseAsJSONString) {
                mView.showProgressIndicator(false);
                mView.onPaymentError(message);
            }
        });

    }

    /*
    //10.1
    //Verify the payment
    @Override
    //--data '{"txref":"MC-1520443531487","SECKEY":"FLWSECK-e6db11d1f8a6208de8cb2f94e293450e-X"}'
    public void requeryTx(final String flwRef, final String publicKey, final boolean shouldISaveCard) {

        RequeryRequestBody body = new RequeryRequestBody();
        body.setFlw_ref(flwRef);
        body.setPBFPubKey(publicKey);

        Log.i("test" , "Verified with freshrequest1, \n Flwref:  " + flwRef + "\n publicKey: " + publicKey);

        mView.showProgressIndicator(true);

        new NetworkRequestImpl().requeryTx(body, new Callbacks.OnRequeryRequestComplete() {
            @Override
            public void onSuccess(RequeryResponse response, String responseAsJSONString) {
                Log.i("test", "REquery :" + responseAsJSONString);
                Log.i("test", "REqueryresp :" + response);
                PaymentActivity.rawResponse = responseAsJSONString;
                mView.showProgressIndicator(false);
                mView.onRequerySuccessful(response, responseAsJSONString, flwRef);
            }

            @Override
            public void onError(String message, String responseAsJSONString) {
                mView.onPaymentFailed(message, responseAsJSONString);
            }
        });
    }
    */

    public void requeryTxv2(final String txref, final String secretKey) {

        RequeryRequestBodyv2 body = new RequeryRequestBodyv2();
        body.setTxref(txref);
        body.setSECKEY(secretKey);

        Log.i("test" , "Verified with request2, \n Flwref:  " + txref + "\n publicKey: " + secretKey);

        mView.showProgressIndicator(true);

        new NetworkRequestImpl().requeryTxv2(body, new Callbacks.OnRequeryRequestv2Complete() {
            @Override
            public void onSuccess(RequeryResponsev2 response, String responseAsJSONString) {
                Log.i("test", "2nd Requery :" + responseAsJSONString);
                mView.showProgressIndicator(false);

                boolean wasTxSuccessful = Utils.wasCardTxSuccessful(ravePayInitializer, responseAsJSONString);

                if (wasTxSuccessful) {
                    mView.onPaymentSuccessful(response.getStatus(), ravePayInitializer.getTxRef(), responseAsJSONString);
                } else {
                    mView.onPaymentFailed(response.getStatus(), responseAsJSONString);
                }
            }

            @Override
            public void onError(String message, String responseAsJSONString) {
                mView.showProgressIndicator(false);
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

    // 4.3
    @Override
    public void fetchFee(final Payload payload, final int reason) {

        FeeCheckRequestBody body = new FeeCheckRequestBody();
        body.setAmount(payload.getAmount());
        body.setCurrency(payload.getCurrency());
        body.setPBFPubKey(payload.getPBFPubKey());

        if (payload.getCardno() == null || payload.getCardno().length() == 0) {
            body.setCard6(payload.getCardBIN());
        }
        else  {
            body.setCard6(payload.getCardno().substring(0, 6));
        }

        mView.showProgressIndicator(true);

        new NetworkRequestImpl().getFee(body, new Callbacks.OnGetFeeRequestComplete() {
            @Override
            public void onSuccess(FeeCheckResponse response) {
                mView.showProgressIndicator(false);

                //4.4
                try {
                    mView.displayFee(response.getData().getCharge_amount(), payload, reason);
                } catch (Exception e) {
                    e.printStackTrace();
                    mView.showFetchFeeFailed("An error occurred while retrieving transaction fee");
                }
             }

            @Override
            public void onError(String message) {
                mView.showProgressIndicator(false);
                Log.e(RaveConstants.RAVEPAY, message);
                mView.showFetchFeeFailed("An error occurred while retrieving transaction fee");
            }
        });

    }

    //10.2
    @Override
    public void chargeToken(TokenChargeBody payload) {

        //TokenChargeBody body = new TokenChargeBody();
        //body.setAmount("100");
        //body.setCountry("NGN");
        //body.setCurrency("NG");
        //body.setEmail("sheaboy66@gmail.com");
        //body.setFirstname("SeyiCorect");
        //body.setIP(Utils.getDeviceImei(context));
        //body.setLastname("Ipaye");
        //body.setSECKEY("secKEy");
        //body.setTxRef("TK Correct");

        mView.showProgressIndicator(true);

        new NetworkRequestImpl().chargeToken(payload, new Callbacks.OnChargeRequestComplete() {
            @Override
            public void onSuccess(ChargeResponse response, String responseAsJSONString) {

                mView.showProgressIndicator(false);
                mView.onChargeTokenComplete(response);
                Log.i("test","Token Charge complete");

            }

            @Override
            public void onError(String message, String responseAsJSONString) {
                mView.showProgressIndicator(false);

                Log.i("Test", "Token gives " + responseAsJSONString);

                if (responseAsJSONString.contains(RaveConstants.tokenNotFound)) {
                    mView.onPaymentError(RaveConstants.tokenNotFound);
                }
                else if (responseAsJSONString.contains(RaveConstants.expired)) {
                    mView.onPaymentError(RaveConstants.tokenExpired);
                }
                else {
                    mView.onPaymentError(message);
                }


            }
        });

    }

    @Override
    public void onDetachView() {
        this.mView = new NullCardView();
    }

    @Override
    public void onAttachView(CardContract.View view) {
        this.mView = view;
    }

    @Override
    public void init(RavePayInitializer ravePayInitializer) {

        if (ravePayInitializer != null) {

            //Build ravePay model
            boolean isEmailValid = emailValidator.isEmailValid(ravePayInitializer.getEmail());
            boolean isAmountValid = amountValidator.isAmountValid(ravePayInitializer.getAmount());
            if (isEmailValid) {
                mView.onEmailValidated(ravePayInitializer.getEmail(), View.GONE);
            } else {
                mView.onEmailValidated("", View.VISIBLE);
            }

            if (isAmountValid) {
                mView.onAmountValidated(String.valueOf(ravePayInitializer.getAmount()), View.GONE);
            } else {
                mView.onAmountValidated("", View.VISIBLE);
            }
        }
    }
}
