package riskfx.engine.network.akka;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import riskfx.engine.network.ConnectionFactory.Notifier;
import riskfx.engine.network.akka.client.AkkaConnectionFactory;

class AkkaConnectionFactoryTest {

	private static final Duration TIMEOUT = Duration.ofSeconds(1);
	private static final String HOST = "127.0.0.1";
	private static final int PORT = 1099;
	private static final String DISPLAY_NAME = "crk";
	private AkkaConnectionFactory factory;

	@BeforeEach
	void setUp() throws Exception {
		factory = new AkkaConnectionFactory(TIMEOUT);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void failIfServeNotDisoveredWithinTimeout() throws InterruptedException {
		// GIVEN 
		final Notifier notifier = Mockito.mock(Notifier.class);
		
		// WHEN 
		factory.connect(HOST, PORT, DISPLAY_NAME, notifier);
		Thread.sleep(TIMEOUT.toMillis());
		
		// THEN 
		Mockito.verify(notifier).notifyFailed("Could not find server");
		
	}

}
