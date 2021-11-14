package riskfx.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;

import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import riskfx.mapeditor.TestHelper;

@ExtendWith(ApplicationExtension.class)
class TerritorySkinUiTest {


	private TerritorySkin<?> view;
	private Scene scene;

	@Start
	public final void start(final Stage stage) {

		view = new TerritorySkin<>("untitled", "Untitled");

		Pane pane = new Pane();
		pane.setMinHeight(600);
		pane.setMinWidth(800);
		pane.setStyle(
				"-fx-background-color: white; -fx-border-color: gray; -fx-border-width: .25px; -fx-padding: 20px;");
		pane.getChildren().add(view);

		scene = new Scene(pane);
		stage.setScene(scene);
		stage.show();

		Thread.currentThread().setUncaughtExceptionHandler(this::uncaughtException);
	}

	private void uncaughtException(final Thread thread, final Throwable ex) {
		System.err.println("Uncaught exception on thread " + thread + ": " + ex.getLocalizedMessage());
		ex.printStackTrace();
	}

	@Test
	void cssStructure(final FxRobot robot) {
		// GIVEN

		// WHEN

		// THEN
		FxAssert.verifyThat(".territory", NodeMatchers.isNotNull());
		FxAssert.verifyThat(".territory-background", NodeMatchers.isNotNull());
		FxAssert.verifyThat(".territory-indicator", NodeMatchers.isNotNull());
		FxAssert.verifyThat(".territory-indicator-text", NodeMatchers.isNotNull());
	}

	@Test
	void indicator(final FxRobot robot) {
		// GIVEN

		// WHEN
		robot.interact(() -> {
			view.setIndicatorText("10");
		});

		// THEN
		FxAssert.verifyThat(".territory-indicator-text", (Text t) -> "10".equals(t.getText()));
	}


	@Test
	void select(final FxRobot robot) {
		// GIVEN
		final List<TerritoryView.Event> events = new ArrayList<>();
		scene.addEventHandler(TerritoryView.Event.SELECTION, events::add);

		// WHEN
		robot.interact(() -> {
			view.select();
		});

		// THEN
		Assertions.assertThat(view.getPseudoClassStates()).contains(PseudoClass.getPseudoClass("selected"));
		Assertions.assertThat(events).hasSize(1);
		Assertions.assertThat(events.get(0))
				.matches(evt -> Objects.equals(TerritoryView.Event.SELECTED, evt.getEventType()));

		// WHEN
		robot.interact(() -> {
			view.deselect();
		}).sleep(100);

		// THEN
		Assertions.assertThat(view.getPseudoClassStates()).doesNotContain(PseudoClass.getPseudoClass("selected"));
		Assertions.assertThat(events).hasSize(2);
		Assertions.assertThat(events.get(1))
				.matches(evt -> Objects.equals(TerritoryView.Event.DESELECTED, evt.getEventType()));

	}

