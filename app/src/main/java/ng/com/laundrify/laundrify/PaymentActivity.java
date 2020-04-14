package ng.com.laundrify.laundrify;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ng.com.laundrify.laundrify.RaveClasses.RaveConstants;
import ng.com.laundrify.laundrify.RaveClasses.RavePayActivity;
import ng.com.laundrify.laundrify.RaveClasses.RavePayManager;
import ng.com.laundrify.laundrify.utils.CardsModel;
import ng.com.laundrify.laundrify.utils.PromoLayoutModel;
import ng.com.laundrify.laundrify.utils.PromotionsModel;
import ng.com.laundrify.laundrify.utils.SendNotification;

public class PaymentActivity extends AppCompatActivity {

    String firstName, lastName, publicKey, encryptionKey, userEmail, orderId, agent, dcKey, secretKey;
    double promoPrice;
    int originalPrice, orderProgress;
    List<String> cardKeys;
    List<String> promotionKeys;
    List<CardsModel> cardsModels;
    List<PromotionsModel> promosModel;
    List<RadioButton> cardRBList;
    List<PromoLayoutModel> promoLayoutModel;
    Intent intent;

    RavePayManager ravePayManager;
    RecyclerView payMethodRecylerView;
    RecyclerView promotionsRecylerView;
    RecyclerView.Adapter payMethodAdapter;
    RecyclerView.Adapter promotionsAdapter;
    Button proceedBtn, doneBtn;
    SharedPreferences sharedPreferences;

    private DatabaseReference userDB;
    private ProgressBar promoProgress;
    private ProgressBar cardProgress;
    boolean fixingDB;
    boolean lastcardAssigned;
    int radioLastPosition, promoLastPosition;
    private String lastCardID;
    private String embedToken;
    private boolean makingPayment;
    private boolean showingReciept;
    private boolean isWeeklyPickup;
    private DatabaseReference baseDB;
    private FirebaseUser user;
    private String lastPromoKey;
    private int lastPromoUsage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        declareVars();

        // TODO Make provisions for incomplete Payments
        // todo insufficient funds return back merror message
        //token_Tx();
        getPromotions();

