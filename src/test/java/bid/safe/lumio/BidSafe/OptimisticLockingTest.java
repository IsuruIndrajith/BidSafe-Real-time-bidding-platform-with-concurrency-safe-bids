package bid.safe.lumio.BidSafe;

import bid.safe.lumio.BidSafe.model.Auction;
import bid.safe.lumio.BidSafe.model.Item;
import bid.safe.lumio.BidSafe.repository.AuctionRepository;
import bid.safe.lumio.BidSafe.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OptimisticLockingTest {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void testOptimisticLocking() {
        Item item = new Item();
        item.setName("Optimistic Lock Test Item");
        item.setDescription("Testing optimistic locking");
        item.setStartingPrice(100);

        Item savedItem = itemRepository.save(item);

        Auction auction = new Auction();
        auction.setItem(savedItem);
        auction.setCurrentHighestBid(100);
        auction.setStartTime(LocalDateTime.now());
        auction.setEndTime(LocalDateTime.now().plusHours(1));
        auction.setStatus("ACTIVE");

        Auction savedAuction = auctionRepository.saveAndFlush(auction);

        Auction auctionA = auctionRepository.findById(savedAuction.getId()).orElseThrow();
        Auction auctionB = auctionRepository.findById(savedAuction.getId()).orElseThrow();

        System.out.println("Before update: Auction A version = " + auctionA.getVersion() + ", highestBid = " + auctionA.getCurrentHighestBid());
        System.out.println("Before update: Auction B version = " + auctionB.getVersion() + ", highestBid = " + auctionB.getCurrentHighestBid());

        auctionA.setCurrentHighestBid(150);
        auctionRepository.saveAndFlush(auctionA);
        System.out.println("After A update: Auction A version = " + auctionA.getVersion() + ", highestBid = " + auctionA.getCurrentHighestBid());

        auctionB.setCurrentHighestBid(200);

        ObjectOptimisticLockingFailureException exception = assertThrows(
                ObjectOptimisticLockingFailureException.class,
                () -> auctionRepository.saveAndFlush(auctionB)
        );

        System.out.println("Optimistic locking exception received: " + exception.getMessage());
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Row was already updated or deleted"));
    }
}