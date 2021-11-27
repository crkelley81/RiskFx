package riskfx.engine;

import java.io.Serializable;
import java.util.Objects;

/* package */ class ImmutableGameConfig implements GameConfig, Serializable {
	private final String id;
	private final String displayName;
	private final GameConfig.Type type;
	private final CardStyle cardStyle;
	private final boolean autoAssign;
	private final boolean autoPlace;
	private final boolean useCapitals;
	private final boolean useMissions;
	
	/* package */ ImmutableGameConfig(GameConfig other) {
		this.id = Objects.requireNonNull(other).getId();
		this.displayName = Objects.requireNonNull(other).getName();
		this.type = Objects.requireNonNull(other.getGameType());
		this.cardStyle = Objects.requireNonNull(other.getCardStyle());
		this.autoAssign = other.isAutoAssign();
		this.autoPlace = other.isAutoPlace();
		this.useCapitals = other.useCapitals();
		this.useMissions = other.useMissions();
	}

	@Override
	public final String getId() {
		return this.id;
	}

	@Override
	public final String getName() {
		return this.displayName;
	}

	@Override
	public final Type getGameType() {
		return this.type;
	}

	@Override
	public final CardStyle getCardStyle() {
		return this.cardStyle;
	}

	@Override
	public final boolean isAutoAssign() {
		return this.autoAssign;
	}

	@Override
	public final boolean isAutoPlace() {
		return this.autoPlace;
	}

	@Override
	public final boolean useCapitals() {
		return this.useCapitals;
	}

	@Override
	public final boolean useMissions() {
		return this.useMissions;
	}

	@Override
	public final GameConfig immutableCopy() {
		return this;
	}
}
