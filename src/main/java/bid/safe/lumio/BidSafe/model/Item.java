package bid.safe.lumio.BidSafe.model;

import jakarta.persistence.*;

@Entity
public class Item {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private double startingPrice;

    public Item() {
    }

    // getters and setters
}