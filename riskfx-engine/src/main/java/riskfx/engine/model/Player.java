package riskfx.engine.model;

import java.io.Serializable;
import java.util.Optional;

public interface Player extends Serializable {
	
	public static Player none() {
		return PlayerNone.NONE;
	}

	public static Player of(final String id, final String displayName) {
		return new PlayerImpl(id, displayName);
	}
	
	public String getId();
	public String getDisplayName();
	public boolean isNone();
	
	public Optional<Territory> getCapital();
	default public boolean hasCapital() {
		return getCapital().isPresent();
	}
	
	static enum PlayerNone implements Player {
		NONE() {

			@Override
			public final String getId() {
				return "none";
			}

			@Override
			public final String getDisplayName() {
				return "None";
			}

			@Override
			public final boolean isNone() {
				return true;
			}

			@Override
			public final Optional<Territory> getCapital() {
				return Optional.empty();
			}

			@Override
			public void setTroopsToDeploy(long troopsToDeploy) {
				// Do nothing
			}

			@Override
			public long getTroopsToDeploy() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public void receiveCard(Card card) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Hand getHand() {
				// TODO Auto-generated method stub
				return null;
			}};
	}
	
	static class PlayerImpl implements Player {

		private final String id;
		private final String displayName;
		private final Hand hand = new Hand();
		
		private Territory capital;
		private long troopsToDeploy;
		
		public PlayerImpl(String id, String displayName) {
			super();
			this.id = id;
			this.displayName = displayName;
		}

		@Override
		public final String getId() {
			return this.id;
		}

		@Override
		public final String getDisplayName() {
			return this.displayName;
		}

		@Override
		public final boolean isNone() {
			return false;
		}

		/* package */ void setCapital(Territory territory) {
			this.capital = territory;
		}

		@Override
		public final Optional<Territory> getCapital() {
			return Optional.ofNullable(capital);
		}

		@Override
		public void setTroopsToDeploy(long troopsToDeploy) {
			this.troopsToDeploy = troopsToDeploy;
		}

		@Override
		public long getTroopsToDeploy() {
			return this.troopsToDeploy;
		}

		@Override
		public void receiveCard(Card card) {
			hand.issue(card);
		}

		@Override
		public Hand getHand() {
			return hand;
		}
		
		@Override public final String toString() {
			return "Player[id=%s]".formatted(id);
		}
	}

	public void setTroopsToDeploy(long troopsToDeploy);

	public long getTroopsToDeploy();

	public void receiveCard(Card card);

	public Hand getHand();

}
