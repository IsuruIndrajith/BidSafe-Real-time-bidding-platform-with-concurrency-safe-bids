package bid.safe.lumio.BidSafe.service;

import bid.safe.lumio.BidSafe.dto.BidRequest;
import bid.safe.lumio.BidSafe.dto.BidUpdate;
import bid.safe.lumio.BidSafe.model.Auction;
import bid.safe.lumio.BidSafe.model.Bid;
import bid.safe.lumio.BidSafe.model.User;
import bid.safe.lumio.BidSafe.repository.AuctionRepository;
import bid.safe.lumio.BidSafe.repository.BidRepository;
import bid.safe.lumio.BidSafe.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

@Service
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public BidService(
            BidRepository bidRepository,
            AuctionRepository auctionRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate) {

        this.bidRepository = bidRepository;
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public Bid placeBid(
            Long auctionId,
            BidRequest request,
            String email) {

        // 1. Find auction
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() ->
                        new RuntimeException("Auction not found"));

        // 2. Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // 3. Read current highest bid
        double currentHighest = auction.getCurrentHighestBid();

        // 4. Compare new bid with current highest
        if (request.getAmount() <= currentHighest) {
            throw new RuntimeException(
                    "Bid must be higher than current highest bid");
        }

        // 5. Create bid
        Bid bid = new Bid();

        bid.setAuction(auction);
        bid.setUser(user);
        bid.setAmount(request.getAmount());
        bid.setCreatedAt(LocalDateTime.now());

        // 6. Save bid
        Bid savedBid = bidRepository.save(bid);

        // 7. Update auction's highest bid
        auction.setCurrentHighestBid(request.getAmount());

        // 8. Save auction
        auctionRepository.save(auction);

        // Create WebSocket update
        BidUpdate update = new BidUpdate(
                auction.getId(),
                user.getUsername(),
                request.getAmount()
        );

        // Broadcast update
        messagingTemplate.convertAndSend(
                "/topic/auctions/" + auctionId,
                update
        );

        return savedBid;
    }
}