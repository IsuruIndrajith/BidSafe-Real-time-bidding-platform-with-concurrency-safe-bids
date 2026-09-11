package bid.safe.lumio.BidSafe.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Bid {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Auction auction;

    @ManyToOne
    private User user;

    @Column
    private double amount;

    @Column
    private LocalDateTime createdAt;

    public Bid() {
    }

    // getters and setters
}