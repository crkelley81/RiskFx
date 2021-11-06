/**
 * 
 */
package riskfx.engine.network.akka.client;

import java.time.Duration;
import java.util.Objects;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import riskfx.engine.network.ConnectionFactory;
import riskfx.engine.network.akka.ConnectionProtocol.Join;
import riskfx.engine.network.akka.ConnectionProtocol.Reply;

/**
 * @author christopher
 *
 */
public class AkkaConnectionFactory implements ConnectionFactory {

	private static record Connect(String actorPath, String displayName, Notifier notifier) {}
	
	
	private final Duration timeout;
	private final ActorSystem<Connect> actorSystem;
	
	public AkkaConnectionFactory(Duration timeout) {
		this.timeout = Objects.requireNonNull(timeout);
		this.actorSystem = ActorSystem.create(main(), "AkkaConnectionFactory");
	}

	@Override
	public final void connect(final String host, final int port, final String displayName, final Notifier notifier) {
		actorSystem.tell(new Connect(actorPath(host, port), displayName, notifier));
	}

	private String actorPath(String host, int port) {
		return "akka://RiskFx@%s:%s/user/RiskFxLogin".formatted(host, port);
	}

	private Behavior<Connect> main() {
		return Behaviors.setup(context -> {
			return Behaviors.receive(Connect.class)
					.onMessage(Connect.class, this::onConnect)
					.build();
		});
	}
	
	private Behavior<Connect> onConnect(final ActorContext<Connect> ctx, final Connect connect) {
		ctx.classicActorContext()
			.actorSelection(connect.actorPath)
			.resolveOne(timeout)
			.exceptionally(ex -> {
				connect.notifier.notifyFailed("Could not find server");
				return null;
			})
			.thenAccept(remoteRef -> {
				final ActorRef<Reply> ref = ctx.spawn(ConnectionActor.create(connect.displayName, connect.notifier), "");
				final Join join = new Join(ref, connect.displayName);
				remoteRef.tell(join, ctx.classicActorContext().system().deadLetters());
			});
		
		return Behaviors.same();
	}
}
