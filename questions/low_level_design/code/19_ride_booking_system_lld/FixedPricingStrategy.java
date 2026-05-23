public class FixedPricingStrategy  implements PricingStrategy{
    @Override
    public double getEstimatedPrice(Location from, Location to) {
        return 2.0 * from.distanceTo(to);
    }
}
