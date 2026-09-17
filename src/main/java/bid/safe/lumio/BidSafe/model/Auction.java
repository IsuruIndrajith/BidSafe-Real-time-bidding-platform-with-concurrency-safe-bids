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

        @Version
        private Long version;

        public Auction() {
        }
    // getters and setters
    public Long getId() {
            return id;
    }

        public void setId(Long id) {
                this.id = id;
        }

        public Item getItem() {
                return item;
        }

        public void setItem(Item item) {
                this.item = item;
        }

        public LocalDateTime getStartTime() {
                return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
                this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
                return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
                this.endTime = endTime;
        }

        public String getStatus() {
                return status;
        }

        public void setStatus(String status) {
                this.status = status;
        }

        public double getCurrentHighestBid() {
                return currentHighestBid;
        }

        public void setCurrentHighestBid(double currentHighestBid) {
                this.currentHighestBid = currentHighestBid;
        }

        public List<Bid> getBids() {
                return bids;
        }

        public void setBids(List<Bid> bids) {
                this.bids = bids;
        }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
    }

