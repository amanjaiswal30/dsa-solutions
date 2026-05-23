import java.util.List;

public class NearestDeliveryPartnerAssignmentStrategy implements PartnerAssignmentStrategy {
    @Override
    public DeliveryPartner assignPartner(List<DeliveryPartner> deliveryPartnerList, Order order) {
        double minDistance = Double.MAX_VALUE;
        DeliveryPartner nearestPartner = null;

        for (DeliveryPartner partner : deliveryPartnerList) {
            if (!partner.isAvailable() || partner.getLocation() == null) {
                continue;
            }

            double dist = partner.getLocation().distanceTo(order.getSource());
            if (dist < minDistance) {
                minDistance = dist;
                nearestPartner = partner;
            }
        }
        return nearestPartner;
    }
}
