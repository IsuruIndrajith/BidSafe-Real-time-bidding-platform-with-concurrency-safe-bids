package bid.safe.lumio.BidSafe.ratelimit;

public class RateLimitExceededException
        extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}