package riskfx.engine;

import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import riskfx.engine.model.Player;

public class PlayerAssociation {

	public static interface Type {
		public String getDisplayName();
		
		public boolean isHuman();
		public boolean isComputer();
		public boolean isNone();
	}
	
	public static enum StandardTypes implements Type {
		NONE() {
			@Override
			public String getDisplayName() {
				return "None";
			}

			@Override
			public boolean isHuman() {
				return false;
			}

			@Override
			public boolean isComputer() {
				return false;
			}

			@Override
			public boolean isNone() {
				return true;
			}
		}, HUMAN() {

			@Override
			public String getDisplayName() {
				return "Human";
			}

			@Override
			public boolean isHuman() {
				return true;
			}

			@Override
			public boolean isComputer() {
				return false;
			}

			@Override
			public boolean isNone() {
				return false;
			}}; 


	}
	
	private final Player player;
	private ObjectProperty<Type> typeProperty = new SimpleObjectProperty<>(this, "type", StandardTypes.HUMAN);
	
	
	public PlayerAssociation(Player of) {
		this.player = Objects.requireNonNull(of);
	}

	public static PlayerAssociation of(Player of) {
		return new PlayerAssociation(of);
	}

	public String getDisplayName() {
		return player.getDisplayName();
	}

	public final ObjectProperty<PlayerAssociation.Type> typeProperty() {
		return this.typeProperty;
	}



}
