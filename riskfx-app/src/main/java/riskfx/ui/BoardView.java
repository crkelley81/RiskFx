package riskfx.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import freetimelabs.io.reactorfx.flux.FxFlux;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableStringProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.css.StyleableStringProperty;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import riskfx.app.util.role.Identifiable;

public class BoardView<T extends Identifiable, V extends TerritoryView> extends Region {
	private static final String DEFAULT_STYLE_CLASS = "board";

	private static final StyleablePropertyFactory<BoardView<?,?>> FACTORY = new StyleablePropertyFactory<>(Region.getClassCssMetaData());
	
	private final DoubleProperty zoomProperty = new SimpleDoubleProperty(this, "zoom", 1.0);
	public final DoubleProperty zoomProperty()				{	return this.zoomProperty; }
	public final double getZoom()							{	return this.zoomProperty.get(); }
	public final void setZoom(final double d)				{	this.zoomProperty.set(d); }
	

	private final ReadOnlyStringWrapper skinUrlProperty = new ReadOnlyStringWrapper(this, "skinUrl");
	public final ReadOnlyStringProperty skinUrlProperty()	{	return this.skinUrlProperty.getReadOnlyProperty(); }
	public final String getSkinUrl()						{	return this.skinUrlProperty.getValue(); }

	private final ObjectProperty<Image> backgroundImageProperty = new SimpleObjectProperty<>(this, "backgroundImage");
	public final ObjectProperty<Image> backgroundImageProperty() { return this.backgroundImageProperty; }
	public final Image getBackgroundImage()					{	return this.backgroundImageProperty.getValue(); }
	public final void setBackgroundImage(final Image img)	{	this.backgroundImageProperty.set(img); }
	
	private final BooleanProperty backgroundImageVisibleProperty = new SimpleBooleanProperty(this, "backgroungImageVisible", true);
	public final BooleanProperty backgroundImageVisibleProperty() { return this.backgroundImageVisibleProperty; }
	public final boolean isBackgroundImageVisible()			{	return this.backgroundImageVisibleProperty.getValue(); }
	public final void setBackgroundImageVisible(boolean b) 	{	this.backgroundImageVisibleProperty.setValue(b); }
	
	private final ListProperty<T> itemsProperty = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());
	public final ObservableList<T> items()					{	return itemsProperty; }
	
	/*
	 * Styleable properties
	 */

	private static final CssMetaData<BoardView<?,?>, String> CSS_BACKGROUND_IMAGE_URL = FACTORY.createUrlCssMetaData("-background-image", s -> s.backgroundImageUrlProperty);
	private final StyleableStringProperty backgroundImageUrlProperty = new SimpleStyleableStringProperty(CSS_BACKGROUND_IMAGE_URL, this, "backgroundImage") ;
	public final StringProperty backgroundImageUrlProperty()	{	return this.backgroundImageUrlProperty; }
	public final String getBackgroundImageUrl()					{	return this.backgroundImageUrlProperty.get(); }
	public final void setBackgroundImageUrl(final String url) 	{ 	this.backgroundImageUrlProperty.set(url); }
	
	/*
	 * 
	 */
	
	private final ImageView imageView = new ImageView();
	private final Rectangle frame = new Rectangle();
	private final Group territoriesGroup = new Group(frame);
	private final Group zoomGroup = new Group(imageView, territoriesGroup);
	private final ScrollPane scrollPane = new ScrollPane(zoomGroup);
	private final Scale scale = new Scale(1.0, 1.0);
	private final StackPane root = new StackPane(scrollPane);
	
	private final Map<T, V> views = new HashMap<>();

	private Function<T, V> territoryViewFactory;
	
	
	public BoardView() {
		super();

		this.getStyleClass().add(DEFAULT_STYLE_CLASS);

		skinUrlProperty.addListener((o, ov, nv) -> onSkinUrlChanged(ov, nv));
		backgroundImageUrlProperty.addListener((o, ov, nv) -> onBackgroundImageUrlChanged(nv));
		
		getChildren().add(root);
		
		scale.xProperty().bind(this.zoomProperty());
		scale.yProperty().bind(this.zoomProperty());
		zoomGroup.getTransforms().add(scale);
		
		imageView.imageProperty().bind(this.backgroundImageProperty());
		imageView.visibleProperty().bind(imageView.imageProperty().isNotNull().and(this.backgroundImageVisibleProperty));
		imageView.managedProperty().bind(imageView.visibleProperty());
		imageView.setFitWidth(-1);
		imageView.setFitHeight(-1);
		imageView.setId("backgroundImageView");
		
		frame.setFill(Color.TRANSPARENT);
		frame.setMouseTransparent(true);
		
		FxFlux.from(imageView.layoutBoundsProperty())
			.subscribe(b -> {
				frame.setWidth( b.getWidth());
				frame.setHeight( b.getHeight());
			});
		
		territoriesGroup.setAutoSizeChildren(false);
		
		FxFlux.fromAdditionsOf(this.items())
			.map(this::createTerritoryView)
			.subscribe( tv -> {
				territoriesGroup.getChildren().add(tv.asNode());
			});
		FxFlux.fromRemovalsOf(this.items())
			.subscribe(t -> {
				final V v = views.remove(t);
				territoriesGroup.getChildren().remove(v);
			});
	}

	@Override
	public final String getUserAgentStylesheet() {
		return Objects.requireNonNull(getClass().getResource("board.css")).toExternalForm();
	}

	private void onSkinUrlChanged(final String ov, final String nv) {
		this.getStylesheets().remove(ov);
		this.getStylesheets().add(nv);
	}
	
	private void onBackgroundImageUrlChanged(final String nv) {
		Optional.ofNullable(nv).ifPresentOrElse( 
				s -> {
					final Image image = new Image(s);
					this.setBackgroundImage(image);
				},
				() -> {
					this.setBackgroundImage(null);
					});
	}
	 
	/* package */ V createTerritoryView(T id) {
		final Function<T,V> factory = this.territoryViewFactory;
		return factory.apply(id);
	}
	public void setItems(ObservableList<T> territories) {
		this.itemsProperty.set(territories);
	}
	public void setTerritoryViewFactory(final Function<T,V> territoryViewFactory) {
		this.territoryViewFactory = Objects.requireNonNull(territoryViewFactory);
	}
}
