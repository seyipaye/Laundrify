package ng.com.laundrify.laundrify;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import static ng.com.laundrify.laundrify.Track_Adapter.CALL_REQUEST;

//Phone Number
//DC details

public class TrackActivity extends AppCompatActivity {
    ArrayList<Track_Model> track_models;
    ArrayList<Items_Model> items;
    TextView noOrders;

    private FirebaseAuth mAuth;
    public static FirebaseDatabase database;
    static DatabaseReference customerOrderDB, orderDB, customerHistoryDB;
    FirebaseUser user;

    static RecyclerView.Adapter recyclerAdapter;

    int image, progress, price;
    static String fname;
    static String lname;
    static String uEmail;
    static String publicKey, secretKey;
    static String encryptionKey;
    static int uWCDay;
    static int uWCMins;
    static int uWCHour;
    static String uWCDC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        showProgressDialog("Please wait...", "Fetching available orders", (long) 60000);

        noOrders = findViewById(R.id.noOrders);
        track_models = new ArrayList<>();

        //Database
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        customerOrderDB = database.getReference().child("Users").child("Customers").child(user.getUid()).child("Orders");
        customerHistoryDB = database.getReference().child("Users").child("Customers").child(user.getUid()).child("History");
        orderDB = database.getReference().child("Orders");

        getUserOrdersKeynPath();

        items = new ArrayList<>(1);
        //if Price is availabe
        items.add(new Items_Model("adaddda", "N90,000"));
        //// TODO get items showing
        /*
        items.add(new Items_Model("adaddda", "N90,000"));
        items.add(new Items_Model("adaddda", "N90,000"));
        items.add(new Items_Model("adaddda", "N90,000"));
        items.add(new Items_Model("adaddda", "N90,000"));
        items.add(new Items_Model("adaddda", "N90,000"));
        items.add(new Items_Model("adadASdda", "N90,000"));
        items.add(new Items_Model("adadSazdda", "N90,000"));
        items.add(new Items_Model("adaddzda", "N90,000"));
        items.add(new Items_Model("adadAZzZdda", "N90,000"));
        items.add(new Items_Model("adadaqs  Sdda", "N90,000"));
        items.add(new Items_Model("adazazAAddda", "N90,000"));
        items.add(new Items_Model("adadasAXQEFERGdda", "N90,000"));
        items.add(new Items_Model("adadERFSdda", "N90,000"));
        items.add(new Items_Model("adadFGSDFSdda", "N90,000"));
        items.add(new Items_Model("adaddVSda", "N90,000"));
        items.add(new Items_Model("adadFVSFVdda", "N90,000"));
        items.add(new Items_Model("adaddda", "N90,000"));
        items.add(new Items_Model("adadDFdda", "N90,000"));
        */

