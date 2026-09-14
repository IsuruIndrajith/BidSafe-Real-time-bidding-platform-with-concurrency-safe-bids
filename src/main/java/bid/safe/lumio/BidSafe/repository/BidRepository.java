package bid.safe.lumio.BidSafe.repository;

import bid.safe.lumio.BidSafe.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long> {
}