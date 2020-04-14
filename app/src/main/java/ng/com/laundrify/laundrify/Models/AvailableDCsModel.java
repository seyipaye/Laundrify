package ng.com.laundrify.laundrify.Models;

import java.util.ArrayList;

public class AvailableDCsModel {
    String name, distance, key;
    ArrayList<String> comments;
    Float avgRating;

    public AvailableDCsModel(String name, Float avgRating, String distance, String key, ArrayList<String> comments) {
        this.name = name;
        this.avgRating = avgRating;
        this.comments = comments;
        this.distance = distance;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public float getAvgRating() {
        return avgRating;
    }

    public String getDistance() {
        return distance;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    public String getKey() {
        return key;
    }
}
