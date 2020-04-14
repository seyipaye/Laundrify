package ng.com.laundrify.laundrify.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ng.com.laundrify.laundrify.Adapters.DCSelectionAdapter;
import ng.com.laundrify.laundrify.Models.AvailableDCsModel;
import ng.com.laundrify.laundrify.Models.KeyndLocation;
import ng.com.laundrify.laundrify.Models.MyLocation;
import ng.com.laundrify.laundrify.Models.Order;
import ng.com.laundrify.laundrify.R;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static ng.com.laundrify.laundrify.MainActivity.database;
import static ng.com.laundrify.laundrify.MainActivity.locationDB;

public class RequestPickupFragment extends DialogFragment {
    private RequestPickupInterface requestPickupInterface;
    private FrameLayout pickUpBody;
    private ImageView van, bike;
    private RadioGroup rg;
    private CheckBox cbFra, cbQui;
    private EditText pick1Notes;
    private TextView headerText;
    private Button nextBtn, backBtn;
    private ImageButton cancelBtn;
    private ScrollView initialLayout;
    private ProgressBar pickupProgressRing;
    private RecyclerView dcSelectionRV;
    private LinearLayout previousChoice, layoutBelow;
    private Spinner stateSpinner, lgaSpinner;
    private TextView pick1Add;
    private EditText searchCard;
    private ImageView locationOverlay;
    private ProgressBar addressProgress;

    private int AUTOCOMPLETE_REQUEST_CODE = 12;

    private MyLocation currentLocation;
    private Context context;
    private PlacesClient placesClient;
    private static MyLocation lastLocation;
    private static boolean isWeeklyPickup, isScheduledPickup;

    private int MAX_DCs = 10;
    private int MAX_Rad = 71;   // 20 in KM
    private int Geo_Rad = 10;    // in units

    String TAG = "RequestPickup";
    ArrayList<AvailableDCsModel> availableDCsModels = new ArrayList<>();
    Location locationA = new Location("Point A");
    GeoQuery geoQuery;

    private DatabaseReference geoQueryRef;
    boolean gettingAvailableDCs = false;
    static public String lastDCKey;
    public static DCSelectionClickListener dcSelectionClickListener;
    private View dView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        dView = inflater.inflate(R.layout.pickup_dialog, null);

