package riskfx.mapeditor.outliner;

import java.util.Objects;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Outliner {
	
	public static Outliner newIntrinsicOutliner(final Image image) {
		return new IntrinsicOutliner(image);
	}
	
	public Flux<OutlineEvent> outline(final Color target, final int startX, final int startY);
	
	public Flux<OutlineEvent> outline(final int startX, final int startY);
	
	default public Mono<String> outlineSvg(final Color target,final int startX, final int startY ) {
		return toSvg(outline(target,startX,startY));
	}
	
	public static Mono<String> toSvg(final Flux<OutlineEvent> flux) {
		return flux.filter(evt -> Objects.equals(OutlineEvent.BOUNDARY, evt.getEventType().getSuperType()))
				.collect(() -> Compressor.create(), (Compressor c, OutlineEvent evt) -> c.combine(evt))
//				.map(sb -> sb.append(" z"))
				.map(Compressor::svg);
	}
	
	
	public static StringBuilder newStringBuilder() {
		return new StringBuilder("M");
	}
	
	public static StringBuilder combine(final StringBuilder sb, final OutlineEvent evt) {
		if (sb.length() > 1) {
			sb.append(" L");
		}
		sb.append(" ").append(evt.x).append(",").append(evt.y);
		return sb;
	}
}
