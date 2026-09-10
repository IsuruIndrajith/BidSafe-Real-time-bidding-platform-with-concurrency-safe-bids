package bid.safe.lumio.BidSafe;

import org.springframework.boot.SpringApplication;

public class TestBidSafeApplication {

	public static void main(String[] args) {
		SpringApplication.from(BidSafeApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
