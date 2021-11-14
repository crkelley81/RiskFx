package riskfx.mapeditor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.ListViewMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

import appfx.ui.ExitController;
import appfx.ui.FileDialogs;
import appfx.ui.UiContext;
import appfx.window.MainWindow;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import reactor.core.publisher.Mono;
import riskfx.mapeditor.MapEditorModel.SortDirection;
import riskfx.mapeditor.io.MapIO;
import riskfx.mapeditor.io.MapImporter;
import riskfx.mapeditor.io.domination.DominationMapImporter;
import riskfx.mapeditor.model.MapSkin;
import riskfx.ui.TerritorySkin;

@ExtendWith(ApplicationExtension.class)

class MapEditorViewTest {

	private static final long SHORT_PAUSE = 100;

	private static record TerritoryInfo(String id, String name, int x, int y) {
	}

	private static final TerritoryInfo[] TERRITORIES = {
			new TerritoryInfo("eastern-united-states", "Eastern United States", 100, 100),
			new TerritoryInfo("western-united-states", "Western United States", 100, 100),
			new TerritoryInfo("alaska", "Alaska", 100, 100) };

	private MainWindow mainWindow;
	private MapEditor mapEditor;

	private ExitController exitController = Mockito.mock(ExitController.class);
	private FileDialogs fileDialogs = Mockito.mock(FileDialogs.class);
	
	private UiContext context;
	private MapIO io = MapIO.create(); // Mockito.mock(MapIO.class);
	private MapEditorModel model = new MapEditorModel();

	private UiContext contextSpy;

	@Start
	public void start(final Stage stage) {
		mainWindow = new MainWindow();
		context = UiContext.of(mainWindow, fileDialogs, exitController);
		contextSpy = Mockito.spy(context);
		
		mapEditor = new MapEditor(contextSpy, model, io);
		mapEditor.inflateView();
		mainWindow.setContent(mapEditor);
		mapEditor.updateAppBar(mainWindow.getAppBar());

		final Scene scene = new Scene(mainWindow);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}
	
	@Test void mainMenuShouldGoToMainMenu(final FxRobot robot) {
		// GIVEN 
		Mockito.doNothing().when(contextSpy).goBack();
		// WHEN 
		robot.clickOn("#navIconBtn").sleep(SHORT_PAUSE);
		robot.clickOn("Main Menu");
		
		// THEN 
		Mockito.verify(contextSpy).goBack();
	}

	@Test
	void importBigEurope(final FxRobot robot)
			throws IOException, InterruptedException, ExecutionException, TimeoutException {
		// GIVEN

		// WHEN
		doImportBigEurope(robot, new DominationMapImporter(), TestHelper.bigeuropePath());
		toggleSortOnBigEurope(robot);
		searchOnBigEurope(robot);

		final Path savePath = Files.createTempFile(getClass().getSimpleName(), ".css");
		saveBigEurope(robot, savePath);
		Files.deleteIfExists(savePath);
		// THEN

	}

	private void searchOnBigEurope(FxRobot robot) {
		// GIVEN

		// WHEN
		robot.clickOn("#searchField").sleep(SHORT_PAUSE);
		robot.write("si").sleep(400);

		// THEN
		List<TerritorySkin> list = model.getMap().territories().stream().filter(ts -> ts.getId().startsWith("si"))
				.collect(Collectors.toList());
		FxAssert.verifyThat("#territoriesList", ListViewMatchers.hasItems(list.size()));

		// WHEN
		robot.clickOn("#searchField").sleep(SHORT_PAUSE);
		robot.type(KeyCode.BACK_SPACE).type(KeyCode.BACK_SPACE).write("ah").sleep(400);

		// THEN
		list = model.getMap().territories().stream().filter(ts -> ts.getId().startsWith("ah"))
				.collect(Collectors.toList());
		FxAssert.verifyThat("#territoriesList", ListViewMatchers.hasItems(list.size()));

		// WHEN
		robot.clickOn("#clearSearchBtn");

		// THEN
		FxAssert.verifyThat("#searchField", TextInputControlMatchers.hasText(""));
		robot.sleep(400);
		FxAssert.verifyThat("#territoriesList", ListViewMatchers.hasItems(model.getMap().territories().size()));

	}

