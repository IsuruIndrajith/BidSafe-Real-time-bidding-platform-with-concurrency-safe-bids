package bid.safe.lumio.BidSafe.controller;

import bid.safe.lumio.BidSafe.dto.BidRequest;
import bid.safe.lumio.BidSafe.model.Bid;
import bid.safe.lumio.BidSafe.service.BidService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auctions/{auctionId}/bids")
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @PostMapping
    public Bid placeBid(
            @PathVariable Long auctionId,
            @RequestBody BidRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        return bidService.placeBid(
                auctionId,
                request,
                email
        );
    }
}