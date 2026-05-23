import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class ATM {
    public enum OperationalStatus {
        UP,
        DOWN,
        MAINTENANCE
    }

    private final String atmId;
    private final String location;
    private final EnumMap<NoteDenomination, Integer> cashInventory = new EnumMap<>(NoteDenomination.class);
    private OperationalStatus status;

    public ATM(String atmId, String location) {
        if (atmId == null || atmId.isBlank()) {
            throw new IllegalArgumentException("ATM id cannot be empty");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("ATM location cannot be empty");
        }
        this.atmId = atmId;
        this.location = location;
        this.status = OperationalStatus.UP;

        for (NoteDenomination denomination : NoteDenomination.values()) {
            cashInventory.put(denomination, 0);
        }
    }

    public String getAtmId() {
        return atmId;
    }

    public String getLocation() {
        return location;
    }

    public OperationalStatus getStatus() {
        return status;
    }

    public void setStatus(OperationalStatus status) {
        this.status = status;
    }

    public boolean isOperational() {
        return status == OperationalStatus.UP;
    }

    public void restock(NoteDenomination denomination, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Restock count must be greater than 0");
        }
        cashInventory.put(denomination, cashInventory.get(denomination) + count);
    }

    public int getAvailableNoteCount(NoteDenomination denomination) {
        return cashInventory.get(denomination);
    }

    public void deductCash(Map<NoteDenomination, Integer> dispensePlan) {
        for (Map.Entry<NoteDenomination, Integer> entry : dispensePlan.entrySet()) {
            NoteDenomination denomination = entry.getKey();
            int nextCount = cashInventory.get(denomination) - entry.getValue();
            if (nextCount < 0) {
                throw new IllegalStateException("Invalid dispense plan for ATM cash inventory");
            }
            cashInventory.put(denomination, nextCount);
        }
    }

    public int getTotalCash() {
        int total = 0;
        for (Map.Entry<NoteDenomination, Integer> entry : cashInventory.entrySet()) {
            total += entry.getKey().getValue() * entry.getValue();
        }
        return total;
    }

    public Map<NoteDenomination, Integer> getCashInventory() {
        return Collections.unmodifiableMap(cashInventory);
    }
}
