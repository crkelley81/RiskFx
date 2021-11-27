package riskfx.engine.model;

import java.io.Serializable;
import java.util.Objects;

import riskfx.util.role.Displayable;
import riskfx.util.role.Identifiable;

public final class Card implements Identifiable, Displayable, Serializable {
	public static enum Type {
		INFANTRY("infantry", "Infantry", false), 
		ARTILLERY("artillery", "Artillery", false), 
		CAVALRY("cavalry", "Cavalry", false), 
		WILDCARD("wildcard", "Wildcard", true);

		private String id;
		private String name;
		private boolean wildcard;

		Type(String id, String name, boolean wildcard) {
			this.id = Objects.requireNonNull(id);
			this.name = Objects.requireNonNull(name);
			this.wildcard = wildcard;
		}

		boolean isWildcard() {
			return this.wildcard;
		}
	}
	
	public static Card territoryCard(final Territory territory, final Type type) {
		Objects.requireNonNull(territory);
		Objects.requireNonNull(type);
		return new Card(type, territory);
	}
	
	public static Card wildcard(long i) {
		return new Card(Type.WILDCARD, i);
	}
	
	
	
	private final String id;
	private final String displayName;
	private final Type type;
	private final Territory territory;
	
	private Card(final Type type, final long index) {
		if (! Objects.equals(Type.WILDCARD, type)) throw new IllegalArgumentException();
		
		this.id = "%s%s".formatted(Type.WILDCARD.toString().toLowerCase(), index);
		this.displayName = "Wildcard";
		this.type = type;
		this.territory = null;
	}
	
	private Card(final Type type, final Territory territory) {
		this.id = "%s%s".formatted(type.toString().toLowerCase(), territory.getId());
		this.displayName = (Type.WILDCARD.equals(type)) ? "Wildcard" : type + " " + territory.getDisplayName();
		this.type = Objects.requireNonNull(type);
		this.territory = territory;
	}
	
	@Override public final String getId() {
		return id;
	}

	@Override
	public final String getDisplayName() {
		return displayName;
	}

	public final Type type() {
		return this.type;
	}
	
	public final boolean isWildcard() {
		return this.type.isWildcard();
	}
	
	public final boolean isNotWildcard() {
		return !isWildcard();
	}
	
	public final boolean isSameOrWilcard(final Card other) {
		return isWildcard() || other.isWildcard() || Objects.equals(this.type, other.type);
	}
}
