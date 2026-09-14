package bid.safe.lumio.BidSafe.dto;
import java.time.LocalDateTime;
public class AuctionRequest {
//    The dto represents the JSON coming from the client.

    private Long itemId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public AuctionRequest() {
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
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
}