package riskfx.engine.network;

import java.util.concurrent.Executor;

public interface ConnectionFactory {
	
	/**
	 * 
	 * @author christopher
	 *
	 */
	public interface Auth {
		public void password(final String password);
		public void cancel();
	}
	
	/**
	 * 
	 */
	public interface Notifier {
		
		void authenticate(final Auth auth);
		void notifyFailed(final String reason);

		void notifySuccess(Connection capture);
		
	}
	
	public static Notifier withExecutor(final Notifier source, final Executor exec) {
		return new Notifier() {

			@Override
			public void notifyFailed(String reason) {
				exec.execute(() -> {
					source.notifyFailed(reason);
				});
				
			}

			@Override
			public void notifySuccess(Connection capture) {
				exec.execute(() -> {
					source.notifySuccess(capture);
				});
			}

			@Override
			public void authenticate(Auth auth) {
				exec.execute(() -> {
					source.authenticate(auth);
				});
			}};
	}
	
	
	public void connect(final String host, final int port, final String displayName, final Notifier notifier);
}
