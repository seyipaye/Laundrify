package ng.com.laundrify.laundrify.Models;

import com.firebase.geofire.GeoLocation;

public class KeyndLocation {
    private String key;
    private GeoLocation geoLocation;

    public KeyndLocation(String key, GeoLocation geoLocation) {
        this.key = key;
        this.geoLocation = geoLocation;
    }

    public String getKey() {
        return key;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }
}
