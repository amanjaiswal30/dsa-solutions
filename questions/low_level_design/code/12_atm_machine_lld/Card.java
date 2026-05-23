public class Card {
    private final String cardNumber;
    private final String pin;
    private final BankAccount linkedAccountNumner;

    public Card(String cardNumber, String pin, BankAccount linkedAccountNumner) {
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.linkedAccountNumner = linkedAccountNumner;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public BankAccount getLinkedAccountNumner() {
        return linkedAccountNumner;
    }

    public boolean isPinValid(String enteredPin) {
        return pin.equals(enteredPin);
    }
}
