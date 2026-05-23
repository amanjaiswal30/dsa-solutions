import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Customer {
    private final String customerId;
    private final String name;
    private final List<Card> cards = new ArrayList<>();

    public Customer(String customerId, String name) {
        this.customerId = customerId;
        this.name = name;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }
}
