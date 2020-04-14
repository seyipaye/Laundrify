package ng.com.laundrify.laundrify.RaveClasses.data;

import java.util.List;

import ng.com.laundrify.laundrify.RaveClasses.FeeCheckRequestBody;
import ng.com.laundrify.laundrify.RaveClasses.card.ChargeRequestBody;
import ng.com.laundrify.laundrify.RaveClasses.card.TokenChargeBody;

/**
 * Created by hamzafetuga on 18/07/2017.
 */

public interface DataRequest {

    interface NetworkRequest {
        void chargeCard(ChargeRequestBody chargeRequestBody, Callbacks.OnChargeRequestComplete callback);
        void chargeGhanaMobileMoneyWallet(ChargeRequestBody chargeRequestBody, Callbacks.OnGhanaChargeRequestComplete callback);
        void validateChargeCard(ValidateChargeBody cardRequestBody, Callbacks.OnValidateChargeCardRequestComplete callback);
        void validateAccountCard(ValidateChargeBody cardRequestBody, Callbacks.OnValidateChargeCardRequestComplete callback);
        void requeryTx(RequeryRequestBody requeryRequestBody, Callbacks.OnRequeryRequestComplete callback);
        void requeryTxv2(RequeryRequestBodyv2 requeryRequestBody, Callbacks.OnRequeryRequestv2Complete callback);
        void getBanks(Callbacks.OnGetBanksRequestComplete callback);
        void chargeAccount(ChargeRequestBody accountRequestBody, Callbacks.OnChargeRequestComplete callback);
        void chargeToken(TokenChargeBody payload, Callbacks.OnChargeRequestComplete callback);
        void getFee(FeeCheckRequestBody body, Callbacks.OnGetFeeRequestComplete callback);
    }

    interface SharedPrefsRequest {
        void saveCardDetsToSave(CardDetsToSave cardDetsToSave);
        CardDetsToSave retrieveCardDetsToSave();
        void saveACard(SavedCard card, String SECKEY, String email);
        List<SavedCard> getSavedCards(String email);
        void saveFlwRef(String flwRef);
        String fetchFlwRef();
    }
}
