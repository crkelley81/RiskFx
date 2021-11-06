package riskfx.engine.network.akka;

import akka.actor.typed.ActorRef;

public interface ConnectionProtocol {

	public interface Request {}
	public static record Join(ActorRef<Reply> replyTo, String displayName) implements Request {}
	
	public interface Reply {}
	public static record Authenticate(ActorRef<AuthReply> replyTo) implements Reply {}
	public static record JoinFailed(String reason) implements Reply {}
	public static record JoinSuccess() implements Reply {}
	
	public interface AuthReply {}
	public static record AuthPassword(String password) implements AuthReply {}
	public static enum AuthCancel implements AuthReply { INSTANCE; }
}
