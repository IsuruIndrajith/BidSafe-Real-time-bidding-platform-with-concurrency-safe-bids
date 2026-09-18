package bid.safe.lumio.BidSafe.service;

import bid.safe.lumio.BidSafe.dto.BidRequest;
import bid.safe.lumio.BidSafe.dto.BidUpdate;
import bid.safe.lumio.BidSafe.exception.ResourceNotFoundException;
import bid.safe.lumio.BidSafe.model.Auction;
import bid.safe.lumio.BidSafe.model.Bid;
import bid.safe.lumio.BidSafe.model.IdempotencyRecord;
import bid.safe.lumio.BidSafe.model.User;
import bid.safe.lumio.BidSafe.repository.AuctionRepository;
import bid.safe.lumio.BidSafe.repository.BidRepository;
import bid.safe.lumio.BidSafe.repository.IdempotencyRecordRepository;
import bid.safe.lumio.BidSafe.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final IdempotencyRecordRepository idempotencyRecordRepository;

    public BidService(
            BidRepository bidRepository,
            AuctionRepository auctionRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate, IdempotencyRecordRepository idempotencyRecordRepository) {

        this.bidRepository = bidRepository;
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
    }

    @Transactional
    public Bid placeBid(
            Long auctionId,
            BidRequest request,
            String email,
            String idempotencyKey) {

        Optional<IdempotencyRecord> existing =
                idempotencyRecordRepository
                        .findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {

            Long bidId = existing.get().getBidId();

            return bidRepository.findById(bidId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Original bid not found"
                            ));
        }

        // 1. Find auction
        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found"));

        System.out.println(
                "THREAD " + Thread.currentThread().getName()
                        + " ACQUIRED LOCK"
        );

        // 2. Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // 3. Read current highest bid
        double currentHighest = auction.getCurrentHighestBid();

//        // 4. Compare new bid with current highest
//        if (request.getAmount() <= currentHighest) {
//            throw new RuntimeException(
//                    "Bid must be higher than current highest bid");
//        }

//        double currentHighest = auction.getCurrentHighestBid();

        System.out.println(
                "THREAD " + Thread.currentThread().getName()
                        + " READ highest = " + currentHighest
                        + " | trying bid = " + request.getAmount()
        );

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (request.getAmount() <= currentHighest) {

            System.out.println(
                    "THREAD " + Thread.currentThread().getName()
                            + " REJECTED bid = " + request.getAmount()
                            + " | read highest = " + currentHighest
            );

            throw new RuntimeException(
                    "Bid must be higher than current highest bid");
        }

        System.out.println(
                "THREAD " + Thread.currentThread().getName()
                        + " PASSED check"
                        + " | bid = " + request.getAmount()
                        + " | read highest = " + currentHighest
        );

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

        IdempotencyRecord record =
                new IdempotencyRecord();

        record.setIdempotencyKey(idempotencyKey);
        record.setBidId(savedBid.getId());

        idempotencyRecordRepository.save(record);

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