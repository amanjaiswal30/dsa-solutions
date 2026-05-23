public class Coin extends Money {

    public Coin(int denominationInCents) {
        super(denominationInCents);
    }

    @Override
    public MoneyType getType() {
        return MoneyType.COIN;
    }
}
