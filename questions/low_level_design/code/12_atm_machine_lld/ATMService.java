import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ATMService {
    private static ATMService instance;
    private final ATM atm;
    private final Map<String, Card> cards = new HashMap<>();
    private final Map<String, BankAccount> accounts = new HashMap<>();
    private ATMState atmState = ATMState.IDLE;

    private Card currentCard;

    private ATMService(ATM atm) {
        this.atm = atm;
    }

    public static synchronized ATMService getInstance(ATM atm) {
        if (instance == null) {
            instance = new ATMService(atm);
        }
        return instance;
    }

    public void addAccount(BankAccount account) {
        accounts.put(account.getAccountNumber(), account);
    }

    public void addCard(Card card) {
        cards.put(card.getCardNumber(), card);
    }

    public String getAtmState() {
        return atmState.name();
    }

    public void insertCard(String cardNumber) {
        if (!atm.isOperational()) {
            throw new IllegalStateException("ATM is currently unavailable");
        }
        if (atmState != ATMState.IDLE) {
            throw new IllegalStateException("ATM is not ready for card insertion");
        }
        if (currentCard != null) {
            throw new IllegalStateException("A card is already inserted");
        }
        Card card = cards.get(cardNumber);
        if (card == null) {
            throw new IllegalArgumentException("Invalid card");
        }
        currentCard = card;
        atmState = ATMState.CARD_INSERTED;
    }

    public void authenticate(String pin) {
        ensureCardInserted();
        if (!currentCard.isPinValid(pin)) {
            ejectCard();
            throw new IllegalArgumentException("Invalid PIN. Card ejected");
        }
        if (currentCard.getLinkedAccountNumner() == null) {
            ejectCard();
            throw new IllegalStateException("Linked account not found");
        }
        atmState = ATMState.AUTHENTICATED;
    }

    public double checkBalance() {
        ensureAuthenticated();
        BankAccount account = getCurrentAccount();
        account.addRecord(TransactionType.BALANCE_ENQUIRY, "Balance enquiry from " + atm.getAtmId());
        return account.getBalance();
    }

    public List<TransactionRecord> miniStatement(int count) {
        ensureAuthenticated();
        BankAccount account = getCurrentAccount();
        account.addRecord(TransactionType.MINI_STATEMENT, "Mini statement from " + atm.getAtmId());
        return account.getMiniStatement(count);
    }

    public Map<NoteDenomination, Integer> withdraw(int amount) {
        ensureAuthenticated();
        if (!atm.isOperational()) {
            throw new IllegalStateException("ATM is currently unavailable");
        }
        if (getAtmCashBalance() == 0) {
            throw new IllegalStateException("ATM is out of money");
        }
        Map<NoteDenomination, Integer> plan = buildDispensePlan(amount);

        getCurrentAccount().debit(amount, "Withdrawal from " + atm.getAtmId());
        atm.deductCash(plan);
        return plan;
    }

    public void restock(NoteDenomination denomination, int count) {
        atm.restock(denomination, count);
    }

    public int getAtmCashBalance() {
        return atm.getTotalCash();
    }

    public Map<NoteDenomination, Integer> getCashInventory() {
        return Collections.unmodifiableMap(atm.getCashInventory());
    }

    public void ejectCard() {
        currentCard = null;
        atmState = ATMState.IDLE;
    }

    private Map<NoteDenomination, Integer> buildDispensePlan(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        int pending = amount;
        Map<NoteDenomination, Integer> plan = new LinkedHashMap<>();

        for (NoteDenomination denomination : NoteDenomination.values()) {
            int available = atm.getAvailableNoteCount(denomination);
            int required = pending / denomination.getValue();
            int toUse = Math.min(available, required);
            if (toUse > 0) {
                plan.put(denomination, toUse);
                pending -= toUse * denomination.getValue();
            }
        }

        if (pending != 0) {
            throw new IllegalStateException("ATM cannot dispense requested amount with current notes");
        }
        return plan;
    }

    private void ensureCardInserted() {
        if (currentCard == null) {
            throw new IllegalStateException("Insert card first");
        }
        if (atmState != ATMState.CARD_INSERTED && atmState != ATMState.AUTHENTICATED) {
            throw new IllegalStateException("ATM is not in card session state");
        }
    }

    private void ensureAuthenticated() {
        if (currentCard == null || currentCard.getLinkedAccountNumner() == null) {
            throw new IllegalStateException("Authenticate first");
        }
        if (atmState != ATMState.AUTHENTICATED) {
            throw new IllegalStateException("ATM is not in authenticated state");
        }
    }

    private BankAccount getCurrentAccount() {
        BankAccount account = currentCard.getLinkedAccountNumner();
        if (account == null) {
            throw new IllegalStateException("Linked account not found");
        }
        return account;
    }
}
