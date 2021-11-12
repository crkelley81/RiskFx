package riskfx.mapeditor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import appfx.ui.UiContext;
import appfx.util.UserNotification;
import javafx.stage.Stage;
import reactor.core.publisher.Mono;
import riskfx.mapeditor.io.MapIO;
import riskfx.mapeditor.model.MapSkin;

@ExtendWith(ApplicationExtension.class)
class MapEditorTest {

	private UiContext context = Mockito.mock(UiContext.class);
	private MapIO io = Mockito.mock(MapIO.class);
	private MapEditorModel model = new MapEditorModel();
	
	private MapEditor editor;
	
	@Start public void start(final Stage stage) {
		editor = new MapEditor(context, model, io);
	}
	
	@Test
	void newTerritory(final FxRobot robot) throws InterruptedException {
		// GIVEN 
		editor.newMap();
		Assertions.assertThat(model.territories()).isEmpty();
		
		// WHEN 
		editor.newTerritory();
		Thread.sleep(100);
		
		Assertions.assertThat(model.selectedTerritoryProperty().get()).isNotNull();
		Assertions.assertThat(model.territories()).isNotEmpty();
	}
	
	@Test
	void newMap(final FxRobot robot) throws InterruptedException, ExecutionException {
		// GIVEN
		final CompletableFuture<MapSkin> map = new CompletableFuture<>();
		model.mapProperty().addListener((o, ov, nv) -> map.complete(nv));
		
		// WHEN
		editor.newMap();
		
		// THEN 
		map.join();
		
		Assertions.assertThat(map.get()).isNotNull();
	}

	@Test void openMapSuccess(final FxRobot robot) throws IOException {
		// GIVEN 
		final MapSkin result = new MapSkin();
		final Path path = TestHelper.bigeuropePath();
		Mockito.when(io.openMap(path)).thenReturn(result);
		
		// WHEN 
		editor.openMap(path)
			.block();
		
		// THEN 
		Mockito.verify(context).lockView(Mockito.any(UserNotification.class));
		Mockito.verify(io).openMap(path);
//		Mockito.verify(context).notify(Mockito.any(UserNotification.class));
		Mockito.verify(context).unlockView();
		
		Assertions.assertThat(model.getMap()).isEqualTo(result);
		// TODO check path
		
		
	}
	
	@Test void openMapSuccessIfProvidesPath(final FxRobot robot) throws IOException, InterruptedException {
		// GIVEN 
		final MapSkin result = new MapSkin();
		final Path path = TestHelper.bigeuropePath();
		Mockito.when(context.showOpenFileDialog(Mockito.any())).thenReturn(Mono.just(path.toFile()));
		Mockito.when(io.openMap(path)).thenReturn(result);
		
		// WHEN 
		editor.openMap();
		Thread.sleep(100);
		
		// THEN 
		Mockito.verify(context).lockView(Mockito.any(UserNotification.class));
		Mockito.verify(io).openMap(path);
//		Mockito.verify(context).notify(Mockito.any(UserNotification.class));
		Mockito.verify(context).unlockView();
		
		Assertions.assertThat(model.getMap()).isEqualTo(result);
		// TODO check path
		
		
	}

	@Test void openMapFailed(final FxRobot robot) throws IOException {
		// GIVEN 
		final MapSkin result = new MapSkin();
		final Path path = TestHelper.bigeuropePath();
		final IOException error = new IOException();
		Mockito.when(io.openMap(path)).thenThrow(error);
		
		// WHEN 
		try {
			editor.openMap(path).block();
		}
		catch (Throwable ex) { 
			
		}
		
		// THEN 
		Mockito.verify(context).lockView(Mockito.any());
		Mockito.verify(io).openMap(path);
		Mockito.verify(context).notifyError(Mockito.any(UserNotification.class), Mockito.eq(error));
		Mockito.verify(context).unlockView();
		
//		Assertions.assertThat(model.getMap()).isEqualTo(result);
		// TODO check path
	}
	
	@Test void openMapDoesNothingIfCancelled(final FxRobot robot) {
		// GIVEN
		Mockito.when(context.showOpenFileDialog(Mockito.any())).thenReturn(Mono.empty());
		
		// WHEN 
		editor.openMap();
		
		// THEN 
		Mockito.verify(context).showOpenFileDialog(Mockito.any());
		Mockito.verifyNoInteractions(io);
		Mockito.verifyNoMoreInteractions(context);
	}

	@Test void saveMapSuccess(final FxRobot robot) throws IOException {
		// GIVEN 
		final MapSkin skin = new MapSkin();
		final Path path = TestHelper.bigeuropePath();
		Mockito.when(io.saveMap(skin, path)).thenReturn(skin);
		
		// WHEN
		editor.saveMap(skin, path).block();
		
		// THEN 
		Mockito.verify(context).lockView(Mockito.any());
		Mockito.verify(io).saveMap(skin, path);
		Mockito.verify(context).unlockView();
		
		Assertions.assertThat(model.getMap()).isEqualTo(skin);
		// TODO path
	}
	
	@Test void saveMapFailed(final FxRobot robot) throws IOException {
		// GIVEN 
		final MapSkin skin = new MapSkin();
		final Path path = TestHelper.bigeuropePath();
		final IOException ioe = new IOException();
		Mockito.when(io.saveMap(skin, path)).thenThrow(ioe);
		
		// WHEN
		try {
			editor.saveMap(skin, path).block();
		}
		catch (Throwable t) {}
		
		// THEN 
		Mockito.verify(context).lockView(Mockito.any());
		Mockito.verify(io).saveMap(skin, path);
		Mockito.verify(context).unlockView();
		Mockito.verify(context).notifyError(Mockito.any(UserNotification.class),Mockito.eq(ioe));
		
//		Assertions.assertThat(model.getMap()).isEqualTo(skin);
		// TODO path
	}
	
}
