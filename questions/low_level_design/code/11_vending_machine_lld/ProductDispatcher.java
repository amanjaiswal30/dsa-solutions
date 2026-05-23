public class ProductDispatcher {
    private boolean operational = true;

    public boolean isOperational() {
        return operational;
    }

    public void setOperational(boolean operational) {
        this.operational = operational;
    }

    public boolean dispense(Product product) {
        return operational && product != null;
    }
}
