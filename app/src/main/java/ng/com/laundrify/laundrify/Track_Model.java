package ng.com.laundrify.laundrify;

import java.util.List;

public class Track_Model {
    private String id, contact, coTime, coAdd, deTime, deAdd, orderDay, orderHour, dcKey, deliveryStamp, dcCompanyName, deliveryManName, dCPhoneNumber, collectionStamp;
    private List<Items_Model> items;
    private int dp, price, button;
    private boolean paid, quickWash, weeklyPickup;
    public Track_Model(Integer dp, String id, boolean quickWash, boolean weeklyPickup, String contact, String coTime, String coAdd,
                       String deTime, String deAdd, Integer button, Integer price, boolean havePaid, List<Items_Model> items,
                       String orderDay, String orderHour, String dcKey, String deliveryStamp, String collectionStamp, String dcCompanyName,
                       String deliveryManName, String dCPhoneNumber) {
        this.id = id;
        this.quickWash = quickWash;
        this.weeklyPickup = weeklyPickup;
        this.paid = havePaid;
        this.contact = contact;
        this.coTime = coTime;
        this.coAdd = coAdd;
        this.deTime = deTime;
        this.deAdd = deAdd;
        this.items = items;
        this.dp = dp;
        this.button = button;
        this.price = price;
        this.orderDay = orderDay;
        this.orderHour = orderHour;
        this.dcKey = dcKey;
        this.deliveryStamp = deliveryStamp;
        this.collectionStamp = collectionStamp;
        this.dcCompanyName = dcCompanyName;
        this.deliveryManName = deliveryManName;
        this.dCPhoneNumber = dCPhoneNumber;
    }

    public String getDcKey() {
        return dcKey;
    }

    public void setDcKey(String dcKey) {
        this.dcKey = dcKey;
    }

    public String getDeliveryStamp() {
        return deliveryStamp;
    }

    public void setDeliveryStamp(String deliveryStamp) {
        this.deliveryStamp = deliveryStamp;
    }

    public String getOrderDay() {
        return orderDay;
    }

    public void setOrderDay(String orderDay) {
        this.orderDay = orderDay;
    }

    public String getOrderHour() {
        return orderHour;
    }

    public void setOrderHour(String orderHour) {
        this.orderHour = orderHour;
    }

    public boolean isQuickWash() {
        return quickWash;
    }

    public void setQuickWash(boolean quickWash) {
        this.quickWash = quickWash;
    }

    public boolean isWeeklyPickup() {
        return weeklyPickup;
    }

    public void setWeeklyPickup(boolean weeklyPickup) {
        this.weeklyPickup = weeklyPickup;
    }

    public Integer getButton() {
        return button;
    }

    public void setButton(Integer button) {
        this.button = button;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public int getDp() {
        return dp;
    }

    public void setDp(int dp) {
        this.dp = dp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getCoTime() {
        return coTime;
    }

    public void setCoTime(String coTime) {
        this.coTime = coTime;
    }

    public String getCoAdd() {
        return coAdd;
    }

    public void setCoAdd(String coAdd) {
        this.coAdd = coAdd;
    }

    public String getDeTime() {
        return deTime;
    }

    public void setDeTime(String deTime) {
        this.deTime = deTime;
    }

    public String getDeAdd() {
        return deAdd;
    }

    public void setDeAdd(String deAdd) {
        this.deAdd = deAdd;
    }

    public List<Items_Model> getItems() {
        return items;
    }

    public void setItems(List<Items_Model> items) {
        this.items = items;
    }

    public String getDcCompanyName() {
        return dcCompanyName;
    }

    public void setDcCompanyName(String dcCompanyName) {
        this.dcCompanyName = dcCompanyName;
    }

    public String getDeliveryManName() {
        return deliveryManName;
    }

    public void setDeliveryManName(String deliveryManName) {
        this.deliveryManName = deliveryManName;
    }

    public String getdCPhoneNumber() {
        return dCPhoneNumber;
    }

    public void setdCPhoneNumber(String dCPhoneNumber) {
        this.dCPhoneNumber = dCPhoneNumber;
    }

    public String getCollectionStamp() {
        return collectionStamp;
    }

    public void setCollectionStamp(String collectionStamp) {
        this.collectionStamp = collectionStamp;
    }
}
