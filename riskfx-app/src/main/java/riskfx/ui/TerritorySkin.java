package riskfx.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import appfx.role.As;
import appfx.role.CssRenderable;
import freetimelabs.io.reactorfx.flux.FxFlux;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.SimpleStyleableDoubleProperty;
import javafx.css.SimpleStyleableStringProperty;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.css.StyleableStringProperty;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import reactor.core.publisher.Flux;
import riskfx.app.util.role.Identifiable;
import riskfx.app.util.role.MutableDisplayable;
import riskfx.app.util.role.MutableIdentifiable;

public class TerritorySkin<T extends Identifiable> extends Region implements TerritoryView<T>, MutableIdentifiable, MutableDisplayable, As, CssRenderable {

	private static final StyleablePropertyFactory<TerritorySkin<?>> FACTORY = new StyleablePropertyFactory<>(Region.getClassCssMetaData());
	private static final PseudoClass PSEUDO_SELECTED = PseudoClass.getPseudoClass("selected");
	
	
	private final Collection<Object> lookups;
	
	private final BooleanProperty selectedProperty = new SimpleBooleanProperty(this, "selected", false) {

		@Override
		protected void invalidated() {
			TerritorySkin.this.pseudoClassStateChanged(PSEUDO_SELECTED, get());
		}
		
	};
	@Override
	public final ReadOnlyBooleanProperty selectedProperty() {
		return this.selectedProperty;
	}
	
	private final StringProperty displayNameProperty = new SimpleStringProperty(this, "displayName", "Untitled");
	public final StringProperty displayNameProperty()	{	return this.displayNameProperty; }

	
	private final CssMetaData<TerritorySkin<?>, Number> CSS_INDICATORY_X = FACTORY.createSizeCssMetaData("-territory-indicatory-x" ,s -> s.indicatorXProperty, 0.0);
	private final StyleableDoubleProperty indicatorXProperty = new SimpleStyleableDoubleProperty(CSS_INDICATORY_X, this, "indicatorX", 0.0);
	public final DoubleProperty indicatorXProperty()	{	return this.indicatorXProperty; }
	public final double getIndicatorX()					{	return this.indicatorXProperty.get(); }
	public final void setIndicatorX(final double x)		{	this.indicatorXProperty.set(x); }
	
	private final CssMetaData<TerritorySkin<?>, Number> CSS_INDICATORY_Y = FACTORY.createSizeCssMetaData("-territory-indicatory-y" ,s -> s.indicatorYProperty, 0.0);
	private final StyleableDoubleProperty indicatorYProperty = new SimpleStyleableDoubleProperty(CSS_INDICATORY_Y, this, "indicatorY", 0.0);
	public final DoubleProperty indicatorYProperty()	{	return this.indicatorYProperty; }
	public final double getIndicatorY()					{	return this.indicatorYProperty.get(); }
	public final void setIndicatorY(final double y)		{	this.indicatorYProperty.set(y); }
	
	private final CssMetaData<TerritorySkin<?>, String> CSS_BACKGROUND_SHAPE = FACTORY.createStringCssMetaData("-territory-shape", s -> s.backgroundShapeProperty, "M10,10 L190,10 L100,190 z");
	private final StyleableStringProperty backgroundShapeProperty = new SimpleStyleableStringProperty(CSS_BACKGROUND_SHAPE, this, "backgroundShape", "M10,10 L190,10 L100,190 z");
	public final StringProperty backgroundShapeProperty() {	return this.backgroundShapeProperty; }
	public final String getBackgroundShape()			{	return this.backgroundShapeProperty.getValue(); }
	public final void setBackgroundShape(final String s) {	this.backgroundShapeProperty.setValue(s); }
	
	private final StringProperty indicatorTextProperty = new SimpleStringProperty(this, "indicatorText");
	public final StringProperty indicatorTextProperty()	{	return this.indicatorTextProperty; }
	public final String getIndicatorText()				{	return this.indicatorTextProperty.getValue(); }
	public final void setIndicatorText(final String s)	{	this.indicatorTextProperty.setValue(s); }
	
	private final DoubleProperty indicatorSizeProperty = new SimpleDoubleProperty(this, "indicatorSize", 10.0);
	public final DoubleProperty indicatorSizeProperty() {	return this.indicatorSizeProperty; }
	public final double getIndicatorSize()				{	return this.indicatorSizeProperty.get(); }
	public final void setIndicatorSize(final double size) {	this.indicatorSizeProperty.set(size); }
	
	private final ObjectProperty<Color> backgroundColorProperty = new SimpleObjectProperty<>(this, "backgroundColor", Color.BLACK);
	public final ObjectProperty<Color> backgroundColorProperty() { return this.backgroundColorProperty; }
	
