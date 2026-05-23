public class Main {
    public static void main(String[] args) {
        BookingService service = BookingService.getInstance();

        String userId = service.registerUser("Aman");
        service.registerCar("KA01AA1111", "Swift", 10000);
        service.registerCar("KA01BB2222", "i20", 20000);

        String selectedCarId = "KA01BB2222"; // user-selected
        Booking booking = service.createBooking(userId, selectedCarId, 50, 1.2);
        System.out.println("Booking ID: " + booking.getId());
        System.out.println("Estimated amount: " + booking.getEstimatedAmount());

        service.pay(booking.getId(), 2000, PaymentMethod.UPI);
        service.completeTrip(booking.getId(), 20070);

        int remaining = service.getRemainingAmount(booking.getId());
        System.out.println("Remaining after trip: " + remaining);

        if (remaining > 0) {
            service.pay(booking.getId(), remaining, PaymentMethod.CARD);
        }

        System.out.println("Final status: " + service.getBooking(booking.getId()).getBookingStatus());
    }
}