        //card_or_Bank_Tx(50, "card");
        //addLast3Items();

    }

    private void declareVars() {

        payMethodRecylerView = findViewById(R.id.paymentMethod_RecyclerView);
        promotionsRecylerView = findViewById(R.id.promotions_RecyclerView);
        promoProgress = findViewById(R.id.promoProgress);
        cardProgress = findViewById(R.id.cardProgress);
        proceedBtn = findViewById(R.id.proceedbtn);
        sharedPreferences = this.getSharedPreferences("ng.com.laundrify.laundrify", Context.MODE_PRIVATE);

        intent = getIntent();
        String actTitle = intent.getStringExtra("Motive");
        if (actTitle != null) {
            setTitle(actTitle);
            makingPayment = false;
            proceedBtn.setVisibility(View.GONE);
        } else {
            makingPayment = true;
            proceedBtn.setVisibility(View.VISIBLE);
        }

        payMethodAdapter = new PayMethod();
        payMethodRecylerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        payMethodRecylerView.setAdapter(payMethodAdapter);
        promotionsAdapter = new PromotionAdt();
        promotionsRecylerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        promotionsRecylerView.setAdapter(promotionsAdapter);

        cardKeys = new ArrayList<>();
        promotionKeys = new ArrayList<>();
        cardsModels = new ArrayList<>();
        promosModel = new ArrayList<>();
        cardRBList = new ArrayList<>();
        promoLayoutModel = new ArrayList<>();
        fixingDB = false;
        lastcardAssigned = false;
        radioLastPosition = -1;
        promoLastPosition = 0;

        baseDB = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        userDB = baseDB.child("Users").child("Customers").child(user.getUid());
       //sharedPreferences.edit().putString("lastCardID", "NJtEaPjMDfgbjETMfesrerr4").apply();

        lastCardID = sharedPreferences.getString("lastCardID", null);

        originalPrice = intent.getIntExtra("Price", 0);
        isWeeklyPickup = intent.getBooleanExtra("IsWeeklyPickup", false);
        firstName = intent.getStringExtra("FirstName");
        lastName = intent.getStringExtra("LastName");
        publicKey = intent.getStringExtra("PublicKey");
        encryptionKey = intent.getStringExtra("EncryptionKey");
        userEmail = intent.getStringExtra("UserEmail");
        agent = intent.getStringExtra("Agent");
        orderId = intent.getStringExtra("OrderID");
        dcKey = intent.getStringExtra("DCKey");
        secretKey = intent.getStringExtra("SecretKey");
        orderProgress = intent.getIntExtra("OrderProgress", 0);
        promoPrice = originalPrice;
        showingReciept = false;
    }


     // "status":"success","message":"Tx Fetched","data":{"txid":79119189,"txref":"refref79989","flwref":"Laundrify/FLW166482175","devicefingerprint":"358942091694944","cycle":"one-time","amount":100,"currency":"NGN","chargedamount":101.4,"appfee":1.4,"merchantfee":0,"merchantbearsfee":0,"chargecode":"00","chargemessage":"No Message","authmodel":"PIN","ip":"105.112.56.135:26170","narration":"CARD Transaction ","status":"successful","vbvcode":"00","vbvmessage":"Approved by Financial Institution","authurl":"N/A","acctcode":null,"acctmessage":null,"paymenttype":"card","paymentid":"3558932","fraudstatus":"ok","chargetype":"normal","createdday":6,"createddayname":"SATURDAY","createdweek":31,"createdmonth":7,"createdmonthname":"AUGUST","createdquarter":3,"createdyear":2019,"createdyearisleap":false,"createddayispublicholiday":0,"createdhour":7,"createdminute":45,"createdpmam":"am","created":"2019-08-03T07:45:47.000Z","customerid":51501011,"custphone":null,"custnetworkprovider":"N/A","custname":"Saya Ipoax","custemail":"sheaboy66@gmail.com","custemailprovider":"GMAIL","custcreated":"2019-08-03T07:45:46.000Z","accountid":39667,"acctbusinessname":"Laundrify","acctcontactperson":"Ipaye Oluwaseyifunmi","acctcountry":"NG","acctbearsfeeattransactiontime":0,"acctparent":1,"acctvpcmerchant":"N/A","acctalias":null,"acctisliveapproved":0,"orderref":"URF_1564818347421_4370735","paymentplan":null,"paymentpage":null,"raveref":"RV3156481834606267FD3B56EF","amountsettledforthistransaction":100,"card":{"expirymonth":"03","expiryyear":"23","cardBIN":"539941","last4digits":"3177","brand":"ZENITH BANK DEBIT STANDARD","card_tokens":[{"embedtoken":"flw-t1nf-412f0095055645db0df63824e985b729-k3n","shortcode":"71180","expiry":"9999999999999"}],"type":"MASTERCARD","life_time_token":"flw-t1nf-412f0095055645db0df63824e985b729-k3n"},"meta":[{"id":106206894,"metaname":"sdk","metavalue":"android","createdAt":"2019-08-03T07:45:47.000Z","updatedAt":"2019-08-03T07:45:47.000Z","deletedAt":null,"getpaidTransactionId":79119189}]}}*/

    /*
    Correct card response
   {"status":"Transaction successfully fetched","message":"Tx Fetched","data":{"txid":99372597,"txref":"refref79989","flwref":"FLW172518164","devicefingerprint":"358240051111110","cycle":"one-time","amount":50,"currency":"NGN","chargedamount":50.7,"appfee":0.7,"merchantfee":0,"merchantbearsfee":0,"chargecode":"00","chargemessage":"Kindly enter the OTP sent to *******7509 and s*******@gmail.com. OR enter the OTP generated on your Hardware Token device.","authmodel":"GTB_OTP","ip":"197.210.53.41:30503","narration":"CARD Transaction ","status":"successful","vbvcode":"00","vbvmessage":"Approved","authurl":"N\/A","acctcode":null,"acctmessage":null,"paymenttype":"card","paymentid":"2094973","fraudstatus":"ok","chargetype":"normal","createdday":6,"createddayname":"SATURDAY","createdweek":35,"createdmonth":7,"createdmonthname":"AUGUST","createdquarter":3,"createdyear":2019,"createdyearisleap":false,"createddayispublicholiday":0,"createdhour":9,"createdminute":21,"createdpmam":"am","created":"2019-08-31T09:21:03.000Z","customerid":69515286,"custphone":null,"custnetworkprovider":"N\/A","custname":"Saya Ipoax","custemail":"sheaboy66@gmail.com","custemailprovider":"GMAIL","custcreated":"2019-08-31T09:21:02.000Z","accountid":39667,"acctbusinessname":"Laundrify","acctcontactperson":"Ipaye Oluwaseyifunmi","acctcountry":"NG","acctbearsfeeattransactiontime":0,"acctparent":1,"acctvpcmerchant":"N\/A","acctalias":null,"acctisliveapproved":0,"orderref":"URF_1567243263239_7969435","paymentplan":null,"paymentpage":null,"raveref":"RV31567243261916D63FF65023","amountsettledforthistransaction":50,"card":{"expirymonth":"01","expiryyear":"22","cardBIN":"539983","last4digits":"2750","brand":"GUARANTY TRUST BANK Mastercard Naira Debit Card","card_tokens":[{"embedtoken":"flw-t1nf-6fbc8c3a743e7613980dc8833a8b0fb2-k3n","shortcode":"507a5","expiry":"9999999999999"}],"type":"MASTERCARD","life_time_token":"flw-t1nf-6fbc8c3a743e7613980dc8833a8b0fb2-k3n"},"meta":[{"id":114519971,"metaname":"sdk","metavalue":"android","createdAt":"2019-08-31T09:21:03.000Z","updatedAt":"2019-08-31T09:21:03.000Z","deletedAt":null,"getpaidTransactionId":99372597}]}}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {

                // Process receipt
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                userDB = FirebaseDatabase.getInstance().getReference()
                        .child("Users").child("Customers").child(user.getUid());
                findViewById(R.id.paymentLayout).setVisibility(View.INVISIBLE);

                showProgressDialog("Warning", "Do not cancel, uploading Transaction...", (long) 60000);

                String lastPM = sharedPreferences.getString("lastPM", null);
                lastPromoKey = sharedPreferences.getString("lastPromoKey", null);
                lastPromoUsage = sharedPreferences.getInt("lastPromoUsage", 0);

                Log.i("test", "Success Message" + message);

                if (lastPM != null) {

                    //Get embedded Token
                    embedToken = null;
                    String newFirst6 = null;
                    String newLast4 = null;
                    String txType = null;
                    String txRef = null;
                    Double newAmount = null;

                    //Get Data from JSON
                    if (lastPM.matches("bank")) {
                        try {
                            JSONObject jsonDataObject = new JSONObject(message).getJSONObject("data");
                            txType = "Bank";
                            txRef = jsonDataObject.getString("txref");
                            newAmount = jsonDataObject.getDouble("amount");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            JSONObject jsonDataObject = new JSONObject(message).getJSONObject("data");
                            JSONObject jsonCard = jsonDataObject.getJSONObject("card");
                            embedToken = jsonCard.getJSONArray("card_tokens").getJSONObject(0).getString("embedtoken");
                            Log.i("test", "Token is finaly " + embedToken);

                            newFirst6 = jsonCard.getString("cardBIN");
                            newLast4 = jsonCard.getString("last4digits");
                            txType = "Card";
                            newAmount = jsonDataObject.getDouble("amount");
                            txRef = jsonDataObject.getString("txref");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if (lastPM.matches("token")) {
                        hideProgressDialog(null);

                        // Token: Don't save
                        hideProgressDialog(null);
                        uploadPayment(newAmount, txRef, txType);
                    } else if (lastPM.matches("bank")) {

                        // Bank: Don't save
                        hideProgressDialog(null);
                        uploadPayment(newAmount, txRef, txType);
                    } else {

                        // New Card: Save
                        if (embedToken != null) {

                            // Make new card Map
                            final Map cardMap = new HashMap();
                            cardMap.put("First6", newFirst6);
                            cardMap.put("Last4", newLast4);
                            cardMap.put("Token", embedToken);
                            final String newCardKey = userDB.child("Cards").push().getKey();

                            // Get new key position
                            final String finalTxRef = txRef;
                            final String finalTxType = txType;
                            final Double finalNewAmount = newAmount;


                            //Upload map in Cards
                            userDB.child("Cards").child(newCardKey).setValue(cardMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                    //Upload Card Key in Payments
                                    userDB.child("Payment").child("CardKeys").child(newCardKey).setValue(0, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                            hideProgressDialog(null);
                                            sharedPreferences.edit().putString("lastCardID", newCardKey).apply();
                                            uploadPayment(finalNewAmount, finalTxRef, finalTxType);
                                        }
                                    });
                                }
                            });
                        } else {

                            // Embeded Token is null
                            hideProgressDialog("Sorry something went wrong, Couldn't save card");
                            uploadPayment(newAmount, txRef, txType);
                        }
                    }
                } else {

                    // Last Payment Method is null
                    hideProgressDialog("Failed to process payment, contact us immediately. Please.");
                }

                // TxFailed
            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Snackbar.make(proceedBtn, "ERROR :" + message, Snackbar.LENGTH_LONG).show();
                Log.d("Final Error Occured: ", message);
                finish();
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Snackbar.make(proceedBtn, "CANCELLED :" + message, Snackbar.LENGTH_LONG).show();
                Log.d("test", "Canceled" + message);
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void uploadPayment(final Double paidAmount, final String txRef, final String txType) {
        showProgressDialog("", "Do not cancel, uploading Transaction...", (long) 60000);

        // Get date
        final SimpleDateFormat sdfStamp = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat paymentFormat = new SimpleDateFormat("MMMM d, h:mm a");
        final Date time = new Date();

        // Make Map
        final Map paymentMap = new HashMap();
        paymentMap.put("TxRef", txRef);
        paymentMap.put("TxType", txType);
        paymentMap.put("PaidAmount", paidAmount);
        paymentMap.put("OriginalAmount", originalPrice);
        paymentMap.put("CustomerName", firstName + " " + lastName);
        paymentMap.put("DcCompanyName", agent);
        paymentMap.put("OrderID", orderId);
        paymentMap.put("DryCleanerID", dcKey);
        paymentMap.put("TimeStamp", sdfStamp.format(time));
        paymentMap.put("TxTime", paymentFormat.format(time));


        // Messaging
        final String laundrifyDC_ID = "1f292792-a34a-4053-adb6-53af8ae6bb21";

        // Upload Promo usage
        if (lastPromoKey != null) {
            userDB.child("Promotions").child(lastPromoKey).child("Usage").setValue(lastPromoUsage + 1);
        }

        // Upload to Payment DB
        final String paymentKey = baseDB.child("Payments").push().getKey();

        // Upload Payment map
        baseDB.child("Payments").child(paymentKey).setValue(paymentMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                // Upload to Admin DB
                baseDB.child("Admin").child("Payments").child(sdfStamp.format(time).substring(0,8)).child(paymentKey).child("TimeStamp").setValue(sdfStamp.format(time));

                // Upload to Order DB
                baseDB.child("Orders").child(orderId).child("HavePaid").setValue(true, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        // Upload payment key
                        baseDB.child("Orders").child(orderId).child("PaymentKey").setValue(paymentKey);

                        // Upload payment method
                        baseDB.child("Orders").child(orderId).child("PaymentMethod").setValue("Card", new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                // Upload to Dc DB
                                baseDB.child("Users").child("DryCleaners").child(dcKey).child("Payments").child(sdfStamp.format(time).substring(0,8))
                                        .child(paymentKey).child("TimeStamp").setValue(sdfStamp.format(time), new DatabaseReference.CompletionListener() {@Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                            // Message DC
                                    baseDB.child("Users").child("DryCleaners").child(dcKey).child("Info")
                                            .child("NotificationToken").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        new SendNotification(
                                                                PaymentActivity.this,
                                                                String.valueOf(dataSnapshot.getValue()), // Token
                                                                "New payment received",
                                                                "Payment of ₦" + originalPrice + " from " + firstName + lastName
                                                                        + " at " + paymentMap.get("TxTime").toString().substring(9)
                                                        );
                                                    }
                                                    showReciept(paidAmount, txRef, txType, time);
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                }
                                            });
                                        }});
                            }});
                    }
                });
            }
        });
    }

    private void showReciept(Double newAmount, String txRef, String txType, Date time) {

        showingReciept = true;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat sdfT = new SimpleDateFormat("hh:mm aa");

        findViewById(R.id.recieptLayout).setVisibility(View.VISIBLE);

        Log.i("test", "Showing for... " + cardsModels.size());
        TextView dateTV, agentTV, amountTV, nameTV, txTypeTV, txRefTV, txTime;
        dateTV = findViewById(R.id.date);
        agentTV = findViewById(R.id.agent);
        amountTV = findViewById(R.id.amount);
        nameTV = findViewById(R.id.paidBy);
        txTypeTV = findViewById(R.id.txType);
        txRefTV = findViewById(R.id.txRef);
        txTime = findViewById(R.id.txTime);

        dateTV.setText(sdf.format(time));
        txTime.setText(sdfT.format(time));
        agentTV.setText(agent);
        amountTV.setText("₦" + newAmount.toString());
        nameTV.setText(lastName + " " + firstName);
        txTypeTV.setText(txType);
        txRefTV.setText(txRef);
        hideProgressDialog(null);

        findViewById(R.id.doneBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(PaymentActivity.this, R.style.AlertDialogCustom));

                if (orderProgress == 3) {
                    builder.setTitle("Warning");
                    builder.setMessage("Are you sure the Delivery Man has seen this ?")
                            .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                } else {
                    builder.setMessage("Thank you for using our service")
                            .setPositiveButton("Feels good", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                }
                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (showingReciept) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(PaymentActivity.this, R.style.AlertDialogCustom));

            if (orderProgress == 3) {
                builder.setTitle("Warning");
                builder.setMessage("Are you sure the Delivery Man has seen this ?")
                        .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
            } else {
                builder.setMessage("Thank you for using our service")
                        .setPositiveButton("Feels good", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
            }

            // todo leked view
            /*
            2019-12-10 10:50:23.815 16921-16921/ng.com.laundrify.laundrify E/WindowManager: android.view.WindowLeaked: Activity ng.com.laundrify.laundrify.PaymentActivity has leaked window DecorView@38f746a[] that was originally added here
        at android.view.ViewRootImpl.<init>(ViewRootImpl.java:489)
        at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:346)
        at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:94)
        at android.app.Dialog.show(Dialog.java:344)
        at ng.com.laundrify.laundrify.PaymentActivity.onBackPressed(PaymentActivity.java:477)
             */
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
        super.onBackPressed();
    }

    private void card_or_Bank_Tx(double price, String motive) {

        //if (price != 0) {
        ravePayManager = new RavePayManager(this);
        ravePayManager
                .setAmount(price)
                .setEmail(userEmail)
                .setfName(firstName)
                .setlName(lastName)
                .setNarration("Payment for Laundrify order " + orderId)
                .setPublicKey(publicKey)
                .setEncryptionKey(encryptionKey)
                .setCountry("NG")
                .setCurrency("NGN")
                .setTxRef("LDYOD" + orderId)
                .acceptMpesaPayments(false)
                .acceptAchPayments(false)
                .acceptGHMobileMoneyPayments(false)
                .acceptUgMobileMoneyPayments(false)
                .onStagingEnv(false)
                .allowSaveCardFeature(false)
                .shouldDisplayFee(false)
                .isPreAuth(false)
                .withTheme(R.style.FullscreenDialogTheme)
                .withToken(false);

        if (motive.matches("card")) {
            if (originalPrice != promoPrice) {
                sharedPreferences.edit().putString("lastPromoKey", lastPromoKey).apply();
                sharedPreferences.edit().putInt("lastPromoUsage", lastPromoUsage).apply();
            }
            sharedPreferences.edit().putString("lastPM", "card").apply();
            ravePayManager
                    .acceptAccountPayments(false)
                    .acceptCardPayments(true)
                    .setSecretKey(secretKey)
                    .initialize();
        } else if (motive.matches("bank")) {
            sharedPreferences.edit().putString("lastPM", "bank").apply();
            ravePayManager
                    .acceptAccountPayments(true)
                    .acceptCardPayments(false)
                    .initialize();
        }

        Log.i("Stuff", "This " + orderId);
    }

    public void token_Tx(double price, String card6, String token) {

        // Secret Key
        // Currency":"NGN"
        // SECKEY
        // Token
        // Country":"NG",
        // Amount
        // Email":"desola.ade1@gmail.com",
        // Firstname":"temi",
        // Lastname":"Oyekole",
        // TxRef":"MC-7666-YU"
        // WithToken

        if (price != 0) {
            if (originalPrice != promoPrice) {
                sharedPreferences.edit().putString("lastPromoKey", lastPromoKey).apply();
                sharedPreferences.edit().putInt("lastPromoUsage", lastPromoUsage).apply();
            }
            sharedPreferences.edit().putString("lastPM", "token").apply();
            ravePayManager = new RavePayManager(this);
            ravePayManager
                    .setAmount(price)
                    .setEmail(userEmail)
                    .setfName(firstName)
                    .setlName(lastName)
                    .setCountry("NG")
                    .setCurrency("NGN")
                    .setTxRef("LDYOD" + orderId)
                    .withTheme(R.style.FullscreenDialogTheme)
                    .withToken(true)
                    .setSecretKey(secretKey)
                    .setcard6(card6)
                    .setToken(token)
                    .initialize();
        }
        Log.i("Stuff", "This " + orderId);
    }

    //When Proceed Button is selected
    public void proceedPayment(View view) {

        // Check if nothing is wrong
        if (radioLastPosition != -1) {

            Log.i("test", "radio is " + radioLastPosition + "size " + cardsModels.size());
            if (radioLastPosition == cardsModels.size()-2) {

                //Cash was selected
                Toast.makeText(this, "Pay ₦" + originalPrice + " to the delivery Man", Toast.LENGTH_SHORT).show();

            } else if (radioLastPosition == cardsModels.size()-3) {

                //Transfer was selected
                bankTransfer();
            } else if (cardsModels.size() >= 4){

                //Card was selected
                cardPayment(cardsModels.get(radioLastPosition));
            }
        } else {
            Toast.makeText(this, "Something went erong, try again later", Toast.LENGTH_SHORT).show();
        }
    }

    public void enterPromoCode(View view) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dView = inflater.inflate(R.layout.settings_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog;
        final TextInputEditText setEdit1 = dView.findViewById(R.id.settingsEditText1);
        TextInputLayout textInput1 = dView.findViewById(R.id.textInputLayout1);

        textInput1.setVisibility(View.VISIBLE);
        textInput1.setHint("Enter promo code");
        builder.setTitle("Apply new promo")
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        checkPromoCode(setEdit1.getText().toString());
                    }})
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }});
        builder.setView(dView);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();
    }

    private void checkPromoCode(final String promoCode) {
        showProgressDialog("", "Checking for promo code...", (long) 60000);

        // Check if used
        baseDB.child("Promotions").child("PromoCodes")
                .child(promoCode).child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {

                    // Not used, get Map
                    baseDB.child("Promotions").child("PromoMaps").child(promoCode)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                final int daysLength;
                                Map map = (Map) dataSnapshot.getValue();

                                // Get Days Length
                                if (map.get("DaysLength") != null) {
                                    daysLength = Integer.valueOf(map.get("DaysLength").toString());
                                } else {
                                    Snackbar.make(proceedBtn, "Couldn't apply Promo, Contact us", Snackbar.LENGTH_LONG).show();
                                    return;
                                }

                                // Get Start and Stop Time
                                final SimpleDateFormat sdfPromo = new SimpleDateFormat("yyyyMMdd");
                                final Date time = new Date();
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(time);

                                map.put("StartStamp", sdfPromo.format(cal.getTime()));

                                cal.add(Calendar.DAY_OF_YEAR, daysLength);
                                map.put("StopStamp", sdfPromo.format(cal.getTime()));

                                final String promoKey = userDB.child("Promotions").push().getKey();
                                map.remove("DaysLength");

                                // Add Map
                                userDB.child("Promotions").child(promoKey).setValue(map, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                        // Add Key
                                        userDB.child("Payment").child("PromotionKeys").child(promoKey).setValue(0, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                                // Add to Promo users
                                                baseDB.child("Promotions").child("PromoCodes").child(promoCode).child(user.getUid()).setValue(0);
                                                hideProgressDialog(null);
                                                getPromotions();
                                            }
                                        });
                                    }
                                });
                            } else {
                                hideProgressDialog("Sorry, couldn't find promo code. Case sentitive");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    hideProgressDialog("Sorry, Promo code used in the past");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void cardPayment(CardsModel cardsModel) {
        if (promosModel.isEmpty())
            token_Tx(originalPrice, cardsModel.getFirst6digits(), cardsModel.getToken());
        else if (promoPrice != 0 && promoPrice == originalPrice) {
            Toast.makeText(this, "Your promo discount is right there", Toast.LENGTH_SHORT).show();
            token_Tx(originalPrice, cardsModel.getFirst6digits(), cardsModel.getToken());
        } else if (promoPrice != originalPrice) {
            token_Tx(promoPrice, cardsModel.getFirst6digits(), cardsModel.getToken());
        }
    }

    private void bankTransfer() {
        if (makingPayment) {
            proceedBtn.setVisibility(View.VISIBLE);
            proceedBtn.setText("Pay ₦" + originalPrice);
        }
        card_or_Bank_Tx(originalPrice, "bank");
    }

    private void addLast3Items(boolean start) {

        //Add last Three items
        cardsModels.add(new CardsModel(null, "  Bank Transfer", R.drawable.ic_account_balance_black_24dp, null, null));
        cardsModels.add(new CardsModel(null, "  Cash Payment", R.drawable.ic_money, null, null));
        cardsModels.add(new CardsModel(null, "  Add new Card", R.drawable.ic_add_black_24dp, null, null));
        cardRBList.clear();
        payMethodAdapter.notifyDataSetChanged();

        Log.i("test", "First" + cardsModels.size());
        if (start) {
            cardProgress.setVisibility(View.VISIBLE);
            payMethodRecylerView.setClickable(false);
        } else {
            cardProgress.setVisibility(View.GONE);
            payMethodRecylerView.setClickable(true);
            if (isWeeklyPickup) {
                addWeeklyPickupBonus();
            }
        }
    }

    private void addWeeklyPickupBonus() {
        baseDB.child("Promotions/WeeklyPickupPromo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    promosModel.add(new PromotionsModel(chgS(dataSnapshot.child("Title").getValue()), chgS(dataSnapshot.child("Description").getValue()),
                            chgI(dataSnapshot.child("Percentage").getValue()), chgI(dataSnapshot.child("MaxValue").getValue()), chgI(dataSnapshot.child("Usage").getValue()), null));
                    promotionsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(PaymentActivity.this, "Conldn't find wekly promo", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private String chgS(Object obj) {
        return String.valueOf(obj);
    }
    private int chgI(Object obj) {
        if (obj != null) {
            return Integer.valueOf(String.valueOf(obj));
        } else {
            return 0;
        }
    }
    private double chgD(Object obj) {
        if (obj != null) {
            return Double.valueOf(String.valueOf(obj));
        } else {
            return 0;
        }
    }

    private void getSavedCards() {

        //Show options first
        cardsModels.clear();
        cardRBList.clear();
        addLast3Items(true);
        cardKeys = new ArrayList<>();

        //Check for saved cards
        userDB.child("Payment").child("CardKeys")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (!fixingDB) {
                            cardProgress.setVisibility(View.VISIBLE);
                            if (dataSnapshot.exists()) {
                                cardKeys.clear();

                                for (DataSnapshot snapChild : dataSnapshot.getChildren()) {
                                    cardKeys.add(snapChild.getKey());
                                }

                                fetchCardsInfo(cardKeys);
                                Log.i("test", "Total saved card is: " + dataSnapshot.getChildrenCount());
                            } else {
                                Log.i("test", "No saved cards");
                                cardProgress.setVisibility(View.GONE);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void fetchCardsInfo(final List<String> keys) {

        //Get info of given Keys
        cardsModels.clear();
        for (final Iterator<String> it = keys.iterator(); it.hasNext();) {
            final String thisCardKey = it.next();
            final boolean itNextable = it.hasNext();
            userDB.child("Cards").child(thisCardKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        cardsModels.add(new CardsModel(chgS(dataSnapshot.child("First6").getValue()),
                                "  **** **** **** " + chgS(dataSnapshot.child("Last4").getValue()),
                                R.drawable.ic_credit_card_black_24dp, chgS(dataSnapshot.child("Token").getValue()), dataSnapshot.getKey()));

                        // Check if finished
                        if (!itNextable) {
                            Log.i("test", "Cards end");
                            addLast3Items(false);
                        }
                        Log.i("test", "Cards next " + itNextable);
                    } else {

                        // Remove lost Keys
                        fixingDB = true;
                        Log.i("test", thisCardKey + " Dosen't exist " + itNextable);
                        userDB.child("Payment").child("CardKeys").child(thisCardKey).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                fixingDB = false;
                            }
                        });
                    if (!itNextable) {
                            addLast3Items(false);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void getPromotions() {
        promotionKeys = new ArrayList<>();
        final double [] lastSeen = new double[1];

        // Get Last seen
        userDB.child("Info").child("LastSeen").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String cutString = chgS(dataSnapshot.getValue()).substring(0,12);
                lastSeen[0] = chgD(cutString);
                Log.i("test", "Cut is..." + cutString);

                // Get Promotions
                userDB.child("Payment").child("PromotionKeys")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {
                                    if (!fixingDB) {
                                        promoProgress.setVisibility(View.VISIBLE);
                                        if (dataSnapshot.exists()) {

                                            promotionKeys.clear();
                                            for (DataSnapshot under : dataSnapshot.getChildren()) {
                                                promotionKeys.add(under.getKey());
                                            }
                                            final Date timeNow = new Date();
                                            fetchPromotionInfo(promotionKeys, lastSeen[0], timeNow);
                                            Log.i("test", "Total in promo is... " + dataSnapshot.getChildrenCount());
                                        } else {
                                            Log.i("test", "No promotion");
                                            promoProgress.setVisibility(View.GONE);
                                        }
                                    } else {
                                        Toast.makeText(PaymentActivity.this, "Fixing Database. Please try again later", Toast.LENGTH_SHORT).show();
                                    }
                                } else {

                                    // Proceed
                                    promoLayoutModel.clear();
                                    promoProgress.setVisibility(View.GONE);
                                    promotionsAdapter.notifyDataSetChanged();
                                    getSavedCards();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchPromotionInfo(final List<String> keys, final double lastSeen, final Date timeNow) {
        final SimpleDateFormat sdfStamp = new SimpleDateFormat("yyyyMMddHHmm");
        final SimpleDateFormat sdfStamp1 = new SimpleDateFormat("yyyyMMdd");


        //Get info of given Keys
        promosModel.clear();
        for (final Iterator<String> it = keys.iterator(); it.hasNext();) {
            final String thisCardKey = it.next();
            final boolean itNextable = it.hasNext();
            userDB.child("Promotions").child(thisCardKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {

                        // Check if user is Malicious
                        if (chgD(sdfStamp.format(timeNow)) >= lastSeen) {

                            // Check if user is eligible
                            final int dayNow = chgI(sdfStamp1.format(timeNow));

                            // If valid by time
                            if (chgI(dataSnapshot.child("StartStamp").getValue()) <= dayNow &&  dayNow <= chgI(dataSnapshot.child("StopStamp").getValue())) {
                                Log.i("test", "Falls in range");

                                // If valid by usage
                                if (chgI(dataSnapshot.child("Usage").getValue()) < chgI(dataSnapshot.child("MaxUsage").getValue())) {

                                    promosModel.add(new PromotionsModel(chgS(dataSnapshot.child("Title").getValue()), chgS(dataSnapshot.child("Description").getValue()),
                                            chgI(dataSnapshot.child("Percentage").getValue()), chgI(dataSnapshot.child("MaxValue").getValue()), chgI(dataSnapshot.child("Usage").getValue()), thisCardKey));
                                } else {
                                    Log.i("test", "Delete, Exceed Usage, Delete");
                                    removePromo(thisCardKey);
                                }

                            } else {
                                Log.i("test", "Delete, Delete, Delete");
                                removePromo(thisCardKey);
                            }
                        } else {

                            // All is not well. Malicious User alert !!!
                            Log.d("test", "all is notttttt well");
                            removePromo(thisCardKey);
                            Snackbar.make(proceedBtn, "You have been blacklisted as a malicious user", Snackbar.LENGTH_LONG).show();
                        }

                        if (!itNextable) {

                            Log.i("test", "Promo ended " + promosModel.size());
                            promoLayoutModel.clear();
                            promoProgress.setVisibility(View.GONE);
                            promotionsAdapter.notifyDataSetChanged();
                            getSavedCards();
                        }
                    } else {

                        //Remove lost Keys
                        Log.i("test", "Removed " + thisCardKey + "for promo" + promosModel.size());
                        fixingDB = true;
                        userDB.child("Payment").child("PromotionKeys").child(thisCardKey).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                fixingDB = false;
                            }
                        });

                        if (!itNextable) {

                            promoLayoutModel.clear();
                            promoProgress.setVisibility(View.GONE);
                            promotionsAdapter.notifyDataSetChanged();
                            getSavedCards();
                        }
                    }
                }

                private void removePromo(String promoKey) {

                    //Remove Promo
                    Log.i("test", "Removed " + promoKey + "for promo" + promosModel.size());
                    fixingDB = true;
                    userDB.child("Promotions").child(promoKey).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            fixingDB = false;
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    //First Recycler Adapter
    public class PayMethod extends RecyclerView.Adapter<PayMethod.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView cardText;
            LinearLayout pmLayout;
            RadioButton cardRB;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                this.cardText = itemView.findViewById(R.id.pmMethods_TextView);
                this.pmLayout = itemView.findViewById(R.id.pm_linear);
                this.cardRB = itemView.findViewById(R.id.cardRadioButton);
            }
        }

        @NonNull
        @Override
        public PayMethod.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pm_inner_item, parent, false);
            PayMethod.MyViewHolder myViewHolder = new PayMethod.MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull PayMethod.MyViewHolder holder, final int position) {
            TextView cardText = holder.cardText;
            LinearLayout pmLayout = holder.pmLayout;
            final RadioButton cardRB = holder.cardRB;
            cardRB.setChecked(false);
            //
            Log.d("test", position + "::::" + cardsModels.size());
            //

            //setView
            cardText.setText(cardsModels.get(position).getLast4digits());
            cardText.setCompoundDrawablesRelativeWithIntrinsicBounds(cardsModels.get(position).getDrawable(), 0, 0, 0);
            if (position == cardsModels.size() - 1) {
                cardRB.setVisibility(View.GONE);
            } else {
                cardRB.setVisibility(View.VISIBLE);
                cardRBList.add(position, cardRB);
            }

            // Button Long click Listener
            if (position < cardsModels.size() - 1) {
                pmLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        removeCard(position);
                        return false;
                    }
                });
            }

            //Button click listener
            pmLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("test", "selected " + position);
                    handleClick(cardRB, position);
                }
            });

            //Assign last card saved
            String last = cardsModels.get(position).getCardKey();
            if (lastCardID != null && last != null && last.matches(lastCardID)) {
                handleClick(cardRB, position);
                lastcardAssigned = true;
            }

            //check if there's is no saved card
            if (position == cardsModels.size()-1 && !lastcardAssigned) {
                handleClick(cardRBList.get(cardRBList.size()-1), position-1);
                Log.i("test", "No Saved card");
            }
        }

        @Override
        public int getItemCount() {
            return cardsModels.size();
        }
    }

    private void removeCard(final int position) {
        if (position >= cardsModels.size() - 3) {

            // If not card Vibrate
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(200);
            }
        } else {

            // If card ...
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle("Delete card");
            builder.setMessage("Are you sure you want to delete card" + cardsModels.get(position).getLast4digits() + "?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            showProgressDialog("", "Removing card...", (long)60000);
                            userDB.child("Cards").child(cardsModels.get(position).getCardKey()).removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    hideProgressDialog("Deleted Card");
                                    cardsModels.remove(position);
                                    cardRBList.remove(position);
                                    payMethodAdapter.notifyItemRemoved(position);
                                    payMethodAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    //Handle Radio button click
    private void handleClick(RadioButton cardRB, int position) {
        Log.i("test", "RBlist is " + cardRBList.size());
        if (position == cardsModels.size()-1) {

            //Add card is selected
            if (makingPayment) {
                proceedBtn.setVisibility(View.GONE);
                if (promosModel.isEmpty())
                    card_or_Bank_Tx(promoPrice, "card");
                else if (promoPrice == originalPrice) {
                    Toast.makeText(this, "Your promo discount is right there", Toast.LENGTH_SHORT).show();
                    card_or_Bank_Tx(originalPrice, "card");
                } else if (promoPrice != originalPrice) {
                    card_or_Bank_Tx(promoPrice, "card");
                }
            } else {
                Toast.makeText(this, "Cards can only be added while making payments", Toast.LENGTH_LONG).show();
            }
        } else {

            //Other Options Selected
            if (makingPayment) {
                proceedBtn.setVisibility(View.VISIBLE);
            }

            if (cardRB.isChecked()) {

                //Uncheck if checked
                cardRB.setChecked(false);
                cardRBList.get(cardRBList.size()-1).setChecked(true);
                proceedBtn.setBackground(getResources().getDrawable(R.drawable.lightgrey_solid_selector));
                proceedBtn.setText("Pay ₦" + originalPrice);
                radioLastPosition = cardsModels.size()-2;
                applyPromo(-1);
            } else {

                //Check if Unchecked
                for (Iterator<RadioButton> it = cardRBList.iterator(); it.hasNext(); ) {
                    RadioButton thisRB = it.next();
                    thisRB.setChecked(false);
                }
                cardRB.setChecked(true);
                radioLastPosition = position;
                proceedBtn.setBackground(getResources().getDrawable(R.drawable.red_solid_selector));

                //Check if card was selected
                if (position == cardsModels.size() - 2) {

                    //Cash was selected
                    proceedBtn.setBackground(getResources().getDrawable(R.drawable.lightgrey_solid_selector));
                    proceedBtn.setText("Pay ₦" + originalPrice);
                    applyPromo(-1);
                } else if (position == cardsModels.size() - 3) {

                    //Bank was selected
                    proceedBtn.setBackground(getResources().getDrawable(R.drawable.red_solid_selector));
                    proceedBtn.setText("Pay ₦" + originalPrice);
                    applyPromo(-1);
                } else if (position < cardsModels.size() - 3) {

                    //Card was selcted
                    applyPromo(promoLastPosition);
                }
            }
        }
    }

    private void applyPromo(int promoPosition) {
        for (Iterator<PromoLayoutModel> it = promoLayoutModel.iterator(); it.hasNext();) {
            PromoLayoutModel thisPromoLayoutModel = it.next();
            LinearLayout thisLayout = thisPromoLayoutModel.getPromoLayout();
            TextView thisTitle = thisPromoLayoutModel.getPromoTitle();
            TextView thisDesc = thisPromoLayoutModel.getPromoDesc();

            thisLayout.setBackground(getResources().getDrawable(R.drawable.lightgrey_solid_selector));
            thisTitle.setTextColor(getResources().getColor(R.color.light_grey));
            thisDesc.setTextColor(getResources().getColor(R.color.light_grey));

            //Card was selected
            if (promoPosition != -1) {
                Log.i("test", "promo " + promoPosition);

                // Change description
                thisDesc.setText(thisPromoLayoutModel.getDescText());

                // Select promo when finished
                if (!it.hasNext()) {
                    changePrice(promoPosition);
                }
            } else {
                thisDesc.setText("Promotion can only be used when paying with Card");
            }
        }
    }

    private void changePrice(int promoPosition) {

        PromoLayoutModel thisPromoLayoutModel = promoLayoutModel.get(promoPosition);
        LinearLayout thisLayout = thisPromoLayoutModel.getPromoLayout();
        TextView thisTitle = thisPromoLayoutModel.getPromoTitle();
        TextView thisDesc = thisPromoLayoutModel.getPromoDesc();
        thisLayout.setBackground(getResources().getDrawable(R.drawable.red_solid_selector));
        thisTitle.setTextColor(getResources().getColor(R.color.colorAccent));
        thisDesc.setTextColor(getResources().getColor(R.color.colorAccent));

        lastPromoKey = promosModel.get(promoPosition).getPromoKey();
        lastPromoUsage = promosModel.get(promoPosition).getUsage();
        double promoPercentage = promosModel.get(promoPosition).getPercentage();
        int promoMaxValue = promosModel.get(promoPosition).getMaxValue();

        if (makingPayment) {
            if (originalPrice != 0 && promoPercentage != 0 && promoMaxValue != 0) {
                double promoCut = promoPercentage / 100 * originalPrice;
                if (promoCut >= promoMaxValue && makingPayment) {
                    promoPrice = originalPrice - promoMaxValue;
                    Snackbar.make(proceedBtn, "Discount exceeds maximum value", Snackbar.LENGTH_SHORT).show();
                } else {
                    promoPrice = originalPrice - promoCut;
                }
                promoPrice = Math.round(promoPrice * 10) / 10.0;
                if (makingPayment) {
                    proceedBtn.setVisibility(View.VISIBLE);
                    proceedBtn.setText("Pay ₦" + promoPrice);
                }
            } else {
                Toast.makeText(this, "Couldn't apply Promo. Contact us", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Second Recycler Adapter
    public class PromotionAdt extends RecyclerView.Adapter<PromotionAdt.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {
            LinearLayout promoLayout;
            TextView promoTitle;
            TextView promoDesc;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                this.promoLayout = itemView.findViewById(R.id.promoLayout);
                this.promoTitle = itemView.findViewById(R.id.promotion_Title);
                this.promoDesc = itemView.findViewById(R.id.promotion_Desc);
            }
        }

        @NonNull
        @Override
        public PromotionAdt.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pm_promotion_inner_item, parent, false);
            PromotionAdt.MyViewHolder myViewHolder = new PromotionAdt.MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull PromotionAdt.MyViewHolder holder, final int position) {
            TextView promoTitle = holder.promoTitle;
            TextView promoDesc = holder.promoDesc;
            LinearLayout promoLayout = holder.promoLayout;

            // Set view
            promoTitle.setText(promosModel.get(position).getHeader());
            promoDesc.setText(promosModel.get(position).getDescription());

            promoLayoutModel.add(position, new PromoLayoutModel(promoLayout, promoTitle, promoDesc,
                    promosModel.get(position).getDescription()));
            promoLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handlePromoClick(position);
                }
            });
        }
        @Override
        public int getItemCount() {
            return promosModel.size();
        }
    }

    private void handlePromoClick(int position) {

        // Check if nothing is wrong
        if (radioLastPosition != -1) {
            Log.i("test", "radio is " + radioLastPosition + "size " + cardsModels.size());

            // Check if card was selected
            if (radioLastPosition == cardsModels.size()-2) {
                Toast.makeText(PaymentActivity.this, "Can only be applied to card", Toast.LENGTH_SHORT).show();
            } else if (radioLastPosition == cardsModels.size()-3) {
                Toast.makeText(PaymentActivity.this, "Can only be applied to card", Toast.LENGTH_SHORT).show();
            } else if (cardsModels.size() >= 4){
                promoLastPosition = position;
                applyPromo(position);
            }
        } else {
            Toast.makeText(PaymentActivity.this, "Something went wrong, try again later", Toast.LENGTH_SHORT).show();
        }
    }

    //Progress Dialog
    Handler handler = new Handler();
    public ProgressDialog mProgressDialog;
    public void showProgressDialog(String title, String message, Long time) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            handler.removeCallbacksAndMessages(null);
        }
        if (title.matches("")) {
            mProgressDialog = new ProgressDialog(this, R.style.AlertDialogCustom);
        } else {
            mProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            mProgressDialog.setTitle(title);
        }
        mProgressDialog.setMessage(message);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(mProgressDialog!=null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    Toast.makeText(PaymentActivity.this, "Couldn't connect, please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        }, time);
    }

    public void hideProgressDialog(String msg) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            handler.removeCallbacksAndMessages(null);
            if (msg != null) {
                Snackbar.make(proceedBtn, msg, Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
