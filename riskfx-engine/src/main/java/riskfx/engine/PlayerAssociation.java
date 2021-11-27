package riskfx.engine;

import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import riskfx.engine.model.Player;

public class PlayerAssociation {
	public static PlayerAssociation of(Player of) {
		return new PlayerAssociation(of);
	}
	
	private final Player player;
	private ObjectProperty<PlayerType> typeProperty = new SimpleObjectProperty<>(this, "type", StandardPlayerTypes.HUMAN);
	
	
	public PlayerAssociation(Player of) {
		this.player = Objects.requireNonNull(of);
	}	

	public String getDisplayName() {
		return player.getDisplayName();
	}

	public final ObjectProperty<PlayerType> typeProperty() {
		return this.typeProperty;
	}

	public final Player getPlayer() {
		return this.player;
	}

	public final void disable() {
		this.typeProperty.setValue(StandardPlayerTypes.NONE);
	}

}
