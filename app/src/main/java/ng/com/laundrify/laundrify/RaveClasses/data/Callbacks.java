package ng.com.laundrify.laundrify.RaveClasses.data;


import java.util.List;

import ng.com.laundrify.laundrify.RaveClasses.responses.ChargeResponse;
import ng.com.laundrify.laundrify.RaveClasses.responses.FeeCheckResponse;
import ng.com.laundrify.laundrify.RaveClasses.responses.GhChargeResponse;
import ng.com.laundrify.laundrify.RaveClasses.responses.RequeryResponse;
import ng.com.laundrify.laundrify.RaveClasses.responses.RequeryResponsev2;

/**
 * Created by hamzafetuga on 18/07/2017.
 */

public class Callbacks {

    public interface OnGhanaChargeRequestComplete {
        void onSuccess(GhChargeResponse response, String responseAsJSONString);
        void onError(String message, String responseAsJSONString);
    }

    public interface OnChargeRequestComplete {
        void onSuccess(ChargeResponse response, String responseAsJSONString);
        void onError(String message, String responseAsJSONString);
    }

    public interface OnValidateChargeCardRequestComplete {
        void onSuccess(ChargeResponse response, String responseAsJSONString);
        void onError(String message, String responseAsJSONString);
    }

    public interface OnRequeryRequestComplete {
        void onSuccess(RequeryResponse response, String responseAsJSONString);
        void onError(String message, String responseAsJSONString);
    }

    public interface OnRequeryRequestv2Complete {
        void onSuccess(RequeryResponsev2 response, String responseAsJSONString);
        void onError(String message, String responseAsJSONString);
    }

    public interface OnGetBanksRequestComplete {
        void onSuccess(List<Bank> banks);
        void onError(String message);
    }

    public interface BankSelectedListener {
        void onBankSelected(Bank b);
    }

    public interface SavedCardSelectedListener {
        void onCardSelected(SavedCard savedCard);
    }

    public interface OnGetFeeRequestComplete {
        void onSuccess(FeeCheckResponse response);
        void onError(String message);
    }
}
