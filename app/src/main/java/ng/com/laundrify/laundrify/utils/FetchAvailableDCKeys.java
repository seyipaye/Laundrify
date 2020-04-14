package ng.com.laundrify.laundrify.utils;

import android.os.Handler;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import ng.com.laundrify.laundrify.Models.KeyndLocation;

import static com.android.volley.VolleyLog.TAG;

public class FetchAvailableDCKeys {
    GeoQuery geoQuery;
    int Geo_Rad, MAX_DCs, MAX_Rad;
    ArrayList<KeyndLocation> availableDCKeys;
    private boolean isSearchFinished;

    public FetchAvailableDCKeys(DatabaseReference geoRef, GeoLocation geoLocation, int Geo_Rad, int MAX_DCs, int MAX_Rad, ArrayList<KeyndLocation> availableDCKeys) {
        GeoFire geoFire = new GeoFire(geoRef);
        geoQuery = geoFire.queryAtLocation(geoLocation, Geo_Rad);
        this.Geo_Rad = Geo_Rad;
        this.MAX_DCs = MAX_DCs;
        this.MAX_Rad = MAX_Rad;
        this.availableDCKeys = availableDCKeys;
        isSearchFinished = false;
        Log.i(TAG, "FetchAvailableDCKeys: " + geoLocation.latitude + " " + geoLocation.longitude);
    }

    public void initialize (final AvailableKeysInterface availableKeysInterface) {
            Log.i(getClass().getName(), "intialize: ");
            startTimer((long) 5000, availableKeysInterface);

            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    availableDCKeys.add(new KeyndLocation(key, location));
                }

                @Override
                public void onKeyExited(String key) {
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                }

                @Override
                public void onGeoQueryReady() {
                    //Log.i(getClass().getName(), "onGeoQueryReady: Key is " + availableDCKeys.size() + " and radius is " + geoQuery.getRadius());

                    // Max not yet reach within radius      DEALS WITH RADIUS!

                    if (geoQuery.getRadius() < MAX_Rad) {

                        // Increase radius
                        int currentRadius = (int) geoQuery.getRadius();
                        //Log.i(getClass().getName(), "onGeoQueryReady: Increasing rad, from " + currentRadius + " to " + (currentRadius + Geo_Rad));
                        geoQuery.setRadius(currentRadius + Geo_Rad);

                    } else {
                        isSearchFinished = true;
                    }
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    availableKeysInterface.onNoKey(error.getMessage());
                }
            });
    }

    //Progress dialog
    private Handler handler = new Handler();

    private void startTimer(final Long time, final AvailableKeysInterface availableKeysInterface) {
        handler.postDelayed(new Runnable() {
            public void run() {
                if (isSearchFinished) {
                    if (availableDCKeys.size() > 0) {
                        stopTimer();
                        geoQuery.removeAllListeners();
                        availableKeysInterface.onGetKey(availableDCKeys);
                    } else {

                        // Nothing found
                        stopTimer();
                        availableKeysInterface.onNoKey("Dry Cleaners not found, try adjusting location");
                        Log.i(getClass().getName(), "onGeoQueryReady: Nothing");
                        geoQuery.removeAllListeners();
                    }
                } else {
                    startTimer((long) 5000, availableKeysInterface);
                }

            }
        }, time);
    }

    private void stopTimer() {
            handler.removeCallbacksAndMessages(null);
    }


}
