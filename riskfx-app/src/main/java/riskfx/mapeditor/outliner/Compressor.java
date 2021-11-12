package riskfx.mapeditor.outliner;

import javafx.geometry.Point2D;

public class Compressor {

	public static Compressor create() {
		return new Compressor();
	}
	
	private Point2D ref;
	private Point2D intermediate;
	private Point2D end;
	
	private final StringBuilder builder = new StringBuilder("M ");
	
	public Compressor combine(final OutlineEvent evt) {
		if (ref == null) {
			ref = new Point2D(evt.x, evt.y);
			builder.append(evt.x).append(",").append(evt.y).append(" ");
		}
		else if (intermediate == null) intermediate = new Point2D(evt.x, evt.y);
		else {
			end = new Point2D(evt.x, evt.y);
			
			Point2D dif1 = intermediate.subtract(ref);
			Point2D dif2 = end.subtract(intermediate);
			double slope1 = ref.angle(intermediate);
			double slope2 = intermediate.angle(end);
			
			System.err.println("Checking slopes: (%s, %s), (%s, %s), (%s, %s), %s, %s".formatted(ref.getX(), ref.getY(), intermediate.getX(), intermediate.getY(), evt.x, evt.y, dif1, dif2));
			if (dif1.equals(dif2)) {
				System.err.println("Suppressing: " + intermediate);
				intermediate = new Point2D(evt.x, evt.y);
				
			}
			else {
				builder.append("L ").append(intermediate.getX()).append(",").append(intermediate.getY()).append(" ");
				ref = new Point2D(intermediate.getX(), intermediate.getY());
				intermediate = new Point2D(evt.x, evt.y);
			}
		}
		
		return this;
	}
	
	public String svg() {
		return builder.append("z").toString();
	}
}
