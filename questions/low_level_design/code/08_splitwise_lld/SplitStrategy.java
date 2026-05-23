import java.util.List;

public interface SplitStrategy {
    List<Split> split(int amount, List<User> users, List<Integer> customAmount);
}