	@Test
	public void bigeuropeStylesheet(final FxRobot robot) {
		// GIVEN
		scene.getStylesheets().add(TestHelper.bigeuropeSkinUrl().toExternalForm());

		// WHEN
		robot.interact(() -> {
			view.setId("s1");
			view.applyCss();
		});

		// THEN
		Assertions.assertThat(view.getId()).isEqualTo("s1");
		Assertions.assertThat(view.getBackgroundShape()).isEqualTo(
				"M 66,7 L 67.0,7.0 L 68.0,6.0 L 70.0,6.0 L 72.0,6.0 L 73.0,7.0 L 74.0,7.0 L 76.0,9.0 L 77.0,9.0 L 78.0,8.0 L 78.0,6.0 L 79.0,6.0 L 80.0,5.0 L 82.0,5.0 L 84.0,5.0 L 84.0,6.0 L 86.0,8.0 L 88.0,6.0 L 90.0,6.0 L 91.0,5.0 L 91.0,4.0 L 92.0,3.0 L 94.0,3.0 L 96.0,3.0 L 98.0,3.0 L 100.0,3.0 L 102.0,3.0 L 104.0,3.0 L 106.0,3.0 L 107.0,3.0 L 107.0,4.0 L 106.0,4.0 L 105.0,5.0 L 106.0,6.0 L 108.0,6.0 L 109.0,6.0 L 110.0,5.0 L 112.0,5.0 L 113.0,5.0 L 114.0,6.0 L 115.0,6.0 L 116.0,7.0 L 117.0,7.0 L 118.0,6.0 L 120.0,6.0 L 121.0,6.0 L 123.0,8.0 L 124.0,9.0 L 124.0,11.0 L 124.0,12.0 L 122.0,14.0 L 123.0,15.0 L 125.0,15.0 L 127.0,15.0 L 128.0,16.0 L 128.0,18.0 L 129.0,19.0 L 129.0,20.0 L 130.0,21.0 L 131.0,21.0 L 131.0,23.0 L 131.0,25.0 L 131.0,27.0 L 131.0,29.0 L 130.0,29.0 L 128.0,31.0 L 128.0,32.0 L 126.0,34.0 L 126.0,35.0 L 124.0,35.0 L 123.0,36.0 L 121.0,36.0 L 120.0,37.0 L 120.0,38.0 L 118.0,40.0 L 116.0,40.0 L 115.0,41.0 L 113.0,41.0 L 112.0,42.0 L 111.0,42.0 L 110.0,41.0 L 109.0,42.0 L 107.0,42.0 L 105.0,44.0 L 103.0,44.0 L 102.0,45.0 L 100.0,45.0 L 98.0,45.0 L 96.0,45.0 L 95.0,46.0 L 94.0,46.0 L 93.0,47.0 L 91.0,47.0 L 90.0,48.0 L 89.0,48.0 L 88.0,49.0 L 86.0,49.0 L 85.0,50.0 L 85.0,51.0 L 83.0,51.0 L 82.0,51.0 L 80.0,53.0 L 79.0,53.0 L 78.0,54.0 L 76.0,54.0 L 74.0,54.0 L 72.0,54.0 L 70.0,54.0 L 68.0,54.0 L 66.0,54.0 L 65.0,54.0 L 64.0,53.0 L 62.0,53.0 L 60.0,51.0 L 58.0,51.0 L 56.0,51.0 L 55.0,51.0 L 54.0,52.0 L 54.0,54.0 L 54.0,55.0 L 53.0,55.0 L 51.0,53.0 L 53.0,51.0 L 52.0,50.0 L 51.0,50.0 L 50.0,49.0 L 48.0,49.0 L 46.0,49.0 L 45.0,48.0 L 44.0,48.0 L 43.0,47.0 L 41.0,47.0 L 40.0,47.0 L 39.0,46.0 L 37.0,46.0 L 35.0,46.0 L 33.0,46.0 L 32.0,46.0 L 31.0,45.0 L 30.0,46.0 L 28.0,46.0 L 26.0,46.0 L 25.0,47.0 L 23.0,47.0 L 22.0,47.0 L 22.0,45.0 L 22.0,43.0 L 22.0,42.0 L 24.0,40.0 L 24.0,39.0 L 26.0,37.0 L 26.0,35.0 L 26.0,34.0 L 25.0,33.0 L 23.0,35.0 L 23.0,36.0 L 21.0,36.0 L 19.0,34.0 L 18.0,35.0 L 16.0,35.0 L 15.0,35.0 L 14.0,34.0 L 13.0,34.0 L 12.0,33.0 L 12.0,32.0 L 13.0,31.0 L 15.0,31.0 L 17.0,31.0 L 18.0,30.0 L 17.0,29.0 L 15.0,29.0 L 13.0,29.0 L 12.0,30.0 L 10.0,30.0 L 9.0,31.0 L 7.0,31.0 L 6.0,31.0 L 6.0,29.0 L 6.0,27.0 L 6.0,25.0 L 6.0,24.0 L 8.0,22.0 L 8.0,21.0 L 10.0,21.0 L 12.0,21.0 L 14.0,21.0 L 15.0,21.0 L 16.0,22.0 L 17.0,21.0 L 18.0,21.0 L 20.0,19.0 L 21.0,18.0 L 19.0,16.0 L 17.0,16.0 L 15.0,16.0 L 13.0,16.0 L 11.0,16.0 L 9.0,16.0 L 7.0,16.0 L 6.0,16.0 L 6.0,14.0 L 6.0,12.0 L 6.0,10.0 L 6.0,8.0 L 6.0,7.0 L 8.0,5.0 L 8.0,3.0 L 10.0,3.0 L 12.0,3.0 L 14.0,3.0 L 14.0,4.0 L 15.0,5.0 L 16.0,4.0 L 16.0,3.0 L 18.0,3.0 L 19.0,4.0 L 19.0,5.0 L 20.0,6.0 L 21.0,6.0 L 22.0,7.0 L 21.0,7.0 L 20.0,8.0 L 21.0,9.0 L 22.0,8.0 L 21.0,7.0 L 23.0,7.0 L 24.0,6.0 L 25.0,6.0 L 26.0,7.0 L 28.0,7.0 L 30.0,7.0 L 30.0,8.0 L 31.0,9.0 L 31.0,11.0 L 31.0,12.0 L 29.0,14.0 L 30.0,15.0 L 31.0,15.0 L 32.0,16.0 L 33.0,16.0 L 33.0,17.0 L 34.0,18.0 L 35.0,17.0 L 36.0,17.0 L 37.0,16.0 L 37.0,14.0 L 38.0,13.0 L 38.0,11.0 L 38.0,10.0 L 39.0,9.0 L 40.0,10.0 L 40.0,12.0 L 41.0,13.0 L 42.0,12.0 L 43.0,12.0 L 44.0,11.0 L 46.0,11.0 L 47.0,12.0 L 49.0,12.0 L 50.0,12.0 L 51.0,11.0 L 53.0,11.0 L 55.0,11.0 L 57.0,11.0 L 58.0,11.0 L 59.0,10.0 L 60.0,10.0 L 62.0,8.0 L 64.0,8.0 z");
	}

