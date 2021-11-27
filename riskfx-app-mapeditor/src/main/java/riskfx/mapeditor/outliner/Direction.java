package riskfx.mapeditor.outliner;

import java.awt.Point;
import java.util.Objects;
import java.util.stream.Stream;

public enum Direction {
	NORTH(0, -1), NORTHEAST(1, -1), EAST(1, 0), SOUTHEAST(1, 1), SOUTH(0, 1), SOUTHWEST(-1, 1), WEST(-1, 0),
	NORTHWEST(-1, -1);

	private final int deltaX;
	private final int deltaY;

	private Direction(int deltaX, int deltaY) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}

	void translateFrom(final Point from, final Point next) {
		next.setLocation(from.x + deltaX, from.y + deltaY);
	}

	Direction search() {
		int index = this.ordinal();
		return this.values()[(index + 3) % this.values().length];
	}

	Direction next() {
		return values()[(ordinal() + 7) % values().length];
	}
	
	Direction previous() {
		return values()[ (ordinal() + 1) % values().length];
	}

	final Stream<Direction> searchStream() {
		final Direction start = search();
		final Direction stop = values()[ ( start.ordinal() + 1 ) % values().length];
//		System.err.println("This= " + this + ", start= " + start + ", stop=" + stop);
		return Stream.iterate(start, d -> {
//			System.err.println("Check: " + d);
			return !Objects.equals(d, stop);
			}, d -> {
		
//			System.err.println("D: " + d);
			return values()[ (d.ordinal() + values().length-1) % values().length]; 
		});
}

	boolean isOpposite(Direction dir) {
		return ((ordinal() + 4) % values().length) == dir.ordinal();
	}

}
