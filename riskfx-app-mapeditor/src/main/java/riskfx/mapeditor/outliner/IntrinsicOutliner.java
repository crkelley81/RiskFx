package riskfx.mapeditor.outliner;

import java.awt.Point;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

public class IntrinsicOutliner implements Outliner {
	public class Done extends State {

		public Done(Color targetColor, int startX, int startY, int x, int y, Direction nextDir) {
			super(targetColor, startX, startY, x, y, nextDir);
		}

		@Override
		protected State nextState(SynchronousSink<OutlineEvent> sink) {
			sink.complete();
			return null;
		}

	}

	private static final Logger LOG = Logger.getLogger(IntrinsicOutliner.class.getName());

	public class Trace extends State {
		private final Set<Point> points;

		public Trace(Color targetColor, int startX, int startY, int x, int y, Direction dir, final Set<Point> points) {
			super(targetColor, startX, startY, x, y, dir);
			this.points = points;
		}

		@Override
		protected State nextState(SynchronousSink<OutlineEvent> sink) {
//			LOG.info("Trace: target=%s, x=%s, y=%s".formatted(targetColor, x, y));
			
			if (!Objects.equals(targetColor, image.getPixelReader().getColor(x, y))) {
				sink.error(new IllegalStateException("Expected %s starting at (%s, %s), but got %s".formatted(targetColor, x, y, image.getPixelReader().getColor(x, y))));
				return null;
			}
			final Point current = new Point(x, y);
			final Point next = new Point();

			final Optional<Direction> result = dir.searchStream()
					.filter(d -> isBoundary(targetColor, d, current.x, current.y, false))
//					.peek(d -> System.err.println("D: passed! " + d))
					.map(Direction::previous)
					.filter(d -> !d.isOpposite(dir))
					.findFirst();

			
			
			if (result.isPresent()) {
				Direction nextDir = result.get();
				nextDir.translateFrom(current, next);
				
				
				if ((next.x == startX) && (next.y == startY)) {
					sink.next(OutlineEvent.finished());
					return new Done(targetColor, startX, startY, next.x, next.y, nextDir);
				} 
				else
				{
					sink.next(OutlineEvent.boundary(nextDir, next.x, next.y));
					return new Trace(targetColor, startX, startY, next.x, next.y, nextDir, points);
					}
				
				}
			else {
				debug(targetColor, dir, current.x, current.y);
				
				sink.error(new IllegalStateException("Cannot find boundary at " + x + ", " + y));
				return null;
			}

			
		}

	}

	abstract class State {
		final Color targetColor;
		final int startX;
		final int startY;
		final int x;
		final int y;
		final Direction dir;

		public State(Color targetColor, int startX, int startY, int x, int y, Direction dir) {
			super();
			this.targetColor = targetColor;
			this.startX = startX;
			this.startY = startY;
			this.x = x;
			this.y = y;
			this.dir = dir;
		}

		protected abstract State nextState(SynchronousSink<OutlineEvent> sink);
	}

	class Start extends State {

		public Start(Color target, int startX, int startY) {
			super(target, startX, startY, startX, startY, Direction.NORTH);
		}

		@Override
		protected State nextState(SynchronousSink<OutlineEvent> sink) {
			LOG.info("Begining outline scan for %s at (%s, %s)".formatted(targetColor, startX, startY));
			sink.next(OutlineEvent.starting(targetColor, startX, startY));
			return new Scan(targetColor, startX, startY, x, y, dir);
		}
	}

	class Scan extends State {

		public Scan(Color targetColor, int startX, int startY, int x, int y, Direction dir) {
			super(targetColor, startX, startY, x, y, dir);
		}

		@Override
		protected State nextState(final SynchronousSink<OutlineEvent> sink) {
			final Point current = new Point(x, y);
			final Point next = new Point();
			boolean done = false;
			while (!done) {
				done = isBoundary(targetColor, dir, current.x, current.y, false);
				if (!done) dir.translateFrom(current, current);
			}

			sink.next(OutlineEvent.startBoundary(current.x, current.y));

			return new Trace(targetColor, current.x, current.y, current.x, current.y, dir, new HashSet<>());
		}

	}

	private final Image image;

	public IntrinsicOutliner(final Image image) {
		this.image = image;
	}

	public void debug(Color targetColor, Direction dir, int x, int y) {
		LOG.info("Debug checking boundary for (%s, %s) from %s with target=%s".formatted(x,y,dir,targetColor));
		dir.searchStream().forEach(d -> {
			final Point current = new Point(x,y);
			final Point next = new Point();
			d.translateFrom(current, next);
			final Color c = image.getPixelReader().getColor(next.x, next.y);
			boolean boundary = isBoundary(targetColor, d, current.x, current.x, true);
			System.err.println("%s (%s, %s): %s %s".formatted(d, next.x, next.y, c, boundary));
			
		});
	}

	public boolean isBoundary(final Color targetColor, final Direction dir, final int x, final int y, boolean debug) {
		final Point current = new Point(x, y);
		final Point test = new Point();
		
		dir.translateFrom(current, test);
		
		if (outOfBounds(test)) return true;
		
		final Color c = image.getPixelReader().getColor(test.x, test.y);
		if (debug) System.err.println("Checking (%s, %s): %s == %s".formatted(test.x, test.y, targetColor, c));
		return !Objects.equals(targetColor, c);
	}

	private boolean outOfBounds(Point test) {
		return (test.x < 0) || (test.y < 0) || (test.x >= image.getWidth()) || (test.y >= image.getHeight());
	}

	@Override
	public Flux<OutlineEvent> outline(Color target, int startX, int startY) {
		return Flux.generate(() -> new Start(target, startX, startY), this::nextState);
	}

	State nextState(final State state, final SynchronousSink<OutlineEvent> sink) {
		return state.nextState(sink);
	}

	@Override
	public Flux<OutlineEvent> outline(int startX, int startY) {
		return outline(image.getPixelReader().getColor(startX, startY), startX, startY);
	}

}
