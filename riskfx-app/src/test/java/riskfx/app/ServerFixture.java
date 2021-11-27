package riskfx.app;

import java.time.Duration;

import akka.actor.ActorSystem;
import riskfx.engine.network.akka.server.ClassicLoginActor;

public class ServerFixture {

	public ActorSystem startup(String host, int port, String password) {
		final ActorSystem serverSystem = ActorSystem.create("RiskFx");
		serverSystem.actorOf(ClassicLoginActor.props(host,  port, password, Duration.ofSeconds(1)), "RiskFxLogin");
		return serverSystem;
	}

}
