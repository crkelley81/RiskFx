package riskfx.engine.network.akka;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import riskfx.engine.network.Connection;
import riskfx.engine.network.ConnectionFactory.Auth;
import riskfx.engine.network.ConnectionFactory.Notifier;
import riskfx.engine.network.akka.ConnectionProtocol.AuthCancel;
import riskfx.engine.network.akka.ConnectionProtocol.AuthReply;
import riskfx.engine.network.akka.ConnectionProtocol.Authenticate;
import riskfx.engine.network.akka.ConnectionProtocol.JoinFailed;
import riskfx.engine.network.akka.ConnectionProtocol.JoinSuccess;
import riskfx.engine.network.akka.ConnectionProtocol.Reply;
import riskfx.engine.network.akka.client.ConnectionActor;

class ConnectionActorTest {

	private static final String DISPLAY_NAME = "crk";
	private ActorTestKit testKit;

	@BeforeEach
	void setUp() throws Exception {
		testKit = ActorTestKit.create();
	}

	@AfterEach
	void tearDown() throws Exception {
		testKit.shutdownTestKit();
	}

	@Test
	void notifySuccessWhenJoinedSuccess() throws InterruptedException {
		// GIVEN
		final Notifier notifier = Mockito.mock(Notifier.class);
		final ActorRef<Reply> ref = testKit.spawn(ConnectionActor.create(DISPLAY_NAME, notifier));
		final ArgumentCaptor<Connection> captor = ArgumentCaptor.forClass(Connection.class);

		// WHEN
		ref.tell(new JoinSuccess());
		Thread.sleep(100);

		// THEN
		Mockito.verify(notifier).notifySuccess(captor.capture());

		// TODO

	}

	@Test
	void notifyFailedWhenUserCancelsAuthentication() throws InterruptedException {
		// GIVEN
		final Notifier notifier = Mockito.mock(Notifier.class);
		final ArgumentCaptor<Auth> authCaptor = ArgumentCaptor.forClass(Auth.class);
		final ActorRef<Reply> ref = testKit.spawn(ConnectionActor.create(DISPLAY_NAME, notifier));
		final TestProbe<AuthReply> probe = testKit.createTestProbe(AuthReply.class);
		
		// WHEN
		ref.tell(new Authenticate(probe.ref()));
		Thread.sleep(100);

		// THEN
		Mockito.verify(notifier).authenticate(authCaptor.capture());
		
		// WHEN 
		authCaptor.getValue().cancel();
		
		// THEN 
		probe.expectMessageClass(AuthCancel.class);
		Mockito.verify(notifier).notifyFailed("Cancelled");
	}

	@Test
	void notfifyFailedWhenJoinFailed() throws InterruptedException {
		// GIVEN
		final Notifier notifier = Mockito.mock(Notifier.class);
		final ActorRef<Reply> ref = testKit.spawn(ConnectionActor.create(DISPLAY_NAME, notifier));

		// WHEN
		ref.tell(new JoinFailed(""));
		Thread.sleep(100);

		// THEN
		Mockito.verify(notifier).notifyFailed("");
	}

}
