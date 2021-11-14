package riskfx.ui;

import java.util.Objects;

import appfx.role.As;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.EventType;
import javafx.scene.Node;
import riskfx.app.util.role.Identifiable;
import riskfx.app.util.role.Selectable;

public interface TerritoryView<T extends Identifiable> extends As, Selectable {

	public static class Event extends javafx.event.Event {
		public static final EventType<Event> ANY = new EventType<>("TERRITORY");
		public static final EventType<Event> SELECTION = new EventType<>(ANY, "SELECTION");
		public static final EventType<Event> SELECTED = new EventType<>(SELECTION, "SELECTED");
		public static final EventType<Event> DESELECTED = new EventType<>(SELECTION, "DESELECTED");
		
		private final TerritoryView<?> territory;

		public Event(final EventType<Event> type, final TerritoryView<?> view) {
			super(type);
			this.territory = Objects.requireNonNull(view);
		}
		
		public final TerritoryView<?> territory() {
			return this.territory;
		}
	}
	
	public ReadOnlyBooleanProperty hoveredProperty();
	default public boolean isHovered()		{	return this.hoveredProperty().get(); }
	
	public Node asNode();
}