	@Test
	public void cssProgrammatic(final FxRobot robot) {
		// GIVEN
		final String svg = "M 66,7 L 67.0,7.0 L 68.0,6.0 L 70.0,6.0 L 72.0,6.0 L 73.0,7.0 L 74.0,7.0 L 76.0,9.0 L 77.0,9.0 L 78.0,8.0 L 78.0,6.0 L 79.0,6.0 L 80.0,5.0 L 82.0,5.0 L 84.0,5.0 L 84.0,6.0 L 86.0,8.0 L 88.0,6.0 L 90.0,6.0 L 91.0,5.0 L 91.0,4.0 L 92.0,3.0 L 94.0,3.0 L 96.0,3.0 L 98.0,3.0 L 100.0,3.0 L 102.0,3.0 L 104.0,3.0 L 106.0,3.0 L 107.0,3.0 L 107.0,4.0 L 106.0,4.0 L 105.0,5.0 L 106.0,6.0 L 108.0,6.0 L 109.0,6.0 L 110.0,5.0 L 112.0,5.0 L 113.0,5.0 L 114.0,6.0 L 115.0,6.0 L 116.0,7.0 L 117.0,7.0 L 118.0,6.0 L 120.0,6.0 L 121.0,6.0 L 123.0,8.0 L 124.0,9.0 L 124.0,11.0 L 124.0,12.0 L 122.0,14.0 L 123.0,15.0 L 125.0,15.0 L 127.0,15.0 L 128.0,16.0 L 128.0,18.0 L 129.0,19.0 L 129.0,20.0 L 130.0,21.0 L 131.0,21.0 L 131.0,23.0 L 131.0,25.0 L 131.0,27.0 L 131.0,29.0 L 130.0,29.0 L 128.0,31.0 L 128.0,32.0 L 126.0,34.0 L 126.0,35.0 L 124.0,35.0 L 123.0,36.0 L 121.0,36.0 L 120.0,37.0 L 120.0,38.0 L 118.0,40.0 L 116.0,40.0 L 115.0,41.0 L 113.0,41.0 L 112.0,42.0 L 111.0,42.0 L 110.0,41.0 L 109.0,42.0 L 107.0,42.0 L 105.0,44.0 L 103.0,44.0 L 102.0,45.0 L 100.0,45.0 L 98.0,45.0 L 96.0,45.0 L 95.0,46.0 L 94.0,46.0 L 93.0,47.0 L 91.0,47.0 L 90.0,48.0 L 89.0,48.0 L 88.0,49.0 L 86.0,49.0 L 85.0,50.0 L 85.0,51.0 L 83.0,51.0 L 82.0,51.0 L 80.0,53.0 L 79.0,53.0 L 78.0,54.0 L 76.0,54.0 L 74.0,54.0 L 72.0,54.0 L 70.0,54.0 L 68.0,54.0 L 66.0,54.0 L 65.0,54.0 L 64.0,53.0 L 62.0,53.0 L 60.0,51.0 L 58.0,51.0 L 56.0,51.0 L 55.0,51.0 L 54.0,52.0 L 54.0,54.0 L 54.0,55.0 L 53.0,55.0 L 51.0,53.0 L 53.0,51.0 L 52.0,50.0 L 51.0,50.0 L 50.0,49.0 L 48.0,49.0 L 46.0,49.0 L 45.0,48.0 L 44.0,48.0 L 43.0,47.0 L 41.0,47.0 L 40.0,47.0 L 39.0,46.0 L 37.0,46.0 L 35.0,46.0 L 33.0,46.0 L 32.0,46.0 L 31.0,45.0 L 30.0,46.0 L 28.0,46.0 L 26.0,46.0 L 25.0,47.0 L 23.0,47.0 L 22.0,47.0 L 22.0,45.0 L 22.0,43.0 L 22.0,42.0 L 24.0,40.0 L 24.0,39.0 L 26.0,37.0 L 26.0,35.0 L 26.0,34.0 L 25.0,33.0 L 23.0,35.0 L 23.0,36.0 L 21.0,36.0 L 19.0,34.0 L 18.0,35.0 L 16.0,35.0 L 15.0,35.0 L 14.0,34.0 L 13.0,34.0 L 12.0,33.0 L 12.0,32.0 L 13.0,31.0 L 15.0,31.0 L 17.0,31.0 L 18.0,30.0 L 17.0,29.0 L 15.0,29.0 L 13.0,29.0 L 12.0,30.0 L 10.0,30.0 L 9.0,31.0 L 7.0,31.0 L 6.0,31.0 L 6.0,29.0 L 6.0,27.0 L 6.0,25.0 L 6.0,24.0 L 8.0,22.0 L 8.0,21.0 L 10.0,21.0 L 12.0,21.0 L 14.0,21.0 L 15.0,21.0 L 16.0,22.0 L 17.0,21.0 L 18.0,21.0 L 20.0,19.0 L 21.0,18.0 L 19.0,16.0 L 17.0,16.0 L 15.0,16.0 L 13.0,16.0 L 11.0,16.0 L 9.0,16.0 L 7.0,16.0 L 6.0,16.0 L 6.0,14.0 L 6.0,12.0 L 6.0,10.0 L 6.0,8.0 L 6.0,7.0 L 8.0,5.0 L 8.0,3.0 L 10.0,3.0 L 12.0,3.0 L 14.0,3.0 L 14.0,4.0 L 15.0,5.0 L 16.0,4.0 L 16.0,3.0 L 18.0,3.0 L 19.0,4.0 L 19.0,5.0 L 20.0,6.0 L 21.0,6.0 L 22.0,7.0 L 21.0,7.0 L 20.0,8.0 L 21.0,9.0 L 22.0,8.0 L 21.0,7.0 L 23.0,7.0 L 24.0,6.0 L 25.0,6.0 L 26.0,7.0 L 28.0,7.0 L 30.0,7.0 L 30.0,8.0 L 31.0,9.0 L 31.0,11.0 L 31.0,12.0 L 29.0,14.0 L 30.0,15.0 L 31.0,15.0 L 32.0,16.0 L 33.0,16.0 L 33.0,17.0 L 34.0,18.0 L 35.0,17.0 L 36.0,17.0 L 37.0,16.0 L 37.0,14.0 L 38.0,13.0 L 38.0,11.0 L 38.0,10.0 L 39.0,9.0 L 40.0,10.0 L 40.0,12.0 L 41.0,13.0 L 42.0,12.0 L 43.0,12.0 L 44.0,11.0 L 46.0,11.0 L 47.0,12.0 L 49.0,12.0 L 50.0,12.0 L 51.0,11.0 L 53.0,11.0 L 55.0,11.0 L 57.0,11.0 L 58.0,11.0 L 59.0,10.0 L 60.0,10.0 L 62.0,8.0 L 64.0,8.0 z";
		
		// WHEN
		robot.interact(() -> {
			view.setId("s1");
			view.setStyle("-territory-shape: \"" + svg + "\";");
		}).sleep(25);

		// THEN
		Assertions.assertThat(view.getId()).isEqualTo("s1");
		Assertions.assertThat(view.getBackgroundShape()).isEqualTo(svg);
		
		robot.sleep(2000);
	}

