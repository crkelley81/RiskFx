package riskfx.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

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
import javafx.event.EventType;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import reactor.core.publisher.Flux;
import riskfx.engine.model.Territory;
import riskfx.util.role.Identifiable;

public class BoardView<T extends Identifiable, V extends TerritoryView> extends Region {
	private static final String DEFAULT_STYLE_CLASS = "board";

	private static final StyleablePropertyFactory<BoardView<?, ?>> FACTORY = new StyleablePropertyFactory<>(
			Region.getClassCssMetaData());

	private final DoubleProperty zoomProperty = new SimpleDoubleProperty(this, "zoom", 1.0);

	public final DoubleProperty zoomProperty() {
		return this.zoomProperty;
	}

	public final double getZoom() {
		return this.zoomProperty.get();
	}

	public final void setZoom(final double d) {
		this.zoomProperty.set(d);
	}

	private final ReadOnlyStringWrapper skinUrlProperty = new ReadOnlyStringWrapper(this, "skinUrl");

	public final ReadOnlyStringProperty skinUrlProperty() {
		return this.skinUrlProperty.getReadOnlyProperty();
	}

	public final String getSkinUrl() {
		return this.skinUrlProperty.getValue();
	}

	private final ObjectProperty<Image> backgroundImageProperty = new SimpleObjectProperty<>(this, "backgroundImage");

	public final ObjectProperty<Image> backgroundImageProperty() {
		return this.backgroundImageProperty;
	}

	public final Image getBackgroundImage() {
		return this.backgroundImageProperty.getValue();
	}

	public final void setBackgroundImage(final Image img) {
		this.backgroundImageProperty.set(img);
	}

	private final BooleanProperty backgroundImageVisibleProperty = new SimpleBooleanProperty(this,
			"backgroungImageVisible", true);

	public final BooleanProperty backgroundImageVisibleProperty() {
		return this.backgroundImageVisibleProperty;
	}

	public final boolean isBackgroundImageVisible() {
		return this.backgroundImageVisibleProperty.getValue();
	}

	public final void setBackgroundImageVisible(boolean b) {
		this.backgroundImageVisibleProperty.setValue(b);
	}

	private final ListProperty<T> itemsProperty = new SimpleListProperty<>(this, "items",
			FXCollections.observableArrayList());

	public final ObservableList<T> items() {
		return itemsProperty;
	}

	/*
	 * Styleable properties
	 */

	private static final CssMetaData<BoardView<?, ?>, String> CSS_BACKGROUND_IMAGE_URL = FACTORY
			.createUrlCssMetaData("-background-image", s -> s.backgroundImageUrlProperty);
	private final StyleableStringProperty backgroundImageUrlProperty = new SimpleStyleableStringProperty(
			CSS_BACKGROUND_IMAGE_URL, this, "backgroundImage");

	public final StringProperty backgroundImageUrlProperty() {
		return this.backgroundImageUrlProperty;
	}

	public final String getBackgroundImageUrl() {
		return this.backgroundImageUrlProperty.get();
	}

	public final void setBackgroundImageUrl(final String url) {
		this.backgroundImageUrlProperty.set(url);
	}

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
		imageView.visibleProperty()
				.bind(imageView.imageProperty().isNotNull().and(this.backgroundImageVisibleProperty));
		imageView.managedProperty().bind(imageView.visibleProperty());
		imageView.setFitWidth(-1);
		imageView.setFitHeight(-1);
		imageView.setId("backgroundImageView");

		frame.setFill(Color.TRANSPARENT);
		frame.setMouseTransparent(true);
//		frame.setWidth(1000);
//		frame.setHeight(800);

//		scrollPane.setPrefViewportWidth(800);
//		scrollPane.setPrefViewportHeight(600);

		scrollPane.setMaxHeight(Double.MAX_VALUE);
		scrollPane.setMaxWidth(Double.MAX_VALUE);
		
