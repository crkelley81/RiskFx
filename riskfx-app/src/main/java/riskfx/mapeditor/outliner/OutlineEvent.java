package riskfx.mapeditor.outliner;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.paint.Color;

public class OutlineEvent extends Event {

	private static final EventType<OutlineEvent> STARTING = new EventType<>(ANY, "STARTING");
	private static final EventType<OutlineEvent> FINISHED = new EventType<>(ANY, "FINISHED");

	static final EventType<OutlineEvent> BOUNDARY = new EventType<>(ANY, "BOUNDARY");
	private static final EventType<OutlineEvent> BOUNDARY_START = new EventType<>(BOUNDARY, "BOUNDARY_START");
	private static final EventType<OutlineEvent> BOUNDARY_POINT = new EventType<>(BOUNDARY, "BOUNDARY_POINT");
	private static final EventType<OutlineEvent> BOUNDARY_END = new EventType<>(BOUNDARY, "BOUNDARY_END");

	public static OutlineEvent starting(Color targetColor, int startX, int startY) {
		return new OutlineEvent(STARTING, targetColor, startX, startY);
	}

	public static OutlineEvent finished() {
		return new OutlineEvent(FINISHED);
	}

	public static OutlineEvent startBoundary(int x, int y) {
		return new OutlineEvent(BOUNDARY_START, null, null, x, y);
	}

	public static OutlineEvent boundary(Direction next, int x, int y) {
		return new OutlineEvent(BOUNDARY_POINT, null, next, x, y);
	}

	private Color color;
	private Direction direction;
	public final int x;
	public final int y;

	public OutlineEvent(final EventType<OutlineEvent> type, final Color c, int x, int y) {
		this(type, c, null, x, y);
	}

	public OutlineEvent(EventType<OutlineEvent> type, Color c, Direction direction, int x, int y) {
		super(type);

		this.color = c;
		this.direction = direction;
		this.x = x;
		this.y = y;
	}

	public OutlineEvent(EventType<OutlineEvent> type) {
		this(type, null, null, -1, -1);
	}

	public String toString() {
		return "OutlineEvent[%s, %s, %s, %s, %s]".formatted(getEventType(), color, direction, x, y);
	}
}