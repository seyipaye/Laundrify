package ng.com.laundrify.laundrify;

public class Items_Model {
   private String itemName, itemPrice;

   public Items_Model (String itemName, String itemPrice) {
       this.itemName = itemName;
       this.itemPrice = itemPrice;
   }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }
}
