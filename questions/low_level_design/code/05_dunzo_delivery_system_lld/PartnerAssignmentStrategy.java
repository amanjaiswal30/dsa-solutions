import java.util.List;

public interface PartnerAssignmentStrategy {
    DeliveryPartner assignPartner(List<DeliveryPartner> deliveryPartnerList, Order order);
}
