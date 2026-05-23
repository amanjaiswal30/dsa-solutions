public class Cash extends Money {

    public Cash(int denominationInCents) {
        super(denominationInCents);
    }

    @Override
    public MoneyType getType() {
        return MoneyType.NOTE;
    }
}
