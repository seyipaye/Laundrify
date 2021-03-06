package ng.com.laundrify.laundrify.RaveClasses;

import org.parceler.Parcel;

/**
 * Created by hamzafetuga on 21/07/2017.
 */

@Parcel
public class RavePayInitializer {

    // Token
    String secretKey;
    public boolean withToken;
    public String card6;
    public String token;

    String email;
    double amount;
    String publicKey;
    String encryptionKey;
    String txRef;
    String narration;
    String currency;
    String country;
    String fName;
    String lName;
    String meta;
    String subAccounts;
    String payment_plan;
    boolean withAch = false;
    boolean withMpesa = false;
    boolean withCard = true;
    boolean withAccount = true;
    boolean withGHMobileMoney = false;
    boolean withUgMobileMoney = false;
    int theme;
    boolean staging = true;
    boolean isPreAuth = false;
    boolean displayFee = true;
    boolean showStagingLabel;

    public RavePayInitializer(String email, double amount, String publicKey,
                              String encryptionKey, String txRef, String narration,
                              String currency, String country, String fName,
                              String lName, boolean withCard,
                              boolean withAccount, boolean withMpesa, boolean withGHMobileMoney,
                              boolean withUgMobileMoney,
                              boolean withAch, int theme,
                              boolean staging, String meta, String subAccounts, String payment_plan, boolean isPreAuth,
                              boolean showStagingLabel,
                              boolean displayFee, boolean withToken, String secretKey, String token, String card6) {
        this.email = email;
        this.amount = amount;
        this.publicKey = publicKey;
        this.encryptionKey = encryptionKey;
        this.txRef = txRef;
        this.narration = narration;
        this.currency = currency;
        this.country = country;
        this.fName = fName;
        this.lName = lName;
        this.withAccount = withAccount;
        this.withGHMobileMoney = withGHMobileMoney;
        this.withUgMobileMoney = withUgMobileMoney;
        this.withMpesa = withMpesa;
        this.withCard = withCard;
        this.withAch = withAch;
        this.theme = theme;
        this.staging = staging;
        this.meta = meta;
        this.subAccounts = subAccounts;
        this.payment_plan = payment_plan;
        this.isPreAuth = isPreAuth;
        this.showStagingLabel = showStagingLabel;
        this.displayFee = displayFee;

        //Token
        this.withToken = withToken;
        this.secretKey = secretKey;
        this.card6 = card6;
        this.token = token;

    }

    public RavePayInitializer() {
    }

    public boolean getShowStagingLabel() {
        return showStagingLabel;
    }

    public void showStagingLabel(boolean showStagingLabel) {
        this.showStagingLabel = showStagingLabel;
    }

    public boolean isWithMpesa() {
        return withMpesa;
    }

    public void setWithMpesa(boolean withMpesa) {
        this.withMpesa = withMpesa;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public String getSubAccount(){return subAccounts;}

    public void setSubAccount(String subAccounts){this.subAccounts=subAccounts;}

    public boolean isStaging() {
        return staging;
    }

    public void setStaging(boolean staging) {
        this.staging = staging;
    }

    public int getTheme() {
        return theme;
    }

    public void setTheme(int theme) {
        this.theme = theme;
    }

    public boolean isWithAccount() {
        return withAccount;
    }

    public void setWithAccount(boolean withAccount) {
        this.withAccount = withAccount;
    }

    public boolean isWithGHMobileMoney() {
        return withGHMobileMoney;
    }

    public boolean isWithUgMobileMoney() {
        return withUgMobileMoney;
    }

    public void setWithGHMobileMoney(boolean withGHMobileMoney) {
        this.withGHMobileMoney = withGHMobileMoney;
    }

    public boolean isWithCard() {
        return withCard;
    }

    public boolean isWithAch() { return withAch; }

    public void setWithAch(boolean withAch) {
        this.withAch = withAch;
    }

    public void setWithCard(boolean withCard) {
        this.withCard = withCard;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getTxRef() {
        return txRef;
    }

    public void setTxRef(String txRef) {
        this.txRef = txRef;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getPayment_plan() {
        return payment_plan;
    }

    public void setPayment_plan(String payment_plan) {
        this.payment_plan = payment_plan;
    }

    public void setPreAuth(boolean isPreAuth) {
        this.isPreAuth = isPreAuth;
    }

    public boolean getIsPreAuth() { return  isPreAuth; }

    public boolean getIsDisplayFee() {
        return displayFee;
    }
    public void setIsDisplayFee() {
        this.displayFee = displayFee;
    }

    //Token
    public boolean isWithToken() {
        return withToken;
    }

    public void setWithToken(boolean withToken) {
        this.withToken = withToken;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getFirst6() {
        return card6;
    }

    public void setCard6(String card6) {
        this.card6 = card6;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
