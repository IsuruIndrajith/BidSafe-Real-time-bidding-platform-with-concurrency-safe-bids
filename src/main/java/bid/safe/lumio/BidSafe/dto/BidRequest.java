package bid.safe.lumio.BidSafe.dto;

import jakarta.validation.constraints.Positive;

public class BidRequest {

    @Positive
    private double amount;

    public BidRequest() {
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}