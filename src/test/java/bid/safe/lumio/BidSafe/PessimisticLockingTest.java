package bid.safe.lumio.BidSafe;

import bid.safe.lumio.BidSafe.dto.BidRequest;
import bid.safe.lumio.BidSafe.model.Auction;
import bid.safe.lumio.BidSafe.model.Item;
import bid.safe.lumio.BidSafe.model.User;
import bid.safe.lumio.BidSafe.repository.AuctionRepository;
import bid.safe.lumio.BidSafe.repository.ItemRepository;
import bid.safe.lumio.BidSafe.repository.UserRepository;
import bid.safe.lumio.BidSafe.service.BidService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PessimisticLockingTest {

    @Autowired
    private BidService bidService;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("secret");
        user.setRole("USER");
        return user;
    }

    @Test
    void testPessimisticLocking() throws Exception {
        System.out.println("\n=================================");
        System.out.println("PESSIMISTIC LOCKING TEST");
        System.out.println("=================================\n");

        final User userA = userRepository.save(createUser("alice2", "alice@example2.com"));
        final User userB = userRepository.save(createUser("bob2", "bob@example2.com"));

        Item item = new Item();
        item.setName("Pessimistic Lock Demo Item");
        item.setDescription("Concurrent bid test");
        item.setStartingPrice(100);
        item = itemRepository.save(item);

        Auction auction = new Auction();
        auction.setItem(item);
        auction.setCurrentHighestBid(100);
        auction.setStartTime(LocalDateTime.now());
        auction.setEndTime(LocalDateTime.now().plusHours(1));
        auction.setStatus("ACTIVE");
        Auction savedAuction = auctionRepository.saveAndFlush(auction);
        Long auctionId = savedAuction.getId();

        System.out.println("Initial highest bid = " + auctionRepository.findById(auctionId).orElseThrow().getCurrentHighestBid());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Callable<String> firstBid = () -> {
            readyLatch.countDown();
            startLatch.await();
            try {
                BidRequest request = new BidRequest();
                request.setAmount(250);
                System.out.println("THREAD A: sending bid 250");
                bidService.placeBid(auctionId, request, userA.getEmail(), "bid-a-" + System.nanoTime());
                System.out.println("THREAD A: bid 250 accepted");
                return "A ACCEPTED";
            } catch (Exception e) {
                System.out.println("THREAD A: " + e.getMessage());
                return "A REJECTED";
            }
        };

        Callable<String> secondBid = () -> {
            readyLatch.countDown();
            startLatch.await();
            try {
                BidRequest request = new BidRequest();
                request.setAmount(220);
                System.out.println("THREAD B: sending bid 220");
                bidService.placeBid(auctionId, request, userB.getEmail(), "bid-b-" + System.nanoTime());
                System.out.println("THREAD B: bid 220 accepted");
                return "B ACCEPTED";
            } catch (Exception e) {
                System.out.println("THREAD B: " + e.getMessage());
                return "B REJECTED";
            }
        };

        Future<String> resultA = executor.submit(firstBid);
        Future<String> resultB = executor.submit(secondBid);

        readyLatch.await();
        startLatch.countDown();

        String responseA = resultA.get();
        String responseB = resultB.get();
        executor.shutdown();

        Auction after = auctionRepository.findById(auctionId).orElseThrow();

        System.out.println("\n=================================");
        System.out.println("RESULT");
        System.out.println("=================================");
        System.out.println("Thread A: " + responseA);
        System.out.println("Thread B: " + responseB);
        System.out.println("Final highest bid: " + after.getCurrentHighestBid());
        System.out.println("Final version: " + after.getVersion());

        int acceptedCount = 0;
        if ("A ACCEPTED".equals(responseA)) acceptedCount++;
        if ("B ACCEPTED".equals(responseB)) acceptedCount++;

        assertEquals(250.0, after.getCurrentHighestBid(), "The higher bid should win after lock serialization");
        assertEquals(1, acceptedCount, "Exactly one concurrent bid should be accepted while the other is rejected");
    }
}