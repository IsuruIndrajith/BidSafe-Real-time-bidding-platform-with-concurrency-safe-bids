package bid.safe.lumio.BidSafe.model;

import jakarta.persistence.*;

@Entity
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String username;

    @Column
    private String email;

    @Column
    private String password;

    @Column
    private String role;

    public User() {
    }

    // getters and setters
}