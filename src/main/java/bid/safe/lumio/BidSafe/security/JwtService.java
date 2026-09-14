package bid.safe.lumio.BidSafe.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private final String secretKey =
            "your-very-long-secret-key-change-this";

    public String generateToken(String email) {

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis()
                                + 1000 * 60 * 60)
                )
                .signWith(Keys.hmacShaKeyFor(
                        secretKey.getBytes()
                ))
                .compact();
    }
}
