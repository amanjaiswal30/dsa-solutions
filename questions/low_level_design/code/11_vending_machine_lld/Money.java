public abstract class Money {
    private final int denominationInCents;

    public Money(int denominationInCents) {
        if (denominationInCents <= 0) {
            throw new IllegalArgumentException("Denomination must be positive");
        }
        this.denominationInCents = denominationInCents;
    }

    public int getDenominationInCents() {
        return denominationInCents;
    }

    public abstract MoneyType getType();

    @Override
    public String toString() {
        return getType() + " ₹" + denominationInCents;
    }
}