	private void saveBigEurope(FxRobot robot, Path savePath)
			throws InterruptedException, ExecutionException, TimeoutException, IOException {
		// GIVEN
//		Mockito.when(io.saveMap(model.getMap(), savePath)).thenReturn(model.getMap());

		// TODO Use the menu
		final CompletableFuture<MapSkin> future = mapEditor.saveMap(model.getMap(), savePath).toFuture();

		future.get(5, TimeUnit.SECONDS);

		// THEN
//		Mockito.verify(io).saveMap(model.getMap(), savePath);
		Assertions.assertThat(Files.exists(savePath)).isTrue();
	}

	private void toggleSortOnBigEurope(final FxRobot robot) {
		// GIVEN
		robot.clickOn("#territoriesPane");
		Assertions.assertThat(model.getSortDirection()).isEqualTo(SortDirection.DESCENDING);

		// WHEN
//		robot.clickOn("#toggleSortBtn");

		// THEN
		final List<TerritorySkin> territories = model.getMap().territories().stream()
				.sorted(Comparator.comparing(TerritorySkin::getId)).collect(Collectors.toList());

		final ListView<TerritorySkin> listView = robot.lookup("#territoriesList").queryListView();
		Assertions.assertThat(listView.getItems().get(0)).isEqualTo(territories.get(0));

		// WHEN
		robot.clickOn("#toggleSortBtn").sleep(SHORT_PAUSE);

		// THEN
		Assertions.assertThat(model.getSortDirection()).isEqualTo(SortDirection.ASCENDING);
		Assertions.assertThat(listView.getItems().get(0)).isEqualTo(territories.get(territories.size() - 1));

		// WHEN
		robot.clickOn("#toggleSortBtn");
		Assertions.assertThat(model.getSortDirection()).isEqualTo(SortDirection.DESCENDING);

	}

	private void doImportBigEurope(final FxRobot robot, final MapImporter importer, final Path path)
			throws IOException, InterruptedException, ExecutionException, TimeoutException {
		// TODO find menu
		final CompletableFuture<MapSkin> future = new CompletableFuture<>();

		final Mono<MapSkin> mono = mapEditor.importWith(importer, path);
		mono.subscribe(future::complete, future::completeExceptionally);
		robot.sleep(SHORT_PAUSE);

		// THEN

		final MapSkin skin = future.get(5, TimeUnit.SECONDS);
		robot.sleep(400);
		
		FxAssert.verifyThat(".glasspane", NodeMatchers.isInvisible());

		Assertions.assertThat(model.getMap()).isEqualTo(skin);
		Assertions.assertThat(model.currentPathProperty().get()).isNull();

		FxAssert.verifyThat("#mapIdField", NodeMatchers.isFocused());
		FxAssert.verifyThat("#mapIdField", TextInputControlMatchers.hasText("bigeurope"));

	}

	@Test
	void newMapAndAddTerritories(final FxRobot robot) throws InterruptedException, MalformedURLException, URISyntaxException {
		// GIVEN
//		Thread.sleep(4000);
		// WHEN
		chooseNewMap(robot);
		enterMapInfo(robot);

		for (TerritoryInfo ti : TERRITORIES) {
			createNewTerritory(robot, ti.id, ti.name, ti.x, ti.y);
		}

		selectTerritories(robot, TERRITORIES[1], TERRITORIES[0]);
	}

	private void selectTerritories(final FxRobot robot, final TerritoryInfo... infos) {
		for (TerritoryInfo info : infos) {
			robot.clickOn(info.id()).sleep(SHORT_PAUSE);
			Assertions.assertThat(model.selectedTerritoryProperty().get().getId()).isEqualTo(info.id());
		}
	}

