package bid.safe.lumio.BidSafe;

import bid.safe.lumio.BidSafe.dto.BidRequest;
import bid.safe.lumio.BidSafe.model.Auction;
import bid.safe.lumio.BidSafe.repository.AuctionRepository;
import bid.safe.lumio.BidSafe.service.BidService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class BidConcurrencyTest {

    @Autowired
    private BidService bidService;

        @Autowired
        private AuctionRepository auctionRepository;

    @Test
    void testConcurrentBids() throws Exception {

        int numberOfThreads = 20;
                long auctionId = 1L;
                double initialHighestBid = 429030D;
                double expectedHighestBid = initialHighestBid + numberOfThreads;

        ExecutorService executor =
                Executors.newFixedThreadPool(numberOfThreads);

        CountDownLatch ready =
                new CountDownLatch(numberOfThreads);

        CountDownLatch start =
                new CountDownLatch(1);

        CountDownLatch done =
                new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {

            final int threadNumber = i;

            executor.submit(() -> {

                try {

                    ready.countDown();

                    start.await();

                    System.out.println(
                            "Thread " + threadNumber + " starting"
                    );

                    BidRequest request = new BidRequest();

                    request.setAmount(
                            initialHighestBid + threadNumber + 1
                    );

                    bidService.placeBid(
                            auctionId,
                            request,
                            "isuru@gmail.com"
                    );

                } catch (Exception e) {

                    System.out.println(
                            "Thread " + threadNumber +
                                    " rejected: " +
                                    e.getMessage()
                    );

                } finally {

                    done.countDown();
                }
            });
        }

        ready.await();

        System.out.println("All threads ready");

        start.countDown();

        done.await();

        executor.shutdown();

        System.out.println("All threads finished");

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow();

        assertEquals(expectedHighestBid, auction.getCurrentHighestBid());
    }
}