        RecyclerView recyclerView = findViewById(R.id.trackRV);
        recyclerAdapter = new Track_Adapter(track_models, this);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        publicKey = null;
        encryptionKey = null;
        secretKey = null;
        getCardPaymentStuff();
        getWeeklyPickupDetails();
        dealWithNotif();
    }

    private void dealWithNotif() {
        Intent intent = this.getIntent();
        String noID = String.valueOf(intent.getIntExtra("NotifID", 0));
        //Toast.makeText(this, "Got Notif..." + noID, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onRestart() {
        finish();
        super.onRestart();
    }


    //fetch orders key and paths
    private void getUserOrdersKeynPath() {
        customerOrderDB.orderByChild("TimeStamp")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot != null) {
                    track_models.clear();
                    int childCount = 0;
                    for (DataSnapshot orders : dataSnapshot.getChildren()) {
                        childCount ++;
                        fetchOrderInfo(orders.getKey(), childCount);
                        //Log.i("test", "orderChild");
                    }
                    //Log.i("test", "Total is: " + dataSnapshot.getChildrenCount());
                } else {
                    noOrders.setVisibility(View.VISIBLE);
                    hideProgressDialog();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //Fetch order info
    private void fetchOrderInfo(final String orderKey, final int childCount) {
        orderDB.child(orderKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot != null) {
                   Map orderInfo = (Map) dataSnapshot.getValue();
                   if (orderInfo.size() > 0) {
                       //Log.i("stuff", "yes map");

                       //dcName = (String) orderInfo.get("DCName");
                       //dcPick = (String) orderInfo.get("DCPick");
                       //dcNumber = (String) orderInfo.get("DCNumber");

                       if (chgS(orderInfo.get("Vehicle")).matches("Bike")) {
                           image = R.drawable.delivery_bike_white;
                       } else {
                           image = R.drawable.delivery_van_white;
                       }

                       track_models.add(new Track_Model(image, orderKey, chgB(orderInfo.get("Quickwash")), chgB(orderInfo.get("WeeklyPickup")),
                               chgS(orderInfo.get("FirstName")) + " | " + chgS(orderInfo.get("PhoneNumber")), chgS(orderInfo.get("CollectionTime")),
                               chgS(orderInfo.get("PickupAddress")), chgS(orderInfo.get("DeliveryTime")), chgS(orderInfo.get("PickupAddress")),
                               chgI(orderInfo.get("Progress")), chgI(orderInfo.get("Price")), chgB(orderInfo.get("HavePaid")), new ArrayList<Items_Model>(),
                               "", "", chgS(orderInfo.get("DryCleanerKey")), chgS(orderInfo.get("DeliveryStamp")),
                               chgS(orderInfo.get("CollectionStamp")), chgS(orderInfo.get("DCCompanyName")), chgS(orderInfo.get("DeliveryManName")),
                               chgS(orderInfo.get("DCPhoneNumber"))));
                       recyclerAdapter.notifyDataSetChanged();
                       noOrders.setVisibility(View.GONE);
                       hideProgressDialog();
                       Log.i("test", "Added " + childCount);

                       //Add first and Last name
                       if (fname == null) {
                           fname = chgS(orderInfo.get("FirstName"));
                           lname = chgS(orderInfo.get("LastName"));
                       }
                   }
                } else {
                    customerOrderDB.child(orderKey).removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("stuff", "cancelled");
            }
        });
    }

    // Changers
    private String chgS(Object obj) {
        return String.valueOf(obj);
    }
    private boolean chgB(Object obj) {
        return Boolean.valueOf(String.valueOf(obj));
    }
    private int chgI(Object obj) {
        return Integer.valueOf(String.valueOf(obj));
    }

    //Get card payment stuff
    private void getCardPaymentStuff() {
        if (publicKey == null || encryptionKey == null) {
            database.getReference().child("PaymentInfo").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Map paymentInfo = (Map) dataSnapshot.getValue();
                        if (paymentInfo.size() > 0) {
                            publicKey = (String) paymentInfo.get("PublicKey");
                            encryptionKey = (String) paymentInfo.get("EncryptionKey");
                            secretKey = (String) paymentInfo.get("SecretKey");

                            uEmail = user.getEmail();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void getWeeklyPickupDetails() {
        Intent intent = getIntent();
        uWCDay = intent.getIntExtra("UserWPDay", -1);
        uWCHour = intent.getIntExtra("UserWPHour", -1);
        uWCMins = intent.getIntExtra("UserWPMins", -1);
        uWCDC = intent.getStringExtra("UserWPDC");
        Log.i("Stuff", "Got " + uWCDay);
        if (uWCDay == -1) {

            // Get from DB
            database.getReference().child("Users").child("Customers").child(user.getUid()).child("Info")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        uWCDC = chgS(dataSnapshot.child("WeeklyPickupDCKey").getValue());
                        if (uWCDC != null){
                            uWCDay = chgI(dataSnapshot.child("WeeklyPickupDay").getValue());
                            uWCHour = chgI(dataSnapshot.child("WeeklyPickupHour").getValue());
                            uWCMins = chgI(dataSnapshot.child("WeeklyPickupMins").getValue());
                        }
                    } else {
                        Toast.makeText(TrackActivity.this, "Couldn't retrieve weeky pickup details", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == CALL_REQUEST) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"Thank you Click the button again!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,"Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Progress dialog
    Handler handler = new Handler();
    public ProgressDialog mProgressDialog;
    public void showProgressDialog(String title, String message, Long time) {
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
                    Toast.makeText(TrackActivity.this, "Couldn't connect, please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        }, time);
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            handler.removeCallbacksAndMessages(null);
        }
    }
}
