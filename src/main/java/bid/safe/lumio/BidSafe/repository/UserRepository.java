package bid.safe.lumio.BidSafe.repository;

import bid.safe.lumio.BidSafe.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}