	private void createNewTerritory(final FxRobot robot, final String id, final String name, final int x, final int y) {
		robot.clickOn("#newTerritoryBtn").sleep(SHORT_PAUSE);

		FxAssert.verifyThat("#territoriesList", NodeMatchers.isVisible());

		FxAssert.verifyThat("#territoryIdField", NodeMatchers.isFocused());
		robot.write(id).press(KeyCode.TAB).sleep(SHORT_PAUSE);

		robot.doubleClickOn("#territoryNameField");
		FxAssert.verifyThat("#territoryNameField", NodeMatchers.isFocused());
		robot.write(name).press(KeyCode.TAB).sleep(SHORT_PAUSE);

		robot.doubleClickOn("#territoryXField");
		FxAssert.verifyThat("#territoryXField", NodeMatchers.isFocused());
		robot.write(String.valueOf(x)).press(KeyCode.TAB).sleep(SHORT_PAUSE);

		robot.doubleClickOn("#territoryYField");
		FxAssert.verifyThat("#territoryYField", NodeMatchers.isFocused());
		robot.write(String.valueOf(y)).press(KeyCode.TAB).sleep(SHORT_PAUSE);

		final TerritorySkin skin = model.selectedTerritoryProperty().get();
		Assertions.assertThat(skin.getId()).isEqualTo(id);
//		Assertions.assertThat(skin.getName()).isEqualTo(name);
//		Assertions.assertThat(skin.getIndicatorX()).isEqualTo(x);
//		Assertions.assertThat(skin.getIndicatorY()).isEqualTo(y);
	}

	private void enterMapInfo(FxRobot robot) throws InterruptedException, URISyntaxException, MalformedURLException {
		FxAssert.verifyThat("#mapIdField", NodeMatchers.isFocused());
		robot.write("test-map").press(KeyCode.TAB).sleep(SHORT_PAUSE);

		FxAssert.verifyThat("#mapNameField", NodeMatchers.isFocused());
		robot.write("Test Map").sleep(SHORT_PAUSE).press(KeyCode.TAB).sleep(SHORT_PAUSE);

		robot.doubleClickOn("#mapAuthorField").sleep(SHORT_PAUSE);
		FxAssert.verifyThat("#mapAuthorField", NodeMatchers.isFocused());
		robot.write("CRK").press(KeyCode.TAB).sleep(SHORT_PAUSE);

		robot.doubleClickOn("#mapDescriptionField");
		FxAssert.verifyThat("#mapDescriptionField", NodeMatchers.isFocused());
		robot.write("This is a description").press(KeyCode.TAB).sleep(SHORT_PAUSE);

		final File backgroundImage = Paths.get(TestHelper.class.getResource("bigeurope_pic.jpg").toURI()).toFile();
		Mockito.when(fileDialogs.showOpenFileDialog(Mockito.any(), Mockito.any()))
			.thenReturn(Mono.just(backgroundImage));		
		
		robot.clickOn("#chooseBackgroundImageBtn");
		
		Assertions.assertThat(model.getMap().getBackgroundImageUrl()).isEqualTo(backgroundImage.toURI().toURL());
		FxAssert.verifyThat("#mapBackgroundField", TextInputControlMatchers.hasText(backgroundImage.toURI().toURL().toExternalForm()));
		
		final File reliefImage = Paths.get(TestHelper.class.getResource("bigeurope_map.gif").toURI()).toFile();
		Mockito.when(fileDialogs.showOpenFileDialog(Mockito.any(), Mockito.any()))
			.thenReturn(Mono.just(reliefImage));		
		robot.clickOn("#chooseReliefImageBtn");
		
		Assertions.assertThat(model.getMap().getPicImageUrl()).isEqualTo(reliefImage.toURI().toURL());
		FxAssert.verifyThat("#mapReliefField", TextInputControlMatchers.hasText(reliefImage.toURI().toURL().toExternalForm()));
		
		// TODO validate all data

	}

	private void chooseNewMap(FxRobot robot) {
		final MapSkin skin = model.getMap();

//		robot.moveTo("#navIconBtn").clickOn().press(MouseButton.PRIMARY).sleep(500);
//		robot.clickOn("#newMapBtn").sleep(SHORT_PAUSE);

		robot.interact(() -> {
			mapEditor.newMap();
		});

		// THEN
		Assertions.assertThat(model.getMap()).isNotEqualTo(skin);

		FxAssert.verifyThat("#mapIdField", NodeMatchers.isVisible());
		FxAssert.verifyThat("#mapIdField", NodeMatchers.isFocused());

	}

}
