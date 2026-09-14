package bid.safe.lumio.BidSafe.controller;

import bid.safe.lumio.BidSafe.dto.AuctionRequest;
import bid.safe.lumio.BidSafe.model.Auction;
import bid.safe.lumio.BidSafe.service.AuctionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @PostMapping
    public Auction createAuction(@RequestBody AuctionRequest request) {
        return auctionService.createAuction(request);
    }

    @GetMapping
    public List<Auction> getAllAuctions() {
        return auctionService.getAllAuctions();
    }

    @GetMapping("/{id}")
    public Auction getAuctionById(@PathVariable Long id) {
        return auctionService.getAuctionById(id);
    }
}
