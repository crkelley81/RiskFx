package riskfx.engine.display;

import java.util.concurrent.Executor;

import riskfx.engine.model.Player;

public interface Display {
	default public void onStart() {}
	default public void onBeginTurn(final Player player, final int playerIdx, final long turnNumber) {}
	default public void onEndTurn(Player player, final int playerIdx, long turnNumber) {}
	
	public static Display inExecutor(final Display display, final Executor e) {
		return new Display() {

			@Override
			public void onStart() {
				e.execute(() -> {
					display.onStart();
				});
			}

			@Override
			public void onBeginTurn(Player player, int playerIdx, long turnNumber) {
				e.execute(() -> {
					display.onBeginTurn(player, playerIdx, turnNumber);
				});
			}

			@Override
			public void onEndTurn(Player player, int playerIdx, long turnNumber) {
				e.execute(() -> {
					display.onEndTurn(player, playerIdx, turnNumber);
				});
			}
			
		};
	}
}