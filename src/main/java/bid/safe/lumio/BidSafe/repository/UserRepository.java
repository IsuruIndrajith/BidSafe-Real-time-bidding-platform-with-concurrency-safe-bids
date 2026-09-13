package bid.safe.lumio.BidSafe.repository;

import bid.safe.lumio.BidSafe.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}