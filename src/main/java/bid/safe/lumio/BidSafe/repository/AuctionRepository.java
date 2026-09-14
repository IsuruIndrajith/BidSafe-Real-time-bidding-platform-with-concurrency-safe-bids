package bid.safe.lumio.BidSafe.repository;

import bid.safe.lumio.BidSafe.model.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
}
