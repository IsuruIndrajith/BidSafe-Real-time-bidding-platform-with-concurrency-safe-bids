package bid.safe.lumio.BidSafe.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Auction {

        @Id
        @GeneratedValue
        private Long id;

        @ManyToOne
        private Item item;

        @Column
        private LocalDateTime startTime;

        @Column
        private LocalDateTime endTime;

        @Column
        private String status;

        @Column
        private double currentHighestBid;

        @OneToMany
        private List<Bid> bids;

        public Auction() {
        }


    // getters and setters
    }

