package riskfx.engine.network.akka.client;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import riskfx.engine.network.Connection;
import riskfx.engine.network.ConnectionFactory.Notifier;
import riskfx.engine.network.akka.ConnectionProtocol;
import riskfx.engine.network.akka.ConnectionProtocol.AuthCancel;
import riskfx.engine.network.akka.ConnectionProtocol.AuthPassword;
import riskfx.engine.network.akka.ConnectionProtocol.Authenticate;
import riskfx.engine.network.akka.ConnectionProtocol.JoinFailed;
import riskfx.engine.network.akka.ConnectionProtocol.JoinSuccess;
import riskfx.engine.network.akka.ConnectionProtocol.Reply;

public class ConnectionActor {

	
	public static Behavior<Reply> create(String displayName, Notifier notifier) {
		return Behaviors.setup(context -> new ConnectionActor(context, displayName).connecting(notifier));
	}

	private static enum Cancel implements Reply { INSTANCE; }
	
	private final CompletableFuture<Void> closeFuture = new CompletableFuture<>();
	
	private final ActorContext<Reply> context;
	private final String displayName;

	
	private ConnectionActor(final ActorContext<Reply> context, final String displayName) {
		this.context = Objects.requireNonNull(context);
		this.displayName = Objects.requireNonNull(displayName);
	}

	private Behavior<Reply> connecting(Notifier notifier) {
		return Behaviors.receive(Reply.class)
				.onMessage(Authenticate.class, (ctx, msg) -> onAuthenticate(ctx, msg, notifier))
				.onMessage(Cancel.class, (ctx, msg) -> onCancel(ctx, msg, notifier))
				.onMessage(JoinSuccess.class, (ctx, msg) -> onJoinSuccess(ctx, msg, notifier))
				.onMessage(JoinFailed.class, (ctx, msg) -> onJoinFailed(ctx, msg, notifier))
				.build();
	}

	private Behavior<Reply> onCancel(ActorContext<Reply> ctx, Cancel msg, Notifier notifier) {
		notifier.notifyFailed("Cancelled");
		return Behaviors.stopped();
	}

	private Behavior<Reply> onAuthenticate(ActorContext<Reply> ctx, Authenticate msg, Notifier notifier) {
		notifier.authenticate(new riskfx.engine.network.ConnectionFactory.Auth() {

			@Override
			public void password(String password) {
				msg.replyTo().tell(new AuthPassword(password));
			}

			@Override
			public void cancel() {
				msg.replyTo().tell(AuthCancel.INSTANCE);
				ctx.getSelf().tell(Cancel.INSTANCE);
			}
			
		});
		return Behaviors.same();
	}

	private Behavior<Reply> onJoinSuccess(final ActorContext<Reply> ctx, final JoinSuccess msg, final Notifier notifier) {
		final Connection connection = new Connection() {};

		notifier.notifySuccess(connection);

		return Behaviors.same();
	}
	
	private Behavior<Reply> onJoinFailed(ActorContext<Reply> ctx, JoinFailed msg, Notifier notifier) {
		notifier.notifyFailed(msg.reason());
		return Behaviors.stopped();
	}

	
}
