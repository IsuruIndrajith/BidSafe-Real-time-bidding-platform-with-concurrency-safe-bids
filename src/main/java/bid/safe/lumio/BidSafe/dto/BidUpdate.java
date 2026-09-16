package bid.safe.lumio.BidSafe.dto;

public class BidUpdate {

    private Long auctionId;
    private String username;
    private double amount;

    public BidUpdate() {
    }

    public BidUpdate(Long auctionId, String username, double amount) {
        this.auctionId = auctionId;
        this.username = username;
        this.amount = amount;
    }

    public Long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}