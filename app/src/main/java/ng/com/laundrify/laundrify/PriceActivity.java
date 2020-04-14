package ng.com.laundrify.laundrify;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ng.com.laundrify.laundrify.utils.PromotionsModel;

public class PriceActivity extends AppCompatActivity {

    private final String TAG = "test";
    ArrayList<Price_Model> price_natives, price_tops, price_bottoms, price_footwear, price_outdoor, price_bedding;
    static ArrayList<PriceFragment> price_fragments = new ArrayList<>();
    Intent thisIntent;

    private PricePagerAdapter pricePagerAdapter;
    ProgressBar priceProgress;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    List<String> promotionKeys;
    private DatabaseReference userDB;
    private boolean fixingDB;
    private DatabaseReference baseDB;
    private DatabaseReference stateRef;
    int availableBonus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        pricePagerAdapter = new PricePagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);


        TabLayout tabLayout = findViewById(R.id.tabs);
        priceProgress = findViewById(R.id.progressBar2);

        tabLayout.setupWithViewPager(mViewPager);

        baseDB = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userDB = baseDB.child("Users").child("Customers").child(user.getUid());

        thisIntent = getIntent();

        // Fetch Available Promo
        priceProgress.setVisibility(View.VISIBLE);
        getPromotions();
    }

    //todo remove dresses and blouse

    // Fetch Available Promo
    private void getPromotions() {
        promotionKeys = new ArrayList<>();
        final double [] lastSeen = new double[1];
        fixingDB = false;

        // Get Last seen
        userDB.child("Info").child("LastSeen").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final String cutString = chgS(dataSnapshot.getValue()).substring(0, 12);
                    lastSeen[0] = chgD(cutString);
                    Log.i("test", "Cut is..." + cutString);

                    // Get Promotions
                    userDB.child("Payment").child("PromotionKeys")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()) {
                                        if (!fixingDB) {

                                            promotionKeys.clear();
                                            for (DataSnapshot under : dataSnapshot.getChildren()) {
                                                promotionKeys.add(under.getKey());
                                            }
                                            final Date timeNow = new Date();
                                            fetchPromotionInfo(promotionKeys, lastSeen[0], timeNow);

                                            Log.i("test", "Total in promo is... " + dataSnapshot.getChildrenCount());
                                        } else {
                                            Toast.makeText(PriceActivity.this, "Fixing Database. Please try again later", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {

                                        // Proceed
                                        priceProgress.setVisibility(View.GONE);
                                        fetchPricing();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                } else {
                    Toast.makeText(PriceActivity.this, "Unexpexted error 009", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchPromotionInfo(final List<String> keys, final double lastSeen, final Date timeNow) {
        final SimpleDateFormat sdfStamp = new SimpleDateFormat("yyyyMMddHHmm");
        final SimpleDateFormat sdfStamp1 = new SimpleDateFormat("yyyyMMdd");


        // Get info of given Keys
        final List<PromotionsModel> promosModel = new ArrayList<>();
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
                                    availableBonus = chgI(dataSnapshot.child("Percentage").getValue());
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
                            Snackbar.make(priceProgress, "You have been blacklisted as a malicious user", Snackbar.LENGTH_LONG).show();
                        }

                        if (!itNextable) {

                            // Proceed
                            Log.i("test", "Promo ended " + promosModel.size());
                            priceProgress.setVisibility(View.GONE);
                            fetchPricing();
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

                            // Proceed
                            Log.i("test", "Promo ended " + promosModel.size());
                            priceProgress.setVisibility(View.GONE);
                            fetchPricing();
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

    // Fetch Available Pricing
    public void fetchPricing () {
        priceProgress.setVisibility(View.VISIBLE);
        final String uLocality = thisIntent.getStringExtra("UserLocality");
        final String uState = thisIntent.getStringExtra("UserState");
        final String uCountry = thisIntent.getStringExtra("UserCountry");
        String lastDCKey = thisIntent.getStringExtra("LastDCKey");

        if (uState == null || uState.matches("")) {

            // Ask for user Address, if absent
            setResult(R.string.NO_ADD_CODE, new Intent());
            finish();
        }

        stateRef = baseDB.child("Locations/Countries/" + uCountry + "/States/" + uState);

        // Try to get Locality from lastDCKey, else get Locality from user Address
        if (lastDCKey != null) {

            // Get Key
            baseDB.child("Users/DryCleaners/" + lastDCKey + "/Info/Locality").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        // Get price with Locality value
                        getFromRef(stateRef.child("Localities/" + dataSnapshot.getValue().toString()));
                    } else {

                        // Nothing found get, from address
                        getFromUserAdd(uLocality, stateRef);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    getFromUserAdd(uLocality, stateRef);
                }
            });
        } else {

            // Nothing found get, from address
            getFromUserAdd(uLocality, stateRef);
        }
    }

    private void getFromUserAdd(String uLocality, DatabaseReference stateRef) {

        // Get Locality from user Address
        priceProgress.setVisibility(View.VISIBLE);
        if (uLocality == null || uLocality.matches("")) {

            // Locality absent, get from user state
            getFromRef(stateRef);
        } else {

            // Get from user Locality
            getFromRef(stateRef.child("Localities/" + uLocality));
        }
    }

    private void getFromRef(DatabaseReference sourceRef) {
        sourceRef.child("Pricing").orderByChild("position").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: " + dataSnapshot.getRef().toString());
                if (dataSnapshot.exists()) {
                    converToPricing(dataSnapshot);
                } else {

                    // Try to check State
                    if (dataSnapshot.getRef().toString().contains("Localities")) {

                        // Can check state
                        getFromRef(stateRef);
                    } else {

                        // Checked state already, still nothing. Ask for user Address
                        Intent data =  new Intent();
                        data.putExtra("ErrorMessage", "Sorry, we're not yet available in your state");
                        setResult(R.string.NO_ADD_CODE, data);
                        Log.e(TAG, "onDataChange: Wrong price ref" + dataSnapshot.getRef());
                        finish();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // Show Pricing
    private void converToPricing(@NonNull DataSnapshot priceDataSnap) {
        priceProgress.setVisibility(View.GONE);
        Log.i(TAG, "converToPricing: " + priceDataSnap.getValue());
        price_fragments.clear();

        // Get each category
        for (DataSnapshot category: priceDataSnap.getChildren()) {

            String title = category.getKey();
            ArrayList<Price_Model> price_items = new ArrayList<>();

            // Get items in category
            for (DataSnapshot item: category.getChildren()) {
                try {
                    Map itemMap = (Map) item.getValue();
                    price_items.add(new Price_Model(chgS(itemMap.get("Link")), availableBonus, chgI(itemMap.get("Price")),
                            chgS(itemMap.get("Header")), chgS(itemMap.get("Description")), item.getKey()));
                } catch (Exception e) {
                    //e.printStackTrace();
                }

            }

            price_fragments.add(new PriceFragment(price_items, new PricingTab(), title));
        }
        pricePagerAdapter.setFragments(price_fragments);
        mViewPager.setAdapter(pricePagerAdapter);
    }

    private String chgS(Object obj) {
        if (obj != null) {
            return String.valueOf(obj);
        } else {
            return null;
        }
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


}
