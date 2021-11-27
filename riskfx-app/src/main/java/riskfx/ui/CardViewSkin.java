package riskfx.ui;

import java.util.stream.Stream;

import freetimelabs.io.reactorfx.flux.FxFlux;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

public class CardViewSkin extends SkinBase<CardView> implements Skin<CardView> {
	private static final double WIDTH = 160 + 16;
	private static final double HEIGHT = 200;
	
	private Label title = new Label();
	private Label subtitle = new Label();
	private VBox container = new VBox();

	private final Rectangle clip = new Rectangle();
	private final Rectangle shape = new Rectangle();

	private final StackPane primaryPane = new StackPane();
	private final SVGPath background = new SVGPath();
	private final SVGPath highlight = new SVGPath();
	private final SVGPath outline = new SVGPath();
	private final SVGPath territoryClip = new SVGPath();

	protected CardViewSkin(CardView cardView) {
		super(cardView);

		title.getStyleClass().add("card-title");
		title.textProperty().bind(cardView.titleProperty());

		subtitle.getStyleClass().add("card-subtitle");
//		subtitle.textProperty().bind(cardView.sub);

	Stream.of(clip, shape).forEach(s -> {
			s.setWidth(WIDTH);
			s.setHeight(HEIGHT);
			s.setArcWidth(20);
			s.setArcHeight(20);
		});

		container.setClip(clip);
		container.setShape(shape);

		
		title.getStyleClass().add("card-title");
		background.getStyleClass().add("card-territory-background");
		highlight.getStyleClass().add("card-territory-highlight");
		outline.getStyleClass().add("card-territory-outline");


		FxFlux.from(cardView.territoryOutlineProperty()).subscribe(this::updateOutline);

		background.fillProperty().bind(cardView.territoryFillProperty());
		highlight.strokeProperty().bind(cardView.territoryFillProperty());

		highlight.setClip(territoryClip);
		primaryPane.getChildren().addAll(background, highlight, outline);

		container.getChildren().addAll(title, primaryPane, subtitle);
		container.setAlignment(Pos.TOP_CENTER);
		container.setSpacing(16);
		getChildren().add(container);
	}

	private void updateOutline(final String svg) {
		background.setContent(svg);
		highlight.setContent(svg);
		outline.setContent(svg);
		territoryClip.setContent(svg);
		
		getSkinnable().requestLayout();
	}
	
	@Override
	protected double computeMaxHeight(double arg0, double arg1, double arg2, double arg3, double arg4) {
		return HEIGHT;
	}

	@Override
	protected double computeMaxWidth(double arg0, double arg1, double arg2, double arg3, double arg4) {
		return WIDTH;
	}

	@Override
	protected double computeMinHeight(double arg0, double arg1, double arg2, double arg3, double arg4) {
		return HEIGHT;
	}

	@Override
	protected double computeMinWidth(double arg0, double arg1, double arg2, double arg3, double arg4) {
		return WIDTH;
	}

	@Override
	protected double computePrefHeight(double arg0, double arg1, double arg2, double arg3, double arg4) {
		return HEIGHT;
	}

	@Override
	protected double computePrefWidth(double arg0, double arg1, double arg2, double arg3, double arg4) {
		return WIDTH;
	}
/*
	@Override
	protected void layoutChildren(double x, double y, double width, double height) {
		super.layoutChildren(x, y, width, height);
		System.err.println("Bounds: %s, %s, %s, %s".formatted(x, y, width, height));
	}
*/
}
