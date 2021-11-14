package riskfx.engine.network.akka.server;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import riskfx.engine.network.akka.ConnectionProtocol.Join;
import riskfx.engine.network.akka.ConnectionProtocol.JoinFailed;
import riskfx.engine.network.akka.ConnectionProtocol.Request;


public class LoginActor {

	public static Behavior<Request> create() {
		return null;
	}

	private final Duration timeout;
	private final Set<String> displayNames = new HashSet<>();
	private final Map<String, ActorRef<?>> sessions = new HashMap<>();

	private LoginActor(final Duration timeout, final LoginService service) {
		this.timeout = Objects.requireNonNull(timeout);
	}

	private Behavior<Request> behavior() {
		return Behaviors.receive(Request.class).onMessage(Join.class, this::onJoin).build();
	}

	private Behavior<Request> onJoin(final ActorContext<Request> ctx, final Join join) {
		if (displayNames.contains(join.displayName())) {
			join.replyTo().tell(new JoinFailed(""));
		} else {
			displayNames.add(join.displayName());

			final ActorRef<Login.Command> ref = ctx
					.spawn(Login.create(ctx.getSelf(), join.displayName(), join.replyTo()), "");

			ctx.watchWith(ref, join);
			ctx.scheduleOnce(timeout, ref, Login.Cancel.INSTANCE);
		}
	
		return Behaviors.same();
	}
}
