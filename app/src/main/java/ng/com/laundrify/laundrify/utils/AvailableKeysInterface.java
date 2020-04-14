package ng.com.laundrify.laundrify.utils;

import java.util.ArrayList;

import ng.com.laundrify.laundrify.Models.KeyndLocation;

public interface AvailableKeysInterface {
    void onGetKey(ArrayList<KeyndLocation> gottenKeys);
    void onNoKey(String errorMsg);
}
