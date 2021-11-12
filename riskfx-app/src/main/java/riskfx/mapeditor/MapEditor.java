package riskfx.mapeditor;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import appfx.ui.UiContext;
import appfx.util.FxmlView;
import appfx.util.UserNotification;
import appfx.window.AppBar;
import freetimelabs.io.reactorfx.flux.FxFlux;
import freetimelabs.io.reactorfx.schedulers.FxSchedulers;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.stage.FileChooser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import riskfx.mapeditor.io.MapIO;
import riskfx.mapeditor.io.MapImporter;
import riskfx.mapeditor.model.MapSkin;
import riskfx.mapeditor.model.TerritorySkin;

public class MapEditor extends FxmlView {

	private final UiContext context;
	private final MapEditorModel model;
	private final MapIO io;

	@Inject
	public MapEditor(final UiContext context, final MapEditorModel model, final MapIO io) {
		this.context = Objects.requireNonNull(context);
		this.model = Objects.requireNonNull(model);
		this.io = Objects.requireNonNull(io);
	}
	
	public void updateAppBar(final AppBar appBar) {
		final Button navIconBtn = new Button("Nav");
		navIconBtn.setId("navIconBtn");
		appBar.navIconProperty().set(navIconBtn);
		
		final ContextMenu menu = new ContextMenu();
		navIconBtn.setOnAction(evt -> {
			System.err.println("Show menu");
			
			menu.show(navIconBtn, Side.BOTTOM, 0, 0);
//			menu.show(navIconBtn, 0, 0);
			});
		
		newMapBtn = new MenuItem("New Map");
		newMapBtn.setId("newMapBtn");
		newMapBtn.setOnAction(evt -> this.newMap());
		
		menu.getItems().add(newMapBtn);
	}
	
	@FXML public final void clearSearch() {
		searchField.clear();
	}

	@FXML
	public final void newMap() {
		final MapSkin map = new MapSkin();
		model.edit(map);
	}

	public final Mono<MapSkin> openMap(final Path path) {
		return Mono.fromCallable(() -> {
			return io.openMap(path);
		}).subscribeOn(Schedulers.boundedElastic()).publishOn(FxSchedulers.fxThread())
				.doOnSubscribe(s -> context.lockView(UserNotification.of("Opening map from %s", path.getFileName())))
				.doOnNext(skin -> model.edit(skin, Optional.of(path)))
				.doOnError(ex -> context
						.notifyError(UserNotification.of("Could not open map from %s", path.getFileName()), ex))
				.doAfterTerminate(() -> context.unlockView());
	}

	@FXML
	public final void openMap() {
		context.showOpenFileDialog(c -> {
		}).map(File::toPath).flatMap(p -> openMap(p)).subscribe();
	}

	public final Mono<MapSkin> saveMap(final MapSkin skin, final Path path) {
		return Mono.fromCallable(() -> {
			return io.saveMap(skin, path);
		}).subscribeOn(Schedulers.boundedElastic()).publishOn(FxSchedulers.fxThread())
				.doOnSubscribe(s -> context.lockView(null)).doOnNext(m -> {
					context.notify(UserNotification.of(""));
					model.edit(m, Optional.of(path));
				})
				.doOnError(ex -> context
						.notifyError(UserNotification.of("Could not open map from %s", path.getFileName()), ex))
				.doAfterTerminate(() -> context.unlockView());
	}

	@FXML
	public final void saveMap() {
		saveMap(model.getMap(), null);
	}

	@FXML
	public final void saveMapAs() {
		context.showOpenFileDialog(c -> {
		}).map(File::toPath).subscribe(path -> saveMap(model.getMap(), path));
	}

	public final Mono<MapSkin> importWith(final MapImporter importer, final Path path) {
		return Mono.fromCallable(() -> {
			return importer.importMap(path);
		}).subscribeOn(Schedulers.boundedElastic())
				.publishOn(FxSchedulers.fxThread())
				.doOnSubscribe(s -> context.lockView(UserNotification.of("")))
				.doOnNext(skin -> {
					this.model.edit(skin);
				})
				.doOnError(ex -> context.notifyError(UserNotification.of(""), ex))
				.doAfterTerminate(() -> context.unlockView());
	}

	public final void importWith(final MapImporter importer) {
		context.showOpenFileDialog(c -> {
		}).map(File::toPath)
		.flatMap(path -> importWith(importer, path))
		.subscribe();
	}
	
	@FXML
	public void newTerritory() {
		final TerritorySkin skin = model.newTerritory("untitled", "Untitled");
		model.select(skin);

		Platform.runLater(() -> {
			Optional.ofNullable(territoriesPane).ifPresent(tp -> tp.setExpanded(true));
			Optional.ofNullable(territoryIdField).ifPresent(tc -> tc.requestFocus());
		});

	}

