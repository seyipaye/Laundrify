package ng.com.laundrify.laundrify.Models;

public class Order {
    private MyLocation location;
    private String note, vehicle, dryCleanerKey, dcCompanyName, deliveryManName, dcNotificationToken;
    private boolean isFragrance, isQuickwash, isWeeklyPickup, isScheduledPickup;
    private String dcPNumber;
    final int price = 0;
    final int progress = 0;
    final boolean havePaid = false;

    public Order(MyLocation location, boolean isFragrance, boolean isQuickwash, boolean isWeeklyPickup, boolean isScheduledPickup,
                 String vehicle, String note, String dryCleanerKey) {
        this.location = location;
        this.isFragrance = isFragrance;
        this.isQuickwash = isQuickwash;
        this.isWeeklyPickup = isWeeklyPickup;
        this.isScheduledPickup = isScheduledPickup;
        this.note = note;
        this.vehicle = vehicle;
        this.dryCleanerKey = dryCleanerKey;
    }

    public void setDcCompanyName(String dcCompanyName) {
        this.dcCompanyName = dcCompanyName;
    }

    public void setDeliveryManName(String deliveryManName) {
        this.deliveryManName = deliveryManName;
    }

    public void setDcPNumber(String dcPNumber) {
        this.dcPNumber = dcPNumber;
    }

    public String getDcNotificationToken() {
        return dcNotificationToken;
    }

    public void setDcNotificationToken(String dcNotificationToken) {
        this.dcNotificationToken = dcNotificationToken;
    }

    public MyLocation getLocation() {
        return location;
    }

    public String getNote() {
        return note;
    }

    public String getVehicle() {
        return vehicle;
    }

    public String getDryCleanerKey() {
        return dryCleanerKey;
    }

    public String getDcCompanyName() {
        return dcCompanyName;
    }

    public String getDeliveryManName() {
        return deliveryManName;
    }

    public boolean isFragrance() {
        return isFragrance;
    }

    public boolean isQuickwash() {
        return isQuickwash;
    }

    public boolean isWeeklyPickup() {
        return isWeeklyPickup;
    }

    public boolean isScheduledPickup() {
        return isScheduledPickup;
    }

    public String getDcPNumber() {
        return dcPNumber;
    }

    public int getPrice() {
        return price;
    }

    public int getProgress() {
        return progress;
    }

    public boolean havePaid() {
        return havePaid;
    }
}
