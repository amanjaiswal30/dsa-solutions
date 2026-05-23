import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MoneyHolder {
    private final TreeMap<Integer, Integer> denominationCount = new TreeMap<>(Collections.reverseOrder());
    // track whether a denomination is a coin or cash note
    private final TreeMap<Integer, MoneyType> denominationType = new TreeMap<>();

    public void addMoney(Money money) {
        if (money.getType() == null) {
            throw new IllegalArgumentException("Money type cannot be null");
        }
        int denom = money.getDenominationInCents();
        denominationCount.put(denom, denominationCount.getOrDefault(denom, 0) + 1);
        denominationType.put(denom, money.getType());
    }

    public void addMoney(List<Money> moneyList) {
        for (Money money : moneyList) {
            addMoney(money);
        }
    }

    public int getTotalBalance() {
        int total = 0;
        for (Map.Entry<Integer, Integer> entry : denominationCount.entrySet()) {
            total += entry.getKey() * entry.getValue();
        }
        return total;
    }

    public boolean canMakeChange(int amountInCents) {
        return simulateDispense(amountInCents) != null;
    }

    public List<Money> dispenseChange(int amountInCents) {
        List<Money> change = simulateDispense(amountInCents);
        if (change == null) {
            throw new IllegalStateException("Cannot dispense exact change for amount: " + amountInCents);
        }
        for (Money money : change) {
            int denomination = money.getDenominationInCents();
            int count = denominationCount.getOrDefault(denomination, 0);
            if (count <= 0) {
                throw new IllegalStateException("Internal money holder inconsistency for denomination: " + denomination);
            }
            if (count == 1) {
                denominationCount.remove(denomination);
                denominationType.remove(denomination);
            } else {
                denominationCount.put(denomination, count - 1);
            }
        }
        return change;
    }

    private List<Money> simulateDispense(int amountInCents) {
        if (amountInCents < 0) {
            return null;
        }
        if (amountInCents == 0) {
            return new ArrayList<>();
        }
        int remaining = amountInCents;
        List<Money> change = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : denominationCount.entrySet()) {
            int denomination = entry.getKey();
            int available = entry.getValue();
            MoneyType type = denominationType.getOrDefault(denomination, MoneyType.COIN);
            while (remaining >= denomination && available > 0) {
                change.add(type == MoneyType.COIN ? new Coin(denomination) : new Cash(denomination));
                remaining -= denomination;
                available--;
            }
        }
        return remaining == 0 ? change : null;
    }
}
