package bid.safe.lumio.BidSafe;

import bid.safe.lumio.BidSafe.dto.BidRequest;
import bid.safe.lumio.BidSafe.service.BidService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
class BidConcurrencyTestwithCounters {

    @Autowired
    private BidService bidService;

    @Test
    void testConcurrentBids() throws Exception {

        int numberOfThreads = 3;

        AtomicInteger accepted =
                new AtomicInteger();

        AtomicInteger rejected =
                new AtomicInteger();

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

                    BidRequest request = new BidRequest();

                    request.setAmount(
                            429060 + threadNumber
                    );

                    bidService.placeBid(
                            1L,
                            request,
                            "isuru@gmail.com"
                    );

                    accepted.incrementAndGet();

                    System.out.println(
                            "ACCEPTED: " +
                                    request.getAmount()
                    );

                } catch (Exception e) {

                    rejected.incrementAndGet();

                    System.out.println(
                            "REJECTED: " +
                                    e.getMessage()
                    );

                } finally {

                    done.countDown();
                }
            });
        }

        ready.await();

        System.out.println(
                "Sending " +
                        numberOfThreads +
                        " concurrent bids..."
        );

        start.countDown();

        done.await();

        executor.shutdown();

        System.out.println(
                "Total sent: " +
                        numberOfThreads
        );

        System.out.println(
                "Accepted: " +
                        accepted.get()
        );

        System.out.println(
                "Rejected: " +
                        rejected.get()
        );
    }
}