		root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
////		root.setStyle("-fx-fill: gray;");
//
//		FxFlux.from(this.backgroundImageProperty).switchMap(image -> FxFlux.from(image.widthProperty()))
//				.subscribe(width -> {
//					frame.setWidth(this.getBackgroundImage().getWidth());
//					frame.setHeight(this.getBackgroundImage().getHeight());
//				});
//
////		frame.setWidth(800);
////		frame.setHeight(600);
//
//		FxFlux.from(territoriesGroup.layoutBoundsProperty()).subscribe(b -> System.err.println("Bounds: " + b));

		territoriesGroup.setAutoSizeChildren(false);

		FxFlux.fromAdditionsOf(this.items()).map(this::createTerritoryView).subscribe(tv -> {
			territoriesGroup.getChildren().add(tv.asNode());
		});
		FxFlux.fromRemovalsOf(this.items()).subscribe(t -> {
			final V v = views.remove(t);
			territoriesGroup.getChildren().remove(v);
		});
	}

	public void bounds() {
		System.err.println("BoardView: " + this.getLayoutBounds());
		System.err.println("  Root: " + root.getLayoutBounds());
		System.err.println("     Scroll: " + scrollPane.getLayoutBounds());
		System.err.println("        Zoom: " + zoomGroup.getLayoutBounds());
		System.err.println("           Terrs: " + territoriesGroup.getLayoutBounds());
		System.err.println("           ImageView: " + this.imageView.getLayoutBounds());
		
		System.err.println("Scale required: " + this.getWidth() / zoomGroup.getLayoutBounds().getWidth());
		System.err.println("Scale required: " + this.getHeight() / zoomGroup.getLayoutBounds().getHeight());
		
	}
	
	
	
	@Override
	protected void layoutChildren() {
		super.layoutChildren();
		
		final double width = this.getLayoutBounds().getWidth();
		final double height = this.getLayoutBounds().getHeight();
		
		this.layoutInArea(root, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
		
		final double scaleX = width / zoomGroup.getLayoutBounds().getWidth();
		final double scaleY = height / zoomGroup.getLayoutBounds().getHeight();
		final double scale = Math.min(scaleX, scaleY);
		
		this.setZoom(scale);
	}

	@Override
	public final String getUserAgentStylesheet() {
		return Objects.requireNonNull(getClass().getResource("board.css")).toExternalForm();
	}

	public void setItems(ObservableList<T> territories) {
		this.itemsProperty.set(territories);
	}

	public void setTerritoryViewFactory(final Function<T, V> territoryViewFactory) {
		this.territoryViewFactory = Objects.requireNonNull(territoryViewFactory);
	}

	public final <E extends MouseEvent> Flux<V> eventsOn(final EventType<E> eventType) {
		return FxFlux.from(zoomGroup, eventType)
//				.doOnNext(evt -> System.err.println("Event: " + evt))
				.map(this::findTerritory)
				.filter(Optional::isPresent)
				.map(Optional::get);
	}

	private Optional<V> findTerritory(final MouseEvent evt) {
		return findTerritory(evt.getX(), evt.getY());
	}

	private Optional<V> findTerritory(double x, double y) {
		return territoryViews().filter(tv -> tv.hit(x, y)).findFirst();
	}

	private Stream<V> territoryViews() {
		return views.values().stream();
	}

	private void onSkinUrlChanged(final String ov, final String nv) {
		this.getStylesheets().remove(ov);
		this.getStylesheets().add(nv);
	}

	private void onBackgroundImageUrlChanged(final String nv) {
		Optional.ofNullable(nv).ifPresentOrElse(s -> {
			final Image image = new Image(s);
			this.setBackgroundImage(image);
		}, () -> {
			this.setBackgroundImage(null);
		});
	}

	/* package */ V createTerritoryView(T id) {
		final Function<T, V> factory = this.territoryViewFactory;
		final V view = factory.apply(id);
		this.views.put(id, view);
		return view;
	}

	public void select(T other) {
		views.get(other).select();
	}

	public void deselect(T selected) {
		views.get(selected).deselect();
	}

	public void clearSelection() {
		views.values().forEach(v -> v.deselect());
	}

}
