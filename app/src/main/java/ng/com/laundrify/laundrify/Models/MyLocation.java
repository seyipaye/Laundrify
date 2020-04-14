package ng.com.laundrify.laundrify.Models;

public class MyLocation {
    public String address;
    public String latitiude;
    public String longitude;
    public String locality;
    public String state;
    public String country;

    public MyLocation(String address, String latitiude, String longitude, String locality, String state, String country) {
        this.address = address;
        this.latitiude = latitiude;
        this.longitude = longitude;
        this.locality = locality;
        this.state = state;
        this.country = country;
    }
}
