package riskfx.engine.model;

public interface Player {
	
	public static Player none() {
		return PlayerNone.NONE;
	}

	public static Player of(final String id, final String displayName) {
		return new PlayerImpl(id, displayName);
	}
	
	public String getId();
	public String getDisplayName();
	public boolean isNone();
	
	
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
			}};
	}
	
	static class PlayerImpl implements Player {

		private final String id;
		private final String displayName;

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
		
	}

}