	@Test
	public void view(final FxRobot robot) {
		// GIVEN
		robot.interact(() -> {
			view.setId("s1");
			view.setDisplayName("S1");
			view.setIndicatorX(447);
			view.setIndicatorY(344);
			view.setIndicatorText("10");
			view.setIndicatorSize(10);

			System.err.println("Background: " + view.getBackgroundShape());
			try {
				view.setBackgroundShape(
						"M 447 328 L 448 329 L 449 330 L 450 330 L 451 331 L 452 331 L 453 332 L 454 332 L 455 333 L 456 333 L 457 334 L 458 335 L 459 335 L 460 335 L 461 336 L 461 337 L 461 338 L 462 339 L 462 340 L 462 341 L 463 342 L 463 343 L 463 344 L 464 345 L 464 346 L 464 347 L 464 348 L 465 349 L 466 350 L 466 351 L 466 352 L 465 353 L 464 354 L 463 355 L 462 355 L 461 355 L 460 356 L 459 357 L 458 358 L 457 358 L 456 359 L 455 360 L 454 360 L 453 360 L 452 359 L 451 358 L 450 357 L 449 356 L 448 356 L 447 355 L 446 354 L 445 354 L 444 353 L 443 353 L 442 352 L 441 351 L 440 351 L 439 350 L 438 350 L 437 350 L 437 349 L 436 348 L 435 347 L 434 347 L 433 346 L 433 345 L 433 344 L 433 343 L 434 342 L 435 341 L 436 340 L 437 339 L 437 338 L 437 337 L 438 336 L 438 335 L 438 334 L 438 333 L 438 332 L 437 331 L 437 330 L 437 329 L 436 328 L 436 327 L 437 327 L 438 327 L 439 326 L 440 326 L 441 325 L 442 325 L 443 326 L 444 327 L 445 327 L 446 328 z");
//				view.setBackgroundShape("M 10 10 L 10 1000 L 1000 1000 L 1000 10 z");
//				view.setBackgroundShape("M 447 328 L 500 328 L 500 428 L 447 428 z");
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
			System.err.println("Background: " + view.getBackgroundShape());

			view.backgroundColorProperty().set(Color.BLUE);
			view.backgroundFadeProperty().set(1.0);

			System.err.println("Faded: " + view.fadedBackgroundColorProperty().get());
		});

		robot.sleep(1000);

		// WHEN
		robot.moveTo("#s1").sleep(10);
//		final Node node = robot.lookup("#s1").query();
//		System.err.println("Layout bounds: local=%s; parent=%s".formatted(node.getBoundsInLocal(), node.getBoundsInParent()));
//		
//		final Node node2 = robot.lookup(".territory-indicator").query();
//		System.err.println("Indicator bounds: local=%s; parent=%s".formatted(node2.getBoundsInLocal(), node2.getBoundsInParent()));

		FxAssert.verifyThat("#s1", NodeMatchers.isVisible());
		FxAssert.verifyThat("#s1", (Node n) -> n.isHover());
		FxAssert.verifyThat("#s1", n -> n.getPseudoClassStates().contains(PseudoClass.getPseudoClass("hover")));

		robot.moveBy(-20, 0);

		FxAssert.verifyThat("#s1", n -> !n.isHover());

		// THEN
		robot.sleep(4000);
	}

}
