public class Main {
    public static void main(String[] args) {
        Logger parentLogger = LoggerFactory.getLogger("com.shop");
        Logger orderLogger = LoggerFactory.getLogger("com.shop.order");
        Logger paymentLogger = LoggerFactory.getLogger("com.shop.order.payment");

        LoggerFactory.addAppender(parentLogger, new ConsoleAppender());
        LoggerFactory.addAppender(paymentLogger, new FileAppender("payments.log"));
        LoggerFactory.addAppender(paymentLogger, new ErrorConsoleAppender());
        LoggerFactory.setLogLevel(LogLevel.INFO);

        parentLogger.setLevel(LogLevel.DEBUG);
        parentLogger.addAppender(new InMemoryAppender());


        orderLogger.debug("Order pipeline warm-up complete");
        paymentLogger.info("Payment started for order=101");
        paymentLogger.error("Payment failed for order=101");

        Logger isolatedLogger = LoggerFactory.getLogger("com.shop.audit");
        isolatedLogger.setAdditive(false);
        isolatedLogger.addAppender(new ConsoleAppender());
        isolatedLogger.info("Audit trail event with isolated appender");
    }
}
