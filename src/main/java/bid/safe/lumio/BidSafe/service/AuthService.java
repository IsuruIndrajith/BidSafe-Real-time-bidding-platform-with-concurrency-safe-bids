package bid.safe.lumio.BidSafe.service;

import bid.safe.lumio.BidSafe.dto.LoginRequest;
import bid.safe.lumio.BidSafe.dto.RegisterRequest;
import bid.safe.lumio.BidSafe.model.User;
import bid.safe.lumio.BidSafe.repository.UserRepository;
import bid.safe.lumio.BidSafe.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private static final Logger log =
            LoggerFactory.getLogger(AuthService.class);

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public User register(RegisterRequest request) {

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setRole("USER");

        User savedUser = userRepository.save(user);

        log.info(
                "USER_REGISTERED userId={} email={}",
                savedUser.getId(),
                savedUser.getEmail()
        );

        return userRepository.save(user);
    }

    public String login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        log.info(
                "USER_LOGIN email={}",
                request.getEmail()
        );

        return jwtService.generateToken(request.getEmail());
    }
}