public class ItemStock {
    private final Item item;
    private int quantity;

    public ItemStock(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public Item getItem()     { return item; }
    public int getQuantity()  { return quantity; }

    public synchronized boolean deductStock(int qty) {
        if (quantity < qty) return false;
        quantity -= qty;
        return true;
    }

    public synchronized void addStock(int qty) { quantity += qty; }
}
