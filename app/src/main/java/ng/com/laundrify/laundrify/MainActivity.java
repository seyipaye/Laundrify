package ng.com.laundrify.laundrify;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ng.com.laundrify.laundrify.Adapters.CustomAdapter;
import ng.com.laundrify.laundrify.Models.MyLocation;
import ng.com.laundrify.laundrify.Models.Order;
import ng.com.laundrify.laundrify.utils.RequestPickupFragment;
import ng.com.laundrify.laundrify.utils.SendNotification;
import ng.com.laundrify.laundrify.utils.Utils;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        RequestPickupFragment.RequestPickupInterface {
    //Add Progress dialog to Profile settings

    //Recall to allow for Notification Settings

    // Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
    // intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
    // intent.putExtra(Settings.EXTRA_CHANNEL_ID, myNotificationChannel.getId());
    // startActivity(intent);

    // TODO handle track activity to stand alone

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private RecyclerView.Adapter<CustomAdapter.MyViewHolder> adapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;
    private List<Model> models;
    public static View.OnClickListener myOnClickListener;
    private int sPMinute, sPHour;
    FlipperLayout flipperLayout;
    private ArrayList<String> slideurls;
    private TextView pick2Details;
    private String sPampm;

    private FirebaseAuth mAuth;
    public static FirebaseDatabase database;
    private DatabaseReference customerDB, orderDB;
    public static DatabaseReference locationDB;
    FirebaseUser user;
    int PRICEREQUESTCODE = 202;

    //todo reflect weekly pick up time and date in app instantly
    // Change focus of inner white
    //User info
    static Map userMap;
    String fname, lname, pNumber, orderID, weeklyCollectDay, scheduleCollectDay, uWPDC;
    int uWPDay, uWPHour, uWPMins;
    boolean uWeeklyPickup;
    TextView headName;
    TextView headEmail;

    List<String> slideActions;

    //Order Vairables
    Calendar sPCal;
    String coTime;
    String coStamp;
    String deTime;
    String deStamp;
    Calendar wPCal;

    // Pickup Dialog
    private MyLocation uLocation;

    int ACCESS_FINE_LOCATION_CODE = 59;

    String TAG = "test";
    String lastDCKey;
    private DialogFragment requestPickupFrag;


    static void assignMap(Map data) {
        userMap = data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Fire base
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        customerDB = database.getReference().child("Users").child("Customers").child(user.getUid());
        orderDB = database.getReference().child("Orders");

        //Ads
        MobileAds.initialize(this, "ca-app-pub-3727881467469894~8208127195");
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

        // Nav. drawer icon
        mDrawerLayout = findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = findViewById(R.id.navView);
        View v = navigationView.getHeaderView(0);
        headName = v.findViewById(R.id.headName);
        headEmail = v.findViewById(R.id.headEmail);
        navigationView.setNavigationItemSelectedListener(this);
        getUserInfo(userMap);
    }

    @Override
    public void onResume() {

        //Rounded Corners
        super.onResume();
        ImageView right, left;
        right = findViewById(R.id.imageViewRight);
        left = findViewById(R.id.imageViewLeft);
        Glide.with(this)
                .load(R.drawable.bottomright)
                .into(right);
        Glide.with(this)
                .load(R.drawable.bottomleft)
                .into(left);

        //flipper
        SharedPreferences sharedPreferences = this.getSharedPreferences("ng.com.laundrify.laundrify", Context.MODE_PRIVATE);
        try {
            slideurls = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.
                    getString("slideurls", ObjectSerializer.serialize(new ArrayList<String>())));
            slideActions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.
                    getString("slideActions", ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("shared", "Shared failed");
        }
        flipperLayout = findViewById(R.id.flipper);
        setFlipperLayout(slideurls.size());

        //CardView
        myOnClickListener = new MyOnClickListener(this);

        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        models = new ArrayList<>();
        models.add(new Model(R.drawable.request, "Request pick up", "We'll get to you in a couple of minutes, just with the touch of a button", 0));
        models.add(new Model(R.drawable.money, "Our Pricing", "Here at Laundrify, we offer low prices you can trust", 1));
        models.add(new Model(R.drawable.weekly, "Weekly pick up?", "Your laundry just got easier! We'll be at your door step anytime anywhere", 2));
        models.add(new Model(R.drawable.schedule, "Schedule a pick up", "Busy now? How about we meet later", 3));
        models.add(new Model(R.drawable.trackpix, "Track Orders", "Bordered about your laundry?. Check out it's progress", 4));

        adapter = new CustomAdapter(models);
        recyclerView.setAdapter(adapter);

        uWeeklyPickup = false;
    }

    private void setFlipperLayout(int viewCount) {
        flipperLayout.clearFlipperViews();
        for (int i = 0; i < viewCount; i++) {
            FlipperView view = new FlipperView(getBaseContext());
            view.setImageUrl(slideurls.get(i))
                    .setImageScaleType(ImageView.ScaleType.CENTER_CROP)
            .setDescription("Request pickup");

            flipperLayout.addFlipperView(view);

            view.setOnFlipperClickListener(new FlipperView.OnFlipperClickListener() {
                @Override
                public void onFlipperClick(FlipperView flipperView) {
                    flipperClick(flipperLayout.getCurrentPagePosition());
                }
            });
        }
    }

    private void flipperClick(int flipperPosition) {
        if (flipperPosition < slideActions.size()) {
            String actionString = slideActions.get(flipperPosition);
            if (actionString.contains("Whatsapp")) {
                String[] adSplit = actionString.split("!");
                contact(adSplit[1], adSplit[2]);
            } else if (actionString.contains("http")) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(actionString));
                startActivity(browserIntent);
            } else if (!actionString.matches("Nothing")){
                Snackbar.make(findViewById(android.R.id.content), actionString, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuReaction(menuItem);
        return true;
    }

    //Get User Info
    public void getUserInfo(Map map) {
        //Log.i("test", "Map Gotten is" + map);
        if (map !=null) {
            fname = (String) map.get("FirstName");
            lname = (String) map.get("LastName");
            pNumber = (String) map.get("PhoneNumber");
            // Todo remove L
            lastDCKey = (String) map.get("LastDryCleanerKey");
            if (map.get("WeeklyPickupDay") != null) {
                Long l1 = (Long)map.get("WeeklyPickupDay");
                uWPDay = l1.intValue();

                Long l2 = (Long)map.get("WeeklyPickupHour");
                uWPHour = l2.intValue();

                Long l3 = (Long)map.get("WeeklyPickupMins");
                uWPMins = l3.intValue();

                uWeeklyPickup = true;
            } else {
                uWPDay = 0;
                uWPHour = 0;
                uWPMins = 0;
            }
            if (map.get("WeeklyPickupDCKey") != null) {
                uWPDC = map.get("WeeklyPickupDCKey").toString();
            } else {
                uWPDC = null;
            }
            if (fname == null) {
                fname = "";
            }
            if (lname == null) {
                lname = "";
            }
            if (pNumber == null) {
                pNumber = "";
            }

            Map lastLocationMap = (Map) map.get("LastLocation");

            if (lastLocationMap != null) {
                uLocation = new MyLocation(Utils.chgS(lastLocationMap.get("Address")), Utils.chgS(lastLocationMap.get("Latitude")),
                        Utils.chgS(lastLocationMap.get("Longitude")), Utils.chgS(lastLocationMap.get("Locality")),
                        Utils.chgS(lastLocationMap.get("State")), Utils.chgS(lastLocationMap.get("Country")));
            } else {
                uLocation = new MyLocation(null, null, null, null, null, "Nigeria");
            }

            locationDB = database.getReference().child("Locations").child("Countries").child(uLocation.country);
        } else {
            finish();
            Toast.makeText(this, "Something happened please contact us", Toast.LENGTH_SHORT).show();
        }

        headName.setText(lname + " " + fname);
        headEmail.setText(user.getEmail());
        final SimpleDateFormat sdfStamp = new SimpleDateFormat("yyyyMMddHHmm");
        customerDB.child("Info/LastSeen").setValue(sdfStamp.format(new Date()));
    }

    // Navigation Drawer
    public void menuReaction(MenuItem menuItem) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dView = inflater.inflate(R.layout.settings_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog;
        final TextInputEditText setEdit1 = dView.findViewById(R.id.settingsEditText1);
        final TextInputEditText setEdit2 = dView.findViewById(R.id.settingsEditText2);
        final TextInputEditText setEdit3 = dView.findViewById(R.id.settingsEditText3);
        final TextInputEditText setEdit4 = dView.findViewById(R.id.settingsEditText4);
        final TextInputEditText setEdit5 = dView.findViewById(R.id.settingsEditText5);
        final TextInputEditText setEdit6 = dView.findViewById(R.id.settingsEditText6);
        final TextView tv1 = dView.findViewById(R.id.textView1);
        final TextView tv2 = dView.findViewById(R.id.textView2);
        final Spinner state = dView.findViewById(R.id.stateSpinner);
        final Spinner lga = dView.findViewById(R.id.lgaSpinner);
        TextInputLayout textInput1 = dView.findViewById(R.id.textInputLayout1);
        TextInputLayout textInput2 = dView.findViewById(R.id.textInputLayout2);
        TextInputLayout textInput3 = dView.findViewById(R.id.textInputLayout3);
        final TextInputLayout textInput4 = dView.findViewById(R.id.textInputLayout4);
        TextInputLayout textInput5 = dView.findViewById(R.id.textInputLayout5);
        TextInputLayout textInput6 = dView.findViewById(R.id.textInputLayout6);
        menuItem.setChecked(false);

        switch (menuItem.getItemId()) {

            // Selected change name
            case R.id.dspName:
                textInput1.setVisibility(View.VISIBLE);
                textInput2.setVisibility(View.VISIBLE);
                textInput1.setHint("First Name");

                if (!fname.matches("")) {
                    setEdit1.setText(fname);
                    setEdit2.setText(lname);
                }

                builder.setTitle("Change Display name")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                uploadNewName(setEdit1.getText().toString(), setEdit2.getText().toString());
                            }})
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //Snackbar.make(findViewById(android.R.id.content), "Change Failed", Snackbar.LENGTH_LONG).show();

                            }});
                builder.setView(dView);
                dialog = builder.create();
                dialog.show();
                break;
            case R.id.phnNumber:
                textInput3.setVisibility(View.VISIBLE);
                if (!pNumber.matches("")) {
                    setEdit3.setText(pNumber);
                }

                builder.setTitle("Change Phone number")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                changePhone(setEdit3.getText().toString());
                            }})
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Snackbar.make(findViewById(android.R.id.content), "Change Failed", Snackbar.LENGTH_LONG).show();
                            }});
                builder.setView(dView);
                dialog = builder.create();
                dialog.show();
                break;
            case R.id.chgAddress:
                changeAddress();
                break;
            case R.id.chgPassword:
                textInput5.setVisibility(View.VISIBLE);
                textInput6.setVisibility(View.VISIBLE);
                builder.setTitle("Change Phone number")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if (setEdit6.getText().toString().matches("null") || setEdit6.getText().toString().matches("nil")) {
                                    updatePassword(setEdit5.getText().toString(), setEdit6.getText().toString());
                                } else {
                                    Toast.makeText(MainActivity.this, "Please note that password must not contain programming syntax", Toast.LENGTH_SHORT).show();
                                }
                            }})
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Snackbar.make(findViewById(android.R.id.content), "Change Failed", Snackbar.LENGTH_LONG).show();
                            }});
                builder.setView(dView);
                dialog = builder.create();
                dialog.show();
                break;
            case R.id.referFriend:
                Toast.makeText(this, "Sorry, it's unavailabe now. You rock!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rate:
                rateThisApplication(MainActivity.this);
                break;
            case R.id.contact:
                contact("+2347012454239", "Hi i'm " + lname + " " + fname + ", I'd love to...");
                break;
            case R.id.terms:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://laundrify.com.ng/about-us/"));
                startActivity(browserIntent);
                break;
            case R.id.privacy:
                Intent browserIntent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://laundrify.com.ng/privacy-and-policy/"));
                startActivity(browserIntent1);
                break;
            case R.id.logOut:
                logOut();
                break;
        }
    }

    private void changeAddress() {
        showProgressDialog("", "Loading", (long) 5000);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dView = inflater.inflate(R.layout.settings_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog;
        final TextInputEditText setEdit4 = dView.findViewById(R.id.settingsEditText4);
        final TextView tv1 = dView.findViewById(R.id.textView1);
        final TextView tv2 = dView.findViewById(R.id.textView2);
        final Spinner state = dView.findViewById(R.id.stateSpinner);
        final Spinner lga = dView.findViewById(R.id.lgaSpinner);
        final TextInputLayout textInput4 = dView.findViewById(R.id.textInputLayout4);
        final String[] stateSelected = new String[1];
        final String[] lgaSelected = new String[1];


        // Change Address clicked
        tv1.setVisibility(View.VISIBLE);
        tv2.setVisibility(View.VISIBLE);
        state.setVisibility(View.VISIBLE);
        lga.setVisibility(View.VISIBLE);
        textInput4.setVisibility(View.VISIBLE);

        locationDB.child("StatesList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {

                    // Found list, set listener for selection
                    List<String> oStates = new ArrayList<>();
                    oStates.add("Select state");
                    oStates.addAll((List<String>) dataSnapshot.getValue());

                    Log.i("test", "1: " + oStates);
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, oStates);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                    state.setAdapter(spinnerArrayAdapter);

                    // Select if there is previous state
                    if (uLocation.state != null) {
                        state.setSelection(spinnerArrayAdapter.getPosition(uLocation.state));
                    } else {
                        Log.i("test", "No Previous State");
                    }
                    hideProgressDialog();
                } else {
                    Toast.makeText(MainActivity.this, "We're not yet operating in your Country", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Sorry, couldn't request Pickup", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        });

        // On state selected
        state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, View view, int position, long id) {
                hideProgressDialog();
                if (position == 0) {
                    // Do nothing
                } else {
                    // Do something
                    // Get State Locality
                    stateSelected[0] = parent.getItemAtPosition(position).toString();
                    setEdit4.setText(", " + parent.getItemAtPosition(position));
                    locationDB.child("States/" + parent.getItemAtPosition(position) +
                            "/LocalitiesList").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                List<String> sLocalities = new ArrayList<>();

                                // Found list
                                sLocalities.add("Select locality");
                                sLocalities.addAll((List<String>) dataSnapshot.getValue());
                                Log.i("Locate", String.valueOf(sLocalities));

                                //Set state locality
                                if (sLocalities != null) {
                                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, sLocalities);
                                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                                    lga.setAdapter(spinnerArrayAdapter);

                                    // Select State if there is
                                    try {
                                        if (uLocation.locality != null) {
                                            lga.setSelection(((ArrayAdapter<String>) lga.getAdapter()).getPosition(uLocation.locality));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Sorry, couldn't get Localities for the selected state", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(MainActivity.this, "Sorry, couldn't get Localities for the selected state", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        lga.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {

                    setEdit4.setText(parent.getItemAtPosition(position) + ", " + state.getSelectedItem() + ".");
                    lgaSelected[0] = parent.getItemAtPosition(position).toString();
                    if (uLocation.address != null && String.valueOf(parent.getItemAtPosition(position)).matches(uLocation.locality)) {
                        setEdit4.setText(uLocation.address);
                    } else {
                        setEdit4.setText(String.format("%s, %s.", parent.getItemAtPosition(position), state.getSelectedItem()));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder.setTitle("Change Address")
                .setPositiveButton("Ok",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int id){

                        if (state.getSelectedItemPosition() == 0) {
                            Snackbar.make(findViewById(android.R.id.content),"No state selected",Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        showProgressDialog("", "Changing Address", (long) 60000);
                        Map uploadMap = new HashMap();

                        uploadMap.put("Address", setEdit4.getText().toString());
                        uploadMap.put("Locality", lgaSelected[0]);
                        uploadMap.put("State", stateSelected[0]);
                        uploadMap.put("Country", uLocation.country);

                        customerDB.child("Info/LastLocation").setValue(uploadMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                // Done reflect changes
                                uLocation = new MyLocation(setEdit4.getText().toString(), null, null, lgaSelected[0], stateSelected[0],
                                        uLocation.country);
                                hideProgressDialog();
                                Log.i(TAG, "onComplete: change address" + uLocation.address + uLocation.locality + uLocation.state
                                        + uLocation.country);
                                Snackbar.make(findViewById(android.R.id.content),"Address changed",Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }})

                .setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int id){
                        Snackbar.make(findViewById(android.R.id.content),"Change Failed",Snackbar.LENGTH_LONG).show();
                    }});
        builder.setView(dView);
        dialog=builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void logOut() {
        mAuth.signOut();
        finish();
    }

    private void doShits() {
        ArrayList<String> areas = new ArrayList<>();
        ArrayList<String> place = new ArrayList<>();
        ArrayList<String> all = new ArrayList<>();
        String[] longShit = {"Amuwo-Odofin Local Government" ,
                "Badagry West Local Council Development Area" ,
                "Coker / Aguda Local Council Development Area" ,
                "Ejigbo Local Council Development Area" ,
                "Mosan-Okunola Local Council Development Area" ,
                "Mushin Local government" ,
                "Ojo Local Government" ,
                "Ojodu Local Council Development Area" ,
                "Ojokoro Local Council development Area" ,
                "Onigbongbo Local Council Development Area" ,
                "Oshodi / Isolo Local Government" ,
                "Agbado / Oke-Odo Local Council Development Area" ,
                "Agboyi-Ketu Local Council Development Area" ,
                "Agege Local Government" ,
                "Ajeromi-Ifelodun Local Government" ,
                "Alimosho Local Government" ,
                "Apapa Local Government" ,
                "Badagry Local Government" ,
                "Apapa-Iganmu Local Council Development Area" ,
                "Bariga Local Council Development Area" ,
                "Egbe - Idimu Local Council Development Area" ,
                "Epe Local Government",
                "Eredo Local Council Development Area" ,
                "Eti-Osa East Local Council Development Area" ,
                "Eti-Osa Local Government" ,
                "Iba Local Council Development Area" ,
                "Ibeju Lekki Local Government" ,
                "Ibogbo / Bayeku Local Council Development Area" ,
                "Ifako-Ijaye Local Government",
                "Ifelodun Local Council Development Area" ,
                "Igando-Ikotun Local Council Development Area" ,
                "Ijede Local Council Development Area",
                "Ikeja Local Government Area",
                "Ikorodu Local Government" ,
                "Ikorodu North Local Council Development Area" ,
                "Ikorodu West Local Council Development Area" ,
                "Ikosi / Ejinrin Local Council Development Area" ,
                "Imota Local Council Developoment Area" ,
                "Iru - Victoria Island Local Council Development Area" ,
                "Isolo Local Council development Area",
                "Itire-Ikate Local Council Development Area" ,
                "Kosofe Local Government",
                "Lagos Island East Local Council Development Area" ,
                "Lagos Mainland Local Government" ,
                "Lekki Local Council Development Area" ,
                "Odi-Olowo / Ojuwoye Local Council Development Area" ,
                "Olorunda Local Council Development Area" ,
                "Oriade Local Council development Area" ,
                "Orile - Agege Local Council Development Area" ,
                "Oto-Awori Local Council Development Area" ,
                "Somolu Local Government" ,
                "Surulere Local Government" ,
                "Ayobo-Ipaja Local Council Development Area" ,
                "Ikosnsheri Local Council Development Area" ,
                "Ikoyi - Obalende Local Council Development Area" ,
                "Lagos Island Local Government" ,
                "Yaba Local Council Development Area"};

        for (int i = 0; i <= longShit.length ; i++) {
            if (i == longShit.length) {
                //all.add("Select LGA");
                //Collections.sort(areas);
                all.addAll(areas);
                //all.add("-----");
                //Collections.sort(place);
                all.addAll(place);
                Collections.sort(all);
                Log.i("Shit", String.valueOf(all));
                Log.i("Shit", String.valueOf(all.size()));
                database.getReference().child("Locations").child("Countries").child("Nigeria").child("States").child("Lagos")
                        .child("LocalitiesList").setValue(all);

            } else {
                if (longShit[i].contains("Development")) {
                    String[] split = longShit[i].split(" Local");
                    String spaced = split[0].replace(" - ", "-").replace(" / ", "/");

                    areas.add(spaced);
                } else {
                    String[] split = longShit[i].split(" Local");
                    String spaced = split[0].replace(" - ", "-").replace(" / ", "/");
                    place.add(spaced);
                }
            }
        }
    }

    public void mainContact (View v) {
        contact("+2347012454239", "Hi i'm " + lname + " " + fname + ", I'd love to...");
    }

    private void contact(String toNumber, String message) {
        try {
            toNumber = toNumber.replace("+", "").replace(" ", "");

            Intent sendIntent = new Intent("android.intent.action.MAIN");
            sendIntent.putExtra("jid", toNumber + "@s.whatsapp.net");
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setPackage("com.whatsapp");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Please make sure you have whatsapp", Snackbar.LENGTH_LONG).show();
        }
    }

    public void rateThisApplication(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try
        {
            context.startActivity(goToMarket);
        }
        catch (ActivityNotFoundException e)
        {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
        finally
        {
            uri = null;
            goToMarket = null;
        }
    }

    private void changePhone(String number) {
        customerDB.child("Info").child("PhoneNumber").setValue(number);
        pNumber = number;
    }

    private void uploadNewName(String first, String last) {
        customerDB.child("Info").child("FirstName").setValue(first);
        customerDB.child("Info").child("LastName").setValue(last);
        fname = first;
        lname = last;
    }

    private void updatePassword(final String oldPass, final String newPass) {
        showProgressDialog("", "Loading", (long) 10000);
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), oldPass);
        // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        hideProgressDialog();
                                        Snackbar.make(findViewById(android.R.id.content), "Change Successful", Snackbar.LENGTH_LONG).show();
                                        customerDB.child("Info").child("Password").setValue(newPass);
                                    } else {
                                        hideProgressDialog();
                                        Snackbar.make(findViewById(android.R.id.content), "Something went wrong. Please try again later", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), "Authentication failed, check old password", Snackbar.LENGTH_LONG).show();
                            hideProgressDialog();
                        }
                    }
                });
    }

    //Card listener
    private class MyOnClickListener implements View.OnClickListener {
        private Context context;
        private MyOnClickListener(Context context) {
            this.context = context;
        }
        @Override
        public void onClick(View v) {
            int selected = recyclerView.getChildLayoutPosition(v);
            switch (selected) {
                case 0:
                    requestPickup(false, false);
                    break;
                case 1:
                    Intent priceIntent = new Intent(getApplicationContext(), PriceActivity.class);
                    priceIntent.putExtra("UserLocality", uLocation.locality);
                    priceIntent.putExtra("UserState", uLocation.state);
                    priceIntent.putExtra("UserCountry", uLocation.country);
                    priceIntent.putExtra("LastDCKey", lastDCKey);
                    startActivityForResult(priceIntent, PRICEREQUESTCODE);
                    break;
                case 2:
                    if (uWeeklyPickup) {
                        weekly_pickup(uWPDay, uWPHour, uWPMins);
                    } else {
                        weekly_pickup(0, 0, 0);
                    }
                    break;
                case 3:
                    schedule();
                    break;
                case 4:
                    track_order();
                    break;
            }
        }
    }

    // Pickup clicked
    public void requestPickup(boolean isWeeklyPickup, boolean isScheduled){
        if (requestPickupFrag != null && requestPickupFrag.isVisible()) {
            Toast.makeText(this, "Take chill...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    getLocationPermission();
                    return;
                }

        requestPickupFrag = RequestPickupFragment.initialize(uLocation, isWeeklyPickup, isScheduled, lastDCKey);
        requestPickupFrag.show(getSupportFragmentManager(), "Request pickup Fragment");
    }

    @Override
    public void pickupInterfaceNegative() {
        Snackbar.make(findViewById(android.R.id.content), "Cancelled pickup " + this.getString(R.string.confusedFace), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void pickupInterfacePositive(final Order order) {

        showProgressDialog("Please wait", "Loading...", (long) 20000);
        requestPickupFrag.dismiss();
        Log.i("Stuff", "Check Location");
        DatabaseReference DCsDB = database.getReference().child("Users").child("DryCleaners").child(order.getDryCleanerKey()).child("Info");
        DCsDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Got all? lunch it up
                if (dataSnapshot.getValue() != null) {
                    String pickTitle = "Confirm your request " + MainActivity.this.getString(R.string.grinningFaceWithSmilingEyes);
                    String pickMsg = (String) dataSnapshot.child("PickupMessage").getValue();
                    order.setDcCompanyName(Utils.chgS(dataSnapshot.child("CompanyName").getValue()));
                    order.setDeliveryManName(Utils.chgS(dataSnapshot.child("DeliveryManName").getValue()));
                    order.setDcPNumber(Utils.chgS(dataSnapshot.child("PhoneNumber").getValue()));
                    order.setDcNotificationToken(Utils.chgS(dataSnapshot.child("NotificationToken").getValue()));

                    /*
                        pickTitle = "Sorry " + MainActivity.this.getString(R.string.faceWithPleadingEyes);
                        pickMsg = "we don't operate at " + localityT + " yet, but you may get rewarded by telling DryCleaners around about our app \"LAUNDRIFY(DC)\"." +
                                " Thanks, we owe you a lot.";
                        if (!pickupAdd.matches(uAddress) || !localityT.matches(uLocality) || !stateT.matches(uState)) {
                            newAdd = true;
                            showSmallDialog();
                        } else {
                            newAdd = false;
                            showSmallDialog();
                        }
                    */

                    //Log.i(TAG, "onDataChange: " + order.getLocation().address + ", " + order.getLocation().locality  + ", " + order.getLocation().state);
                    //Log.i(TAG, "onDataChange: " + uLocation.address  + ", " + uLocation.locality + ", " + uLocation.state);

                    // Check if no former adress
                    if (uLocation.address != null || uLocation.locality != null || uLocation.state != null) {

                        //Check if matches current address
                        if (!order.getLocation().address.matches(uLocation.address) || !order.getLocation().locality.matches(uLocation.locality) ||
                                !order.getLocation().state.matches(uLocation.state)) {
                            Log.i("stuff", "Contains Message");
                            showSmallDialog(pickTitle, pickMsg, order, true);

                        } else {
                            showSmallDialog(pickTitle, pickMsg, order, false);
                        }
                    } else {
                        showSmallDialog(pickTitle, pickMsg, order, true);
                    }
                } else {
                    hideProgressDialog();
                    Toast.makeText(MainActivity.this, "Unexpected error 006, Try again later ", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
       }

    // Pickup's next Clicked
    private void showSmallDialog(String pickTitle, String pickMsg, final Order order, final boolean isNewAdd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.AlertDialogCustom));
        builder.setTitle(pickTitle)
                .setMessage(pickMsg)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        //Remove Order
                        if (order.isWeeklyPickup()) {
                            Snackbar.make(findViewById(android.R.id.content), "Cancelled plan " +
                                    MainActivity.this.getString(R.string.confusedFace), Snackbar.LENGTH_LONG).show();
                        } else if (order.isScheduledPickup()) {
                            Snackbar.make(findViewById(android.R.id.content), "Cancelled schedule " +
                                    MainActivity.this.getString(R.string.confusedFace), Snackbar.LENGTH_LONG).show();
                        } else  {
                            Snackbar.make(findViewById(android.R.id.content), "Cancelled pickup " +
                                    MainActivity.this.getString(R.string.confusedFace), Snackbar.LENGTH_LONG).show();
                        }

                        if (isNewAdd) {
                            updateAdd(order.getLocation());
                        }
                    }});
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK, so save the selectedItems results somewhere
                    // or return them to the component that opened the dialog
                    Log.e("test", "Confirm");
                    if (order.isWeeklyPickup() || order.isScheduledPickup()) {
                        Snackbar.make(findViewById(android.R.id.content), "Plan completed " +
                                MainActivity.this.getString(R.string.faceThrowingAKiss) + ". We'll be here at the specified time.", Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Request confirmed " +
                                MainActivity.this.getString(R.string.faceThrowingAKiss) + ". We'll be here before an hour", Snackbar.LENGTH_LONG).show();
                    }
                    // Upload Order details
                    makeOrderMap(order, isNewAdd);
                }
            });

        final AlertDialog dialog = builder.create();
        dialog.show();
        hideProgressDialog();
    }

    // Shared small dialog
    private void smallDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.AlertDialogCustom));
        if (title.equals("")) {} else {
            builder.setTitle(title);
        }
        builder .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }});
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    // upload Order
    public void makeOrderMap(Order order, boolean isNewAdd) {
        showProgressDialog("", "Authenticating Order...", (long) 60000);

        // Order uploads
        String preId = orderDB.push().getKey();
        Map<String, java.io.Serializable> orderInfo = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMM, yyyy | hh:mm aa", Locale.ENGLISH);
        SimpleDateFormat sdfStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);

        orderInfo.put("UserID", user.getUid());
        orderInfo.put("FirstName", fname);
        orderInfo.put("LastName", lname);
        orderInfo.put("PhoneNumber", pNumber);
        orderInfo.put("Note", order.getNote());
        orderInfo.put("Fragrance", order.isFragrance());
        orderInfo.put("Quickwash", order.isQuickwash());
        orderInfo.put("Vehicle", order.getVehicle());
        orderInfo.put("PickupAddress", order.getLocation().address);
        orderInfo.put("Locality", order.getLocation().locality);
        orderInfo.put("State", order.getLocation().state);
        orderInfo.put("Price",order.getPrice());
        orderInfo.put("Progress",order.getProgress());
        orderInfo.put("HavePaid",order.havePaid());
        orderInfo.put("WeeklyPickup", order.isWeeklyPickup());
        orderInfo.put("DryCleanerKey", order.getDryCleanerKey());
        orderInfo.put("DCCompanyName", order.getDcCompanyName());
        orderInfo.put("DeliveryManName", order.getDeliveryManName());
        orderInfo.put("DCPhoneNumber", order.getDcPNumber());


        if (order.isWeeklyPickup()) {

            Log.i("test", "Uploading. " + wPCal.getTime());
            Log.i(TAG, "makeOrderMap: Uploading. " + wPCal.getTime());
            //Is WeeklyPickup
            orderID = "WP-" + preId.substring(1, 5) + preId.substring(16);

            //Get Collection details
            final Date coDate = wPCal.getTime();
            coTime = sdf.format(coDate);
            coStamp = sdfStamp.format(coDate);

            //Get Delivery details
            wPCal.add(Calendar.DAY_OF_YEAR, 7);
            final Date deDate = wPCal.getTime();
            deTime = sdf.format(deDate);
            deStamp = sdfStamp.format(deDate);

            //Put details
            orderInfo.put("CollectionTime", coTime);
            orderInfo.put("CollectionStamp", coStamp);
            orderInfo.put("DeliveryTime", deTime);
            orderInfo.put("DeliveryStamp", deStamp);
            uploadOrder(orderInfo, orderID, order, isNewAdd);
            uWPDC = order.getDryCleanerKey();
            customerDB.child("Info").child("WeeklyPickupDCKey").setValue(order.getDryCleanerKey());

            if (lastDCKey == null || !order.getDryCleanerKey().matches(lastDCKey)) {
                customerDB.child("Info").child("LastDryCleanerKey").setValue(order.getDryCleanerKey());
            }
        } else if (order.isScheduledPickup()) {

            //Is Scheduled
            orderInfo.put("CollectionTime", scheduleCollectDay);
            orderID = "SP-" + preId.substring(1, 5) + preId.substring(16);

            //Get Collection details
            final Date coDate = sPCal.getTime();
            coTime = sdf.format(coDate);
            coStamp = sdfStamp.format(coDate);

            //Get Delivery details
            sPCal.add(Calendar.DAY_OF_YEAR, 4);
            final Date deDate = sPCal.getTime();
            deTime = sdf.format(deDate);
            deStamp = sdfStamp.format(deDate);

            //Put details
            orderInfo.put("CollectionTime", coTime);
            orderInfo.put("CollectionStamp", coStamp);
            orderInfo.put("DeliveryTime", deTime);
            orderInfo.put("DeliveryStamp", deStamp);
            uploadOrder(orderInfo, orderID, order, isNewAdd);

            if (lastDCKey == null || !order.getDryCleanerKey().matches(lastDCKey)) {
                customerDB.child("Info").child("LastDryCleanerKey").setValue(order.getDryCleanerKey());
            }
        } else {

            //It's a Normal Request
            orderID = preId.substring(1, 5) + preId.substring(16);

            //Get Collection details
            final Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.HOUR_OF_DAY, 1);
            final Date coDate = cal.getTime();
            coTime = sdf.format(coDate);
            coStamp = sdfStamp.format(coDate);

            //Get Delivery details
            cal.add(Calendar.DAY_OF_YEAR, 4);
            final Date deDate = cal.getTime();
            deTime = sdf.format(deDate);
            deStamp = sdfStamp.format(deDate);

            //Put details
            orderInfo.put("CollectionTime", coTime);
            orderInfo.put("CollectionStamp", coStamp);
            orderInfo.put("DeliveryTime", deTime);
            orderInfo.put("DeliveryStamp", deStamp);
            uploadOrder(orderInfo, orderID, order, isNewAdd);

            if (lastDCKey == null || !order.getDryCleanerKey().matches(lastDCKey)) {
                customerDB.child("Info").child("LastDryCleanerKey").setValue(order.getDryCleanerKey());
            }
        }
    }

    private void uploadOrder(final Map<String, Serializable> orderInfo, final String nOrderID, final Order order, final boolean isNewAdd) {

        //Upload to main
        orderDB.child(nOrderID).setValue(orderInfo, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                //Upload to customer DB
                customerDB.child("Orders").child(nOrderID).child("TimeStamp").setValue(coStamp, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        //Upload to DryCleaner's DB
                        database.getReference().child("Users").child("DryCleaners").child(order.getDryCleanerKey()).child("Orders").child("Pending")
                                .child(nOrderID).child("TimeStamp").setValue(coStamp, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                hideProgressDialog();

                                if (order.getDcNotificationToken() != null) {
                                    new SendNotification(
                                            MainActivity.this,
                                            order.getDcNotificationToken(),
                                            "New order received",
                                            "Order for pickup at " + order.getLocation().address + " before"
                                                    + orderInfo.get("CollectionTime").toString().substring(20));
                                }

                                if (isNewAdd) {
                                    updateAdd(order.getLocation());
                                }
                            }});
                    }
                });
            }
        });
    }

    // Update Address
    public void updateAdd(final MyLocation newLocation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.AlertDialogCustom));
            builder.setTitle("Confirm change")
            .setMessage("Do you want to change your default location to " + newLocation.address + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        showProgressDialog("", "Changing Address", (long) 60000);
                        Map uploadMap = new HashMap();

                        uploadMap.put("Address", newLocation.address);
                        uploadMap.put("Latitude", newLocation.latitiude);
                        uploadMap.put("Longitude", newLocation.longitude);
                        uploadMap.put("Locality", newLocation.locality);
                        uploadMap.put("State", newLocation.state);
                        uploadMap.put("Country", newLocation.country);

                        customerDB.child("Info/LastLocation").setValue(uploadMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                uLocation = newLocation;
                                hideProgressDialog();
                            }
                        });
                    }})
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Dismiss
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    //Weekly Pickup clicked
    private void weekly_pickup(final int pDay, final int pHour, int pMins) {
        DialogFragment dialog = FullscreenDialog.newInstance(pDay, pHour, pMins);
        Log.i("Great!", "here " + ":" + pDay + ":" + pHour + ":" + pMins);
        ((FullscreenDialog) dialog).setCallback(new FullscreenDialog.Callback() {
            @Override
            public void onActionClick(int nDay, int nHour, int nMin, Calendar nPickupCal) {
                wPCal = nPickupCal;
                //Log.i("test", nDay + ":" + nHour + ":" + nMin + new SimpleDateFormat("EEEE dd MMM, yyyy | hh:mm aa").format(pickupCal.getTime()));
                if (nDay == 0) {
                    Snackbar.make(findViewById(android.R.id.content), "Your 10% bonus is waiting for you here" , Snackbar.LENGTH_LONG).show();
                    customerDB.child("Info").child("WeeklyPickupDCKey").removeValue();
                    uWPDay = 0;
                    uWPHour = 0;
                    uWPMins = 0;
                    uWPDC = null;
                } else {
                    uploadWeeklyP (nDay, nHour, nMin);
                }
            }
        });
        dialog.show(getSupportFragmentManager(), "tag");
    }

    // Upload selected Values
    private void uploadWeeklyP(final int nDay, final int nHour, final int nMin) {
        showProgressDialog("Please wait", "Creating plan...", (long) 20000);

        //Get the giving values and ask when to start
        String option1, option2;

        //Get dates
        option1 = "";
        option2 = "";

        Calendar cal = Calendar.getInstance();
        Calendar option1Cal = Calendar.getInstance();
        Calendar option2Cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, nHour);
        cal.set(Calendar.MINUTE, nMin);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMM, yyyy | hh:mm aa.", Locale.ENGLISH);

        //Get day to start
        do {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == nDay) {
                if (option1.matches("")) {
                    option1 = sdf.format(cal.getTime());
                    option1Cal.setTime(cal.getTime());
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                } else {
                    option2 = sdf.format(cal.getTime());
                    option2Cal.setTime(cal.getTime());
                }
            } else {
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        } while (option1.matches("") || option2.matches(""));

        //Inflate Dialog
            LayoutInflater inflater = LayoutInflater.from(this);
            final View dView = inflater.inflate(R.layout.weekly_dialog_ask, null);
            final RadioGroup rg = dView.findViewById(R.id.wdRG);
            RadioButton rb = dView.findViewById(R.id.wdRB);
            RadioButton rb2 = dView.findViewById(R.id.wdRB2);

        //Set Text for options
            rb.setText(option1);
            rb2.setText(option2);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.AlertDialogCustom));
        final String finalOption1 = option1;
        final String finalOption2 = option2;
        final Calendar finalOption1Cal = option1Cal;
        final Calendar finalOption2Cal = option2Cal;
        builder.setTitle("When do you want to start ?")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            int rbid = rg.getCheckedRadioButtonId();
                            if (rbid == R.id.wdRB) {
                                uploadOption(finalOption1Cal.getTime());
                            } else if (rbid == R.id.wdRB2) {
                                uploadOption(finalOption2Cal.getTime());
                            } else {

                            //No option selected
                            Log.i("test", weeklyCollectDay + " and " + finalOption1 + ":" + finalOption2);
                            Toast.makeText(MainActivity.this, "Please select when to start", Toast.LENGTH_SHORT).show();
                            }

                        }

                        private void uploadOption(Date time) {
                            showProgressDialog("Please wait", "Configuring plan...", (long) 60000);
                            wPCal.setTime(time);
                            uWPDay = nDay;
                            uWPHour = nHour;
                            uWPMins = nMin;
                            Log.i("test", "Initializing... " + wPCal.getTime());
                            customerDB.child("Info").child("WeeklyPickupDay").setValue(nDay, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    customerDB.child("Info").child("WeeklyPickupHour").setValue(nHour, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            customerDB.child("Info").child("WeeklyPickupMins").setValue(nMin, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                    hideProgressDialog();
                                                    requestPickup(true, false);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    })

                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Snackbar.make(findViewById(android.R.id.content), "Cancelled plan " +
                                    MainActivity.this.getString(R.string.confusedFace) + ". Your 10% discount is still here.", Snackbar.LENGTH_LONG).show();

                        }});
            builder.setView(dView);
            AlertDialog dialog = builder.create();
            dialog.show();
            hideProgressDialog();
    }

    //Schedule Pickup
    private void schedule() {
        TimePickerDialog timePickerDialog;
        sPCal = Calendar.getInstance();
        sPMinute = sPCal.get(Calendar.MINUTE);
        sPHour = sPCal.get(Calendar.HOUR_OF_DAY);

        timePickerDialog = new TimePickerDialog(new ContextThemeWrapper(this, R.style.FullscreenDialogTheme), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (hourOfDay > 12) {
                    sPampm = "PM";
                    sPHour = hourOfDay - 12;
                } else if (hourOfDay == 12) {
                    sPampm = "PM";
                    sPHour = hourOfDay;
                } else {
                    sPampm = "AM";
                    sPHour = hourOfDay;
                }
                sPMinute = minute;

                sPCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                sPCal.set(Calendar.MINUTE, minute);


                //Do something
                if (hourOfDay > 8 && hourOfDay < 17 ) {
                    //Update database
                    //Snackbar.make(findViewById(android.R.id.content), "We'll be here " + String.format("%02d:%02d", sPHour, sPMinute) + sPampm, Snackbar.LENGTH_LONG).show();
                    //Get date and upload details
                    showProgressDialog("", "Fixing Schedule...", (long) 60000);
                    schedulenupload();
                    Log.e("test", "You choose " + hourOfDay + ":" + sPMinute);
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Sorry, we only operate 9AM - 4PM", Snackbar.LENGTH_LONG).show();
                }
            }
        }, sPHour, sPMinute,false);
        timePickerDialog.show();
    }

    private void schedulenupload() {
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMM, yyyy");
        sdf.format(today);
        scheduleCollectDay = sdf.format(today) + " | " + sPHour + ":" + sPMinute;
        requestPickup(false, true);
        hideProgressDialog();
    }

    //Track Order
    private void track_order () {
        Intent trackInt = new Intent(getApplicationContext(), TrackActivity.class);
        trackInt.putExtra("UserWPDay" , uWPDay);
        trackInt.putExtra("UserWPHour" , uWPHour);
        trackInt.putExtra("UserWPMins" , uWPMins);
        trackInt.putExtra("UserWPDC" , uWPDC);
        Log.i("Stuff", "Passed " + uWPDay);
        startActivity(trackInt);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "onActivityResult: Main Result");

        if (requestCode == PRICEREQUESTCODE) {
            if (resultCode == R.string.NO_ADD_CODE) {

                // Prompt to enter address
                changeAddress();
                if (data.getStringExtra("ErrorMessage") != null) {
                    Toast.makeText(this, data.getStringExtra("ErrorMessage"), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Set Address to view Local Pricing", Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == RESULT_OK) {
                // Do nothing
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getLocationPermission() {
        hideProgressDialog();
        if (ContextCompat.checkSelfPermission( this, ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions( this, new String[] {  ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},
                    ACCESS_FINE_LOCATION_CODE );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_FINE_LOCATION_CODE) {
            Toast.makeText(this, "Try it, again", Toast.LENGTH_SHORT).show();
            hideProgressDialog();
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
                    Toast.makeText(MainActivity.this, "Couldn't connect, please try again later.", Toast.LENGTH_SHORT).show();
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