	private final DoubleProperty backgroundFadeProperty = new SimpleDoubleProperty(this, "backgroundFade", 0.3);
	public final DoubleProperty backgroundFadeProperty() {	return this.backgroundFadeProperty; }
	
	private final ObjectProperty<Color> fadedBackgroundColorProperty = new SimpleObjectProperty<>(this, "fadedBackroundColor", Color.BLACK);
	public final ObjectProperty<Color> fadedBackgroundColorProperty() { return this.fadedBackgroundColorProperty; }
	
	/*
	 * Nodes
	 */
	private final SVGPath background = new SVGPath();
	private final SVGPath clip = new SVGPath();
	private final SVGPath shape = new SVGPath();
	private final Circle indicator = new Circle();
	private final Text indicatorText = new Text();
	private final StackPane indicatorPane = new StackPane(indicator, indicatorText);
	
	
	public TerritorySkin(final String id, final String name, final Object...lookups) {
		super();
		
		Objects.requireNonNull(id);
		Objects.requireNonNull(name);
		
		setId(id);
		setDisplayName(name);
		
		this.lookups = Arrays.asList(lookups);
		
		Flux.combineLatest(
				FxFlux.from(this.backgroundColorProperty),
				FxFlux.from(this.backgroundFadeProperty),
				TerritorySkin::fadeColor)
			.subscribe(fadedBackgroundColorProperty::set);
		
		// Control 
		getStyleClass().add("territory");
		
		indicatorText.getStyleClass().add("territory-indicator-text");
		indicatorText.textProperty().bind(indicatorTextProperty);
		indicatorText.visibleProperty().bind(this.indicatorTextProperty.isNotNull());
		indicatorText.managedProperty().bind(indicatorText.visibleProperty());
		
		indicator.getStyleClass().add("territory-indicator");
		indicator.radiusProperty().bind(indicatorSizeProperty);
		
		indicatorPane.visibleProperty().bind(indicatorSizeProperty.greaterThan(0.0));
		indicatorPane.managedProperty().bind(indicatorPane.visibleProperty());
		indicatorPane.layoutXProperty().bind(indicatorXProperty.subtract(indicatorSizeProperty.divide(2.0)));
		indicatorPane.layoutYProperty().bind(indicatorYProperty.subtract(indicatorSizeProperty.divide(2.0)));
	
		background.getStyleClass().add("territory-background");
		background.fillProperty().bind(fadedBackgroundColorProperty);
		background.contentProperty().bind(backgroundShapeProperty);
		
		clip.contentProperty().bind(backgroundShapeProperty);
		shape.contentProperty().bind(backgroundShapeProperty);
		
		setClip(clip);
		setShape(shape);
		
		getChildren().setAll(background, indicatorPane);
	}
	
	
	
	@Override
	public final List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
		return FACTORY.getCssMetaData();
	}
	@Override
	public final String getUserAgentStylesheet() {
		return Objects.requireNonNull(getClass().getResource("territory.css")).toExternalForm();
	}
	@Override
	public final void renderCss(StringBuilder buffer) {
		buffer.append("#").append(getId()).append(" {").append("\n");
		buffer.append("\t-indicator-x: ").append(getIndicatorX()).append(";\n");
		buffer.append("\t-indicator-y: ").append(getIndicatorY()).append(";\n");
		buffer.append("\t-indicator-size: ").append(getIndicatorSize()).append(";\n");
		buffer.append("\t-territory-shape: \"").append(getBackgroundShape()).append("\";\n");
		buffer.append("}").append("\n");
		buffer.append("\n");
	}
	
	@Override
	public final <X> Optional<X> as(final Class<X> clazz) {
		return As.as(clazz, lookups);
	}
	
	@Override
	public void select() {
		this.selectedProperty.set(true);
		this.fireEvent(new TerritoryView.Event(TerritoryView.Event.SELECTED, this));
	}
	@Override
	public void deselect() {
		this.selectedProperty.set(false);
		this.fireEvent(new TerritoryView.Event(TerritoryView.Event.DESELECTED, this));
	}
	
	@Override
	public Node asNode() {
		return this;
	}
	@Override
	public final ReadOnlyBooleanProperty hoveredProperty() {
		return super.hoverProperty();
	}
	
	private static Color fadeColor(final Color base, final Number fade) {
		final double factor = clamp(fade.doubleValue(), 0, 1.0);
		final Color result = base.deriveColor(0, 1, 1, factor);
//		final Color result = Color.rgb((int) c.getRed(), (int) c.getGreen(), (int) c.getBlue(), factor);
//		System.err.println("Fade %s at %s to %s".formatted(c, fade, result));
		return result;
	}
	private static double clamp(double v, double min, double max) {
		if (v < min) return min;
		if (v > max) return max;
		return v;
	}
}
	
