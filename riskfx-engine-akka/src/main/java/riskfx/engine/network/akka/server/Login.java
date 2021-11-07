package riskfx.engine.network.akka.server;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import riskfx.engine.network.akka.ConnectionProtocol.Reply;
import riskfx.engine.network.akka.ConnectionProtocol.Request;

public class Login {

	public interface Command {}
	
	public static enum Cancel implements Command { INSTANCE; }

	public static Behavior<Command> create(ActorRef<Request> self, String displayName, ActorRef<Reply> replyTo) {
		// TODO Auto-generated method stub
		return null;
	}
}
