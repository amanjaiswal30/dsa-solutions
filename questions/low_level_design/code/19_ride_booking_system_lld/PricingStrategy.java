public interface PricingStrategy {
    double getEstimatedPrice(Location from, Location to);
}
