import java.util.UUID;

public class Item {
    final String itemId;
    final String itemName;
    final double itemPrice;

    public Item(String itemName, double itemPrice) {
        this.itemId = UUID.randomUUID().toString();
        this.itemName = itemName;
        this.itemPrice = itemPrice;
    }

    public String getItemId()    { return itemId; }
    public String getItemName()  { return itemName; }
    public double getItemPrice() { return itemPrice; }

    @Override
    public String toString() {
        return itemName + " ($" + itemPrice + ")";
    }
}
