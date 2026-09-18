package bid.safe.lumio.BidSafe.service;

import bid.safe.lumio.BidSafe.dto.AuctionRequest;
import bid.safe.lumio.BidSafe.model.Auction;
import bid.safe.lumio.BidSafe.model.Item;
import bid.safe.lumio.BidSafe.repository.AuctionRepository;
import bid.safe.lumio.BidSafe.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final ItemRepository itemRepository;
    private static final Logger log =
            LoggerFactory.getLogger(AuctionService.class);

    public AuctionService(
            AuctionRepository auctionRepository,
            ItemRepository itemRepository) {

        this.auctionRepository = auctionRepository;
        this.itemRepository = itemRepository;
    }

    public Auction createAuction(AuctionRequest request) {

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Auction auction = new Auction();

        auction.setItem(item);
        auction.setStartTime(request.getStartTime());
        auction.setEndTime(request.getEndTime());
        auction.setStatus("UPCOMING");
        auction.setCurrentHighestBid(item.getStartingPrice());

        Auction savedAuction = auctionRepository.save(auction);

        log.info(
                "AUCTION_CREATED auctionId={} itemId={}",
                savedAuction.getId(),
                item.getId()
        );

        return auctionRepository.save(auction);
    }

    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll();
    }

    public Auction getAuctionById(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
    }
}
