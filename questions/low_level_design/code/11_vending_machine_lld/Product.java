public class Product {
    private final String id;
    private final String name;
    private final int priceInCents;

    public Product(String id, String name, int priceInCents) {
        this.id = id;
        this.name = name;
        this.priceInCents = priceInCents;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPriceInCents() {
        return priceInCents;
    }

    @Override
    public String toString() {
        return name + " (" + id + ", ₹" + priceInCents + ")";
    }
}