        builder.setView(dView);
        return builder.create();
    }

    @Override
    public void onStart() {

        van = dView.findViewById(R.id.imageView8);
        bike = dView.findViewById(R.id.imageView9);
        rg = dView.findViewById(R.id.radioGroup2);
        cbFra = dView.findViewById(R.id.checkBox1);
        cbQui = dView.findViewById(R.id.checkBox);
        stateSpinner = dView.findViewById(R.id.stateSpinner);
        lgaSpinner = dView.findViewById(R.id.lgaSpinner);
        pick1Notes = dView.findViewById(R.id.pick1Notes);
        pick1Add = dView.findViewById(R.id.pick1Add);
        headerText = dView.findViewById(R.id.headerText);
        searchCard = dView.findViewById(R.id.my_autocomplete_search_input);
        locationOverlay = dView.findViewById(R.id.locationOverlay);
        addressProgress = dView.findViewById(R.id.addressProgress);
        cancelBtn = dView.findViewById(R.id.cancel_button);
        nextBtn = dView.findViewById(R.id.nxtBtn);
        backBtn = dView.findViewById(R.id.backBtn);
        initialLayout = dView.findViewById(R.id.initialLayout);
        dcSelectionRV = dView.findViewById(R.id.dcSelectionRV);
        pickupProgressRing = dView.findViewById(R.id.progressRing);
        previousChoice = dView.findViewById(R.id.previousChoice);
        layoutBelow = dView.findViewById(R.id.layoutBelow);
        pickUpBody = dView.findViewById(R.id.body);
        showProgress();

        //showProgress();
        dcSelectionRV.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        // Initialize the SDK
        Places.initialize(context, context.getString(R.string.placesKey));

        // Create a new Places client instance
        placesClient = Places.createClient(context);

        final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS);

        currentLocation = null;

        // Check if there's no former Address
        if (lastLocation.locality == null) {

            // Permission granted, Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful()) {
                        FindCurrentPlaceResponse response = task.getResult();
                        PlaceLikelihood placeLikelihood = response.getPlaceLikelihoods().get(0);

                        searchCard.setText(placeLikelihood.getPlace().getAddress());
                        currentLocation = new MyLocation(placeLikelihood.getPlace().getAddress(),
                                String.valueOf(placeLikelihood.getPlace().getLatLng().latitude),
                                String.valueOf(placeLikelihood.getPlace().getLatLng().longitude), null, null, null);
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                            Snackbar.make(layoutBelow, "Location not found, use the search tool", Snackbar.LENGTH_SHORT).show();

                        }
                    }
                }
            });
        } else {

            // Assign current Location
            searchCard.setText(lastLocation.address);
            searchCard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_location_black_24dp, 0);
            currentLocation = lastLocation;
        }

        searchCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the autocomplete intent
                Autocomplete.IntentBuilder autoIntent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY, placeFields)
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setCountry("NG");

                // Set text on Card if there is any
                if (searchCard.getText() != null && !searchCard.getText().toString().matches("")) {
                    autoIntent.setInitialQuery(searchCard.getText().toString());
                }
                startActivityForResult(autoIntent.build(context), AUTOCOMPLETE_REQUEST_CODE);
            }
        });

        if (isWeeklyPickup) {
            cbQui.setClickable(false);
        }

        Glide.with(this)
                .load(R.drawable.van_orange)
                .into(van);
        Glide.with(this)
                .load(R.drawable.bike_orange)
                .into(bike);
        van.animate().alpha(1).setDuration(1500);
        bike.animate().alpha(1).setDuration(1500);
        rg.animate().alpha(1).setDuration(2500);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("test", "Next not Clicked");
                dismiss();
                requestPickupInterface.pickupInterfaceNegative();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    nextClicked();
                } catch (Exception e) {

                    e.printStackTrace();
                    goBack();
                    Snackbar.make(layoutBelow, "Couldn't process pick up, try again later", Snackbar.LENGTH_SHORT).show();
                    Log.i(TAG, "onClick: next catch");
                }
            }
        });
        hideProgress();
        super.onStart();
    }

    private void nextClicked() {

        if (!gettingAvailableDCs) {

            // If contains nothing
            if (currentLocation == null) {
                Snackbar.make(layoutBelow, "No address found enter one", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Fetch device Lat and Long if absent
            showProgress();
            if (currentLocation.longitude == null || currentLocation.latitiude == null) {
                GPSTracker gpsTracker = new GPSTracker(context);
                if (gpsTracker.getIsGPSTrackingEnabled()) {
                    Log.i(TAG, "onClick: GPSTracker Location is" + gpsTracker.latitude + gpsTracker.longitude);
                    currentLocation.latitiude = String.valueOf(gpsTracker.latitude);
                    currentLocation.longitude = String.valueOf(gpsTracker.longitude);
                } else {
                    Snackbar.make(layoutBelow, "Too many gps request. Try again later", Snackbar.LENGTH_SHORT).show();
                }
            }

            /*
                if (currentLocation.state == null) {
                    String capitalizedAddress = capitalize(capitalize(capitalize(currentLocation.address, " "), "-"), "/");

                    // Extract state from address
                    String trimmedRefinedAddress = capitalizedAddress.replace(", ", ",").replace(" ,", ",").replace(".", "");
                    String[] commaSplit = trimmedRefinedAddress.split(",");
                    int splitLength = commaSplit.length;

                    if (commaSplit.length > 0) {

                        // Contains State
                        currentLocation.state = commaSplit[splitLength - 1];
                        if (commaSplit.length > 2) {
                            currentLocation.state = commaSplit[splitLength - 2];
                        }
                    } else {

                        // Contains no State, require user address correction
                        goBack();
                        Toast.makeText(MainActivity.this, "Separate State in Address with COMMA", Toast.LENGTH_SHORT).show();
                    }
                }
                */

            gettingAvailableDCs = true;
            Log.i(TAG, "getAvailableDCs: Current Place is " + currentLocation.address
                    + currentLocation.latitiude
                    + currentLocation.longitude
                    + "That's all...more?" + locationDB.getRef());

            locationDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.child("AvailableDryCleaners").exists()) {

                        // Found, ride on
                        geoQueryRef = dataSnapshot.getRef().child("AvailableDryCleaners");
                        locationA.setLatitude(Double.valueOf(currentLocation.latitiude));
                        locationA.setLongitude(Double.valueOf(currentLocation.longitude));
                        ArrayList<KeyndLocation> availableDCKeys = new ArrayList<>();
                        if (lastDCKey != null) {
                            availableDCKeys.add(new KeyndLocation(lastDCKey, null));
                        }

                        GeoLocation thisGeo = new GeoLocation(Double.valueOf(currentLocation.latitiude),
                                Double.valueOf(currentLocation.longitude));

                        new FetchAvailableDCKeys(geoQueryRef, thisGeo, Geo_Rad, MAX_DCs, MAX_Rad, availableDCKeys)
                                .initialize(new AvailableKeysInterface() {
                                    @Override
                                    public void onGetKey(ArrayList<KeyndLocation> gottenKeys) {
                                        fetchDCDetails(gottenKeys);
                                    }

                                    @Override
                                    public void onNoKey(String errorMsg) {
                                        gettingAvailableDCs = false;
                                        Snackbar.make(layoutBelow, errorMsg, Snackbar.LENGTH_SHORT).show();
                                        goBack();
                                    }
                                });
                    } else {

                        // Nothing found, request address change
                        goBack();
                        Snackbar.make(layoutBelow, "No available DryCleaner in your Country. We'll be around soon", Snackbar.LENGTH_SHORT).show();

                        if (locationOverlay.getVisibility() == View.VISIBLE) {
                            fetchAddressData();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

                    /*
                    if (currentLocation.addressComponent != null){

                    } else {
                        Log.i(TAG, "onClick: Address component NULL NULL NULL");
                    }
                     */

        } else {
            Snackbar.make(layoutBelow, "Chill", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void fetchDCDetails(ArrayList<KeyndLocation> gottenKeys) {

        Log.i(TAG, "fetchDCDetails: ");
        dcSelectionClickListener = new DCSelectionClickListener();
        availableDCsModels.clear();
        int dcPosition = 0;
        for (Iterator<KeyndLocation> it = gottenKeys.iterator(); it.hasNext(); ) {
            final KeyndLocation thisModel = it.next();
            final boolean itNextable = it.hasNext();
            final int finalDCPosition = dcPosition;
            dcPosition++;
            database.getReference("Users/DryCleaners").child(thisModel.getKey() + "/Profile")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Log.i(TAG, "onDataChange: ArrayPosition" + finalDCPosition);

                                gettingAvailableDCs = false;
                                //ArrayList comments = (ArrayList) dataSnapshot.child("Comments").getValue();
                                String distance = "";

                                // If first dc Key is last dc used
                                if (lastDCKey != null && finalDCPosition == 0) {

                                    inflatePreviousChoice(new AvailableDCsModel(chgS(dataSnapshot.child("Name").getValue()),
                                            Float.valueOf(dataSnapshot.child("AvgRating").getValue().toString()),
                                            distance, thisModel.getKey(), null));
                                } else {

                                    // Do the norms
                                    if (thisModel.getGeoLocation() != null) {
                                        Location locationB = new Location("point B");
                                        locationB.setLatitude(thisModel.getGeoLocation().latitude);
                                        locationB.setLongitude(thisModel.getGeoLocation().longitude);

                                        distance = String.valueOf(locationA.distanceTo(locationB) / 1000);

                                        if (distance.contains(".")) {
                                            distance = distance.substring(0, distance.indexOf(".") + 2);
                                        }
                                    }

                                    availableDCsModels.add(new AvailableDCsModel(chgS(dataSnapshot.child("Name").getValue()),
                                            Float.valueOf(dataSnapshot.child("AvgRating").getValue().toString()),
                                            distance, thisModel.getKey(), null));

                                    if (!itNextable) {
                                        if (availableDCsModels.size() > 0) {
                                            showList();
                                        } else {
                                            goBack();
                                            Snackbar.make(layoutBelow, "Unexpected error 007", Snackbar.LENGTH_SHORT).show();
                                            Log.e(TAG, "onDataChange: no available DC");
                                        }
                                    }
                                }
                            } else {
                                if (!itNextable) {
                                    if (availableDCsModels.size() > 0) {
                                        showList();
                                    } else {
                                        goBack();
                                        Snackbar.make(layoutBelow, "Unexpected error 007", Snackbar.LENGTH_SHORT).show();
                                        Log.e(TAG, "onDataChange: no available DC");
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

    }

    private void inflatePreviousChoice(AvailableDCsModel availableDCsModel) {
        LinearLayout previousChoiceItem = dView.findViewById(R.id.previousChoiceItem);
        ImageView dcImage = dView.findViewById(R.id.imageView10);
        RatingBar ratingBar = dView.findViewById(R.id.ratingBar);
        TextView dcCompanyName = dView.findViewById(R.id.dcCompanyName);
        TextView ratingText = dView.findViewById(R.id.ratingText);
        TextView reviews = dView.findViewById(R.id.reviews);
        TextView distance = dView.findViewById(R.id.distance);

        dcCompanyName.setText(availableDCsModel.getName());
        distance.setText(String.valueOf(availableDCsModel.getDistance()));
        ratingText.setText(String.valueOf(availableDCsModel.getAvgRating()));
        try {
            ratingBar.setRating(availableDCsModel.getAvgRating());
        } catch (Exception e) {
            e.printStackTrace();
            ratingBar.setRating(0);
        }

        previousChoice.setVisibility(View.VISIBLE);
        previousChoiceItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    requestWithDc(availableDCsModels.get(0));
                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(layoutBelow, "Unnexpexted error 008, Try again later", Snackbar.LENGTH_SHORT).show();

                }
            }
        });

                /*
        Glide.with(holder.itemView)
                .load(modelsData.get(listPosition).getImage())
                .into(dcImage);
                */

    }

    private void showProgress() {
        initialLayout.setVisibility(View.INVISIBLE);
        pickupProgressRing.setVisibility(View.VISIBLE);
        dcSelectionRV.setVisibility(View.GONE);
        previousChoice.setVisibility(View.GONE);
        nextBtn.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.GONE);
        nextBtn.setText("Loading...");
        nextBtn.setClickable(false);
        gettingAvailableDCs = true;
    }

    private void goBack() {
        pickupProgressRing.setVisibility(View.GONE);
        dcSelectionRV.setVisibility(View.INVISIBLE);
        nextBtn.setText("NEXT");
        nextBtn.setClickable(true);
        initialLayout.setVisibility(View.VISIBLE);
        previousChoice.setVisibility(View.GONE);
        nextBtn.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.GONE);
        headerText.setText("Share pickup point");
        gettingAvailableDCs = false;
        Log.i(TAG, "goBack: Went back");
    }

    private void hideProgress() {
        initialLayout.setVisibility(View.VISIBLE);
        pickupProgressRing.setVisibility(View.GONE);
        dcSelectionRV.setVisibility(View.GONE);
        previousChoice.setVisibility(View.GONE);
        nextBtn.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.GONE);
        nextBtn.setText("NEXT");
        nextBtn.setClickable(true);
        gettingAvailableDCs = false;
    }

    private void showList() {
        dcSelectionRV.setAdapter(new DCSelectionAdapter(availableDCsModels));
        initialLayout.setVisibility(View.INVISIBLE);
        pickupProgressRing.setVisibility(View.GONE);
        dcSelectionRV.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.VISIBLE);
        nextBtn.setVisibility(View.GONE);
        headerText.setText("Pick a Dry Cleaner");
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
    }

    // Recycler view listener
    class DCSelectionClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int selectedPosition = dcSelectionRV.getChildLayoutPosition(v);
            try {
                requestWithDc(availableDCsModels.get(selectedPosition));
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar.make(layoutBelow, "Unexpected error 005, Try again later", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void requestWithDc (AvailableDCsModel selectedDCModel) {

            int rbid = rg.getCheckedRadioButtonId();
            RadioButton new_rb = dView.findViewById(rbid);

            //Assign vehicle
            String vehicle = (String) new_rb.getText();

            Log.i(TAG, "requestWithDc: " + currentLocation.state + currentLocation.address);

            if (currentLocation.state != null && !currentLocation.address.matches("")) {

                Order thisOrder = new Order(currentLocation, cbFra.isChecked(), cbQui.isChecked(), isWeeklyPickup, isScheduledPickup,
                        vehicle, String.valueOf(pick1Notes.getText()), selectedDCModel.getKey());

                requestPickupInterface.pickupInterfacePositive(thisOrder);
            } else {
                goBack();
            }
        //Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
    }

    private String chgS(Object obj) {
        return String.valueOf(obj);
    }

    public String capitalize(@NonNull String input, String by) {

        Log.i(TAG, "capitalize: " + input);
        String[] words = input.split(by);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            if (i > 0 && word.length() > 0) {
                builder.append(by);
            }

            if (word.length() > 1) {
                String cap = word.substring(0, 1).toUpperCase() + word.substring(1);
                builder.append(cap);
            } else {
                builder.append(word);
            }
        }
        return builder.toString();
    }

    private void fetchAddressData() {
        final String[] selectedState = new String[1];

        // Fetch List Of State
        locationOverlay.setVisibility(View.VISIBLE);
        addressProgress.setVisibility(View.VISIBLE);
        locationDB.child("StatesList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {

                    // Found list, set listener for selection
                    List<String> oStates = new ArrayList<>();
                    oStates.add("Select state");
                    oStates.addAll((List<String>) dataSnapshot.getValue());

                    //Log.i("test", "1: " + oStates);
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, oStates);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                    stateSpinner.setAdapter(spinnerArrayAdapter);

                    // Select there is previous state
                    if (lastLocation.state != null && !lastLocation.state.matches("")) {
                        stateSpinner.setSelection(spinnerArrayAdapter.getPosition(lastLocation.state));
                    } else {
                        Log.i("test", "No Previous State" + lastLocation.state);
                    }
                    locationOverlay.setVisibility(View.GONE);
                    addressProgress.setVisibility(View.GONE);
                } else {
                    Snackbar.make(layoutBelow, "We're not yet operating in your Country", Snackbar.LENGTH_SHORT).show();

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(layoutBelow, "Sorry, couldn't request Pickup", Snackbar.LENGTH_SHORT).show();

                //hideProgressDialog();
            }
        });

        // On state selected
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, View view, int position, long id) {
                locationOverlay.setVisibility(View.GONE);
                addressProgress.setVisibility(View.GONE); // Just if no match was found
                if (position == 0){
                    //Do nothing
                } else {

                    // Get State Locality
                    selectedState[0] = parent.getItemAtPosition(position).toString();
                    pick1Add.setText(String.format(", %s.", selectedState[0]));
                    locationDB.child("States").child(selectedState[0])
                            .child("LocalitiesList").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            List<String> sLocalities = new ArrayList<>();
                            if (dataSnapshot.exists()) {

                                // Found list
                                sLocalities.add("Select locality");
                                sLocalities.addAll((List<String>) dataSnapshot.getValue());
                                Log.i("Locate", String.valueOf(sLocalities));

                                //Set state localities
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, sLocalities);
                                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                                lgaSpinner.setAdapter(spinnerArrayAdapter);

                                // Select State if there is
                                if (!lastLocation.locality.matches("")) {
                                    lgaSpinner.setSelection(spinnerArrayAdapter.getPosition(lastLocation.locality));
                                }
                            } else {

                                // Restore to default
                                sLocalities.add("Loading...");
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, sLocalities);
                                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                                lgaSpinner.setAdapter(spinnerArrayAdapter);
                                Snackbar.make(layoutBelow, "Sorry, couldn't get Localities for the selected state", Snackbar.LENGTH_SHORT).show();
                            }
                            locationOverlay.setVisibility(View.GONE);
                            addressProgress.setVisibility(View.GONE);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Snackbar.make(layoutBelow, "Sorry, couldn't get Localities for the selected state", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        lgaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {

                    if (!lastLocation.address.matches("") && String.valueOf(parent.getItemAtPosition(position)).matches(lastLocation.address)) {
                        pick1Add.setText(lastLocation.address);
                    } else {
                        pick1Add.setText(String.format("%s, %s.", parent.getItemAtPosition(position), stateSpinner.getSelectedItem()));
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        pick1Add.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                currentLocation = new MyLocation(editable.toString(), null, null, lgaSpinner.getSelectedItem().toString(),
                        stateSpinner.getSelectedItem().toString(), lastLocation.country);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        String selectedState = null, selectedCountry = null, selectedLocality = null;
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                /*
                Place: 47 Marina Rd, AddressComponents{asList=[AddressComponent{name=47, shortName=47, types=[street_number]},
                  AddressComponent{name=Marina Road, shortName=Marina Rd, types=[route]},
                  AddressComponent{name=Lagos Island, shortName=Lagos Island, types=[neighborhood, political]},
                  AddressComponent{name=Lagos, shortName=Lagos, types=[locality, political]},
                  AddressComponent{name=Lagos Island, shortName=Lagos Island, types=[administrative_area_level_2, political]},
                  AddressComponent{name=Lagos, shortName=LA, types=[administrative_area_level_1, political]},
                  AddressComponent{name=Nigeria, shortName=NG, types=[country, political]}]}
                 */

                // User selected a place
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getAddress() + ", " + place.getAddressComponents());
                if (searchCard != null) {
                    searchCard.setText(place.getAddress());
                    List<AddressComponent> addressComponents = place.getAddressComponents().asList();
                    for (Iterator<AddressComponent> it = addressComponents.iterator(); it.hasNext(); ) {
                        AddressComponent thisComponent = it.next();

                        //Log.i(TAG, "onClick: " + thisComponent.getTypes().toString());
                        if (thisComponent.getTypes().toString().contains("country")) {
                            selectedCountry = thisComponent.getName();
                            Log.i(TAG, "onActivityResult: " + thisComponent.getName());
                        } else if (thisComponent.getTypes().toString().contains("administrative_area_level_1")) {
                            selectedState = thisComponent.getName();
                            Log.i(TAG, "onActivityResult: " + thisComponent.getName());
                        } else if (thisComponent.getTypes().toString().contains("administrative_area_level_2")) {
                            selectedLocality = thisComponent.getName();
                            Log.i(TAG, "onActivityResult: " + thisComponent.getName());
                        }
                    }
                    currentLocation = new MyLocation(place.getAddress(),
                            String.valueOf(place.getLatLng().latitude),
                            String.valueOf(place.getLatLng().longitude), selectedLocality, selectedState, selectedCountry);

                    if (locationOverlay != null) {

                        // Restore address selection to default
                        pick1Add.setText(null);
                        locationOverlay.setVisibility(View.VISIBLE);
                    }
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {

                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
                if (locationOverlay != null) {
                    locationOverlay.setVisibility(View.GONE);
                }
                Snackbar.make(layoutBelow, "Location result error, Contact us", Snackbar.LENGTH_SHORT).show();

            } else if (resultCode == RESULT_CANCELED) {

                // The user canceled the operation.
                Log.i(TAG, "onActivityResult: Search Result Cancelled");
                if (locationOverlay != null) {
                    if (locationOverlay.getVisibility() == View.VISIBLE) {
                        fetchAddressData();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static RequestPickupFragment initialize(MyLocation location, boolean isMainWeeklyPickup, boolean isMainScheduledPickup, String mainLastDCKey) {
        isWeeklyPickup = isMainWeeklyPickup;
        isScheduledPickup = isMainScheduledPickup;
        lastLocation = location;
        lastDCKey = mainLastDCKey;
        return new RequestPickupFragment();
    }

    public interface RequestPickupInterface {
        void pickupInterfacePositive (Order order);
        void pickupInterfaceNegative ();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context callerContext) {
        super.onAttach(context);
        context = callerContext;

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            requestPickupInterface = (RequestPickupInterface) callerContext;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
