package ng.com.laundrify.laundrify.RaveClasses;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import org.parceler.Parcels;

import java.util.List;

import ng.com.laundrify.laundrify.R;
import ng.com.laundrify.laundrify.RaveClasses.responses.SubAccount;

import static ng.com.laundrify.laundrify.RaveClasses.RaveConstants.RAVEPAY;
import static ng.com.laundrify.laundrify.RaveClasses.RaveConstants.RAVE_PARAMS;
import static ng.com.laundrify.laundrify.RaveClasses.RaveConstants.RAVE_REQUEST_CODE;

public class RavePayManager {
    private String email;
    private double amount = -1;
    private String publicKey;
    private String encryptionKey;
    private String txRef;
    private String narration = "";
    private String currency = "NGN";
    private String country = "NG";
    private String fName = "";
    private String lName = "";
    private String meta = "";
    private String subAccounts = "";
    private String payment_plan;
    private Activity activity;
    boolean withCard = true;
    boolean withAccount = true;
    boolean withAch = false;
    boolean withMpesa = false;
    boolean withGHMobileMoney = false;
    boolean withUgMobileMoney = false;
    private int theme = R.style.DefaultTheme;
    boolean staging = true;
    boolean allowSaveCard = true;
    boolean isPreAuth =  false;
    boolean showStagingLabel = true;
    boolean displayFee = true;
    private boolean withToken = false;
    private String secretKey;
    private String firstCard6;
    private String token;

    public RavePayManager allowSaveCardFeature(boolean allowSaveCard) {
        this.allowSaveCard = allowSaveCard;
        return this;
    }

    public RavePayManager onStagingEnv(boolean staging) {
        this.staging = staging;
        return this;
    }

    public RavePayManager withTheme(int theme) {
        this.theme = theme;
        return this;
    }

    public RavePayManager(Activity activity) {
        this.activity = activity;
    }

    public RavePayManager acceptAchPayments(boolean withAch) {
        this.withAch = withAch;
        return this;
    }

    public RavePayManager acceptCardPayments(boolean withCard) {
        this.withCard = withCard;
        return this;
    }

    public RavePayManager acceptMpesaPayments(boolean withMpesa) {
        this.withMpesa = withMpesa;
        return this;
    }

    public RavePayManager acceptAccountPayments(boolean withAccount) {
        this.withAccount = withAccount;
        return this;
    }

    public RavePayManager acceptGHMobileMoneyPayments(boolean withGHMobileMoney) {
        this.withGHMobileMoney = withGHMobileMoney;
        return this;
    }

    public RavePayManager acceptUgMobileMoneyPayments(boolean withUgMobileMoney) {
        this.withUgMobileMoney = withUgMobileMoney;
        return this;
    }

    public RavePayManager isPreAuth(boolean isPreAuth){
        this.isPreAuth = isPreAuth;
        return this;
    }

    public RavePayManager setMeta(List<Meta> meta) {
        this.meta = Utils.stringifyMeta(meta);
        return this;
    }

    public RavePayManager setSubAccounts(List<SubAccount> subAccounts){
        this.subAccounts = Utils.stringifySubaccounts(subAccounts);
        return this;
    }

    public RavePayManager showStagingLabel(boolean showStagingLabel) {
        this.showStagingLabel = showStagingLabel;
        return this;
    }


    public RavePayManager setEmail(String email) {
        this.email = email;
        return this;
    }

    public RavePayManager setAmount(double amount) {
        if (amount != 0) {
            this.amount = amount;
        }
        return this;
    }

    public RavePayManager setPublicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public RavePayManager setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
        return this;
    }

    public RavePayManager setTxRef(String txRef) {
        this.txRef = txRef;
        return this;
    }

    public RavePayManager setNarration(String narration) {
        this.narration = narration;
        return this;
    }

    public RavePayManager setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public RavePayManager setCountry(String country) {
        this.country = country;
        return this;
    }

    public RavePayManager setfName(String fName) {
        this.fName = fName;
        return this;
    }

    public RavePayManager setlName(String lName) {
        this.lName = lName;
        return this;
    }

    public RavePayManager setPaymentPlan(String payment_plan) {
        this.payment_plan = payment_plan;
        return this;
    }

    //Token



    public void initialize() {
        if (activity != null) {

            Intent intent = new Intent(activity, RavePayActivity.class);
            intent.putExtra(RAVE_PARAMS, Parcels.wrap(createRavePayInitializer()));
            activity.startActivityForResult(intent, RAVE_REQUEST_CODE) ;
        } else {
                Log.d(RAVEPAY, "Context is required!");
        }
    }

    public RavePayManager shouldDisplayFee(boolean displayFee) {
        this.displayFee = displayFee;
        return this;
    }

    public RavePayInitializer createRavePayInitializer() {
        return new RavePayInitializer(email, amount, publicKey, encryptionKey, txRef, narration,
                currency, country, fName, lName, withCard, withAccount, withMpesa,
                withGHMobileMoney, withUgMobileMoney, withAch, theme, staging, meta, subAccounts,
                payment_plan,
                isPreAuth, showStagingLabel,displayFee, withToken, secretKey, token, firstCard6);
       }

       //Token
    public RavePayManager withToken(boolean useToken) {
        this.withToken = useToken;
        return this;
    }

    public RavePayManager setSecretKey (String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public RavePayManager setcard6 (String card6) {
        this.firstCard6 = card6;
        return this;
    }

    public RavePayManager setToken(String token) {
        this.token = token;
        return this;
    }
}