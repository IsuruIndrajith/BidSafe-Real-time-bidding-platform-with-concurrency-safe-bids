package bid.safe.lumio.BidSafe.controller;

import bid.safe.lumio.BidSafe.dto.BidRequest;
import bid.safe.lumio.BidSafe.model.Bid;
import bid.safe.lumio.BidSafe.ratelimit.RateLimitExceededException;
import bid.safe.lumio.BidSafe.ratelimit.RateLimitService;
import bid.safe.lumio.BidSafe.service.BidService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auctions/{auctionId}/bids")
public class BidController {

    private final BidService bidService;
    private final RateLimitService rateLimitService;

    public BidController(BidService bidService,
                         RateLimitService rateLimitService) {
        this.bidService = bidService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping
    public Bid placeBid(
            @PathVariable Long auctionId,
            @Valid @RequestBody BidRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {

        String email = authentication.getName();

        boolean allowed = rateLimitService.isAllowed(
                "user:" + email
        );
        if (!allowed) {
            throw new RateLimitExceededException(
                    "Too many bid requests. Please try again later."
            );
        }

        return bidService.placeBid(
                auctionId,
                request,
                email,
                idempotencyKey
        );
    }
}