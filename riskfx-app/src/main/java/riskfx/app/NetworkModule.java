package riskfx.app;

import java.time.Duration;

import dagger.Provides;
import javafx.application.Platform;
import riskfx.engine.network.ConnectionFactory;
import riskfx.engine.network.akka.client.AkkaConnectionFactory;

@dagger.Module
public class NetworkModule {

	@Provides public ConnectionFactory provideConnectionFactory() {
		return new AkkaConnectionFactory(Duration.ofSeconds(1), Platform::runLater);
	}
}