	@FXML
	public void chooseBackgroundImage() {
		context.showOpenFileDialog(this::openImage).flatMap(f -> Mono.fromCallable(() -> {
			return f.toURI().toURL();
		})).subscribe(u -> {
			// TODO set background image
		});

	}

	@FXML
	public void chooseReliefImage() {
		context.showOpenFileDialog(this::openImage).flatMap(f -> Mono.fromCallable(() -> {
			return f.toURI().toURL();
		})).subscribe(u -> {
			// TODO set background image
		});

	}

	private void openImage(final FileChooser chooser) {
		// TODO
	}

	@FXML public final void toggleSort() {
		model.toggleSort();
	}
	
	@FXML
	public void initialize() {
		territoriesList.setItems(model.territories());
		territoriesList.setCellFactory(TerritorySkinCellFactory.forListView());
		territoriesList.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
			model.select(nv);
		});
		
		
		FxFlux.from(searchField.textProperty())
			.sampleTimeout(s -> Flux.interval(Duration.ofMillis(300)).take(1).publishOn(FxSchedulers.fxThread()))
			.subscribe(s -> model.search(s));
		
		final TextFormatter<URL> backgroundFormatter = new TextFormatter<>(Formatters.url());
		final TextFormatter<URL> reliefFormatter = new TextFormatter<>(Formatters.url());
		 
		Bind.bind(mapIdField, model.mapProperty(), MapSkin::idProperty);
		Bind.bind(mapNameField, model.mapProperty(), MapSkin::displayNameProperty);
		Bind.bind(mapAuthorField, model.mapProperty(), MapSkin::authorProperty);
		Bind.bind(mapDescriptionField, model.mapProperty(), MapSkin::descriptionProperty);
//		Bind.bind(backgroundFormatter.valueProperty(), model.mapProperty(), t -> (URL) null);

		model.mapProperty().addListener(this::onMapSkinChanged);
		model.selectedTerritoryProperty().addListener(this::onSelectedTerritoryChanged);

		/*
		saveAsBtn.disableProperty().bind(model.mapProperty().isNull());
		saveBtn.disableProperty().bind(model.mapProperty().isNull());
		importMenu.setOnShowing(evt -> buildImportMenu());
		*/
		
		// Territory editor
		final TextFormatter<Integer> xFormatter = null;
		final TextFormatter<Integer> yFormatter = null;
		
		Bind.bind(territoryIdField, model.selectedTerritoryProperty(), TerritorySkin::idProperty);
//		Bind.bind(territoryNameField, model.selectedTerritoryProperty(), TerritorySkin::nameProperty);
	}

	private void buildImportMenu() {
		importMenu.getItems().clear();
		io.importers().stream()
			.map(this::createImporterMenuItem)
			.forEach(mi -> importMenu.getItems().add(mi));
	}

	private MenuItem createImporterMenuItem(final MapImporter importer) {
		final MenuItem mi = new MenuItem(importer.name());
		mi.setOnAction(evt -> importWith(importer));
		return mi;
	}
	
	private void onMapSkinChanged(final ObservableValue<? extends MapSkin> o, final MapSkin ov, final MapSkin nv) {
		if (nv != null) {
			mapIdField.textProperty().bindBidirectional(nv.idProperty());
		} else {
			mapIdField.textProperty().unbind();
		}

		mapPane.setExpanded(true);
		mapIdField.requestFocus();
	}

	private void onSelectedTerritoryChanged(final ObservableValue<? extends TerritorySkin> o, final TerritorySkin ov,
			final TerritorySkin nv) {
		if (nv != null) {
			territoriesList.getSelectionModel().select(nv);
			territoriesPane.setExpanded(true);
//			territoryIdField.requestFocus();
		} else {
			territoriesList.getSelectionModel().clearSelection();
		}
	}

	@FXML
	private MenuItem newMapBtn;
	@FXML
	private MenuItem openMapBtn;
	@FXML
	private MenuItem saveAsBtn;
	@FXML
	private MenuItem saveBtn;
	@FXML private Menu importMenu;
	
	
	@FXML
	private TextField searchField;

	@FXML
	private Button toggleSortBtn;
	@FXML
	private Button newTerritoryBtn;

	@FXML
	private TextField mapIdField;
	@FXML
	private TextField mapNameField;
	@FXML
	private TextField mapAuthorField;
	@FXML
	private TextArea mapDescriptionField;
	@FXML
	private TextField mapBackgroundField;
	@FXML
	private TextField mapReliefField;

	@FXML
	private TitledPane mapPane;
	@FXML
	private TitledPane territoriesPane;

	@FXML
	private ListView<TerritorySkin> territoriesList;

	@FXML
	private TextField territoryIdField;
	@FXML
	private TextField territoryNameField;
	@FXML
	private TextField territoryXField;
	@FXML
	private TextField territoryYField;
	@FXML
	private TextArea territoryShapeField;

	
}
