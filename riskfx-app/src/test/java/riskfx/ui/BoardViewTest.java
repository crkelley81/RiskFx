package riskfx.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.GeneralMatchers;
import org.testfx.matcher.base.NodeMatchers;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import reactor.core.publisher.Flux;
import riskfx.engine.game.Game;
import riskfx.engine.games.BigEuropeGameConfig;
import riskfx.engine.model.Player;
import riskfx.engine.model.Territory;

@ExtendWith(ApplicationExtension.class)
class BoardViewTest {

	private BoardView<Territory, TerritoryView<Territory>> board;

//	@Start
	public void start(final Stage stage, BoardView<Territory, TerritoryView<Territory>> boardView) {
		this.board = boardView;
		board.setPrefSize(800, 600);

		final Scene scene = new Scene(board);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}

	@Nested
	@DisplayName("with an empty board view")
	public class EmptyBoard {
		@Start
		public void start(final Stage stage) {
			BoardViewTest.this.start(stage, new BoardView<>());
		}

		@Test
		void css(final FxRobot robot) {
			// GIVEN
			// WHEN
			// THEN
			Assertions.assertThat(board.getStyleClass()).contains("board");

			FxAssert.verifyThat("#backgroundImageView", NodeMatchers.anything());
		}

		@Test
		void backgroundImage(final FxRobot robot) {
			// GIVEN
			final String url = ViewFixture.bigeuropeImageUrl().toExternalForm();

			// WHEN
			robot.interact(() -> {
				board.setBackgroundImageUrl(url);
			}).sleep(10);

			// THEN
			Assertions.assertThat(board.getBackgroundImageUrl()).isEqualTo(url);
			Assertions.assertThat(board.getBackgroundImage()).isNotNull();
			Assertions.assertThat(board.getBackgroundImage().getUrl()).isEqualTo(url);
			FxAssert.verifyThat("#backgroundImageView", hasImage(board.getBackgroundImage()));
			FxAssert.verifyThat("#backgroundImageView", NodeMatchers.isVisible());

			// WHEN
			robot.interact(() -> {
				board.setBackgroundImageVisible(false);
			}).sleep(10);

			// THEN
			Assertions.assertThat(robot.lookup("#backgroundImageView").tryQuery().isEmpty()).isFalse();
//			FxAssert.verifyThat("#backroundImageView", NodeMatchers.isNull());

			// WHEN
			robot.interact(() -> {
				board.setBackgroundImageVisible(true);
			}).sleep(100);

			// THEN
			FxAssert.verifyThat("#backgroundImageView", NodeMatchers.isVisible());

		}

	}

	@Nested
	@DisplayName("with Big Europe as normal board view")
	public class AsBoardView {

		private BigEuropeGameConfig gameConfig;
		private Game game;

		private Player playerRed;
		private Player playerBlue;

		private Territory territorySi1;

		@Start
		public void start(final Stage stage) {
			gameConfig = new BigEuropeGameConfig();
			game = Game.from(gameConfig);

			final BoardView<Territory, TerritoryView<Territory>> boardView = new BoardView<>();
			boardView.setTerritoryViewFactory(t -> {
				final TerritorySkin<Territory> tv = new TerritorySkin<Territory>(t.getId(), t.getDisplayName());
				tv.setUserData(t);
				return tv;
			});
			boardView.items().addAll(game.map().territories());

			final String skinUrl = ViewFixture.bigeuropeSkinUrl();
			boardView.getStylesheets().add(skinUrl);
			boardView.setBackgroundImageUrl(getClass().getResource("bigeurope_pic.jpg").toExternalForm());

			playerRed = game.lookupPlayer("red");
			playerBlue = game.lookupPlayer("blue");

//			territorySi1 = game.

			BoardViewTest.this.start(stage, boardView);
//			stage.setMaximized(false);
//			stage.setFullScreen(false);
//			stage.setFullScreen(true);
		}

		@Test
		public void hasBackgroundImage(final FxRobot robot) {
			// GIVEN
			// WHEN
			robot.sleep(1000);

			// THEN
			FxAssert.verifyThat("#backgroundImageView",
					hasImageFrom(getClass().getResource("bigeurope_pic.jpg").toExternalForm()));
		}

		@Test
		public void hadTerritoryViews(final FxRobot robot) {
			// GIVEN
			// WHEN
			// THEN
			game.map().territories().forEach(t -> {
				FxAssert.verifyThat("#" + t.getId(), NodeMatchers.isVisible());
			});

		}

		@Test
		public void s1HasCorrectShape(final FxRobot robot) {
			final TerritorySkin<?> s1 = robot.lookup("#s1").query();
			Assertions.assertThat(s1.getBackgroundShape()).isEqualTo(
					"M 66,7 L 67.0,7.0 L 68.0,6.0 L 70.0,6.0 L 72.0,6.0 L 73.0,7.0 L 74.0,7.0 L 76.0,9.0 L 77.0,9.0 L 78.0,8.0 L 78.0,6.0 L 79.0,6.0 L 80.0,5.0 L 82.0,5.0 L 84.0,5.0 L 84.0,6.0 L 86.0,8.0 L 88.0,6.0 L 90.0,6.0 L 91.0,5.0 L 91.0,4.0 L 92.0,3.0 L 94.0,3.0 L 96.0,3.0 L 98.0,3.0 L 100.0,3.0 L 102.0,3.0 L 104.0,3.0 L 106.0,3.0 L 107.0,3.0 L 107.0,4.0 L 106.0,4.0 L 105.0,5.0 L 106.0,6.0 L 108.0,6.0 L 109.0,6.0 L 110.0,5.0 L 112.0,5.0 L 113.0,5.0 L 114.0,6.0 L 115.0,6.0 L 116.0,7.0 L 117.0,7.0 L 118.0,6.0 L 120.0,6.0 L 121.0,6.0 L 123.0,8.0 L 124.0,9.0 L 124.0,11.0 L 124.0,12.0 L 122.0,14.0 L 123.0,15.0 L 125.0,15.0 L 127.0,15.0 L 128.0,16.0 L 128.0,18.0 L 129.0,19.0 L 129.0,20.0 L 130.0,21.0 L 131.0,21.0 L 131.0,23.0 L 131.0,25.0 L 131.0,27.0 L 131.0,29.0 L 130.0,29.0 L 128.0,31.0 L 128.0,32.0 L 126.0,34.0 L 126.0,35.0 L 124.0,35.0 L 123.0,36.0 L 121.0,36.0 L 120.0,37.0 L 120.0,38.0 L 118.0,40.0 L 116.0,40.0 L 115.0,41.0 L 113.0,41.0 L 112.0,42.0 L 111.0,42.0 L 110.0,41.0 L 109.0,42.0 L 107.0,42.0 L 105.0,44.0 L 103.0,44.0 L 102.0,45.0 L 100.0,45.0 L 98.0,45.0 L 96.0,45.0 L 95.0,46.0 L 94.0,46.0 L 93.0,47.0 L 91.0,47.0 L 90.0,48.0 L 89.0,48.0 L 88.0,49.0 L 86.0,49.0 L 85.0,50.0 L 85.0,51.0 L 83.0,51.0 L 82.0,51.0 L 80.0,53.0 L 79.0,53.0 L 78.0,54.0 L 76.0,54.0 L 74.0,54.0 L 72.0,54.0 L 70.0,54.0 L 68.0,54.0 L 66.0,54.0 L 65.0,54.0 L 64.0,53.0 L 62.0,53.0 L 60.0,51.0 L 58.0,51.0 L 56.0,51.0 L 55.0,51.0 L 54.0,52.0 L 54.0,54.0 L 54.0,55.0 L 53.0,55.0 L 51.0,53.0 L 53.0,51.0 L 52.0,50.0 L 51.0,50.0 L 50.0,49.0 L 48.0,49.0 L 46.0,49.0 L 45.0,48.0 L 44.0,48.0 L 43.0,47.0 L 41.0,47.0 L 40.0,47.0 L 39.0,46.0 L 37.0,46.0 L 35.0,46.0 L 33.0,46.0 L 32.0,46.0 L 31.0,45.0 L 30.0,46.0 L 28.0,46.0 L 26.0,46.0 L 25.0,47.0 L 23.0,47.0 L 22.0,47.0 L 22.0,45.0 L 22.0,43.0 L 22.0,42.0 L 24.0,40.0 L 24.0,39.0 L 26.0,37.0 L 26.0,35.0 L 26.0,34.0 L 25.0,33.0 L 23.0,35.0 L 23.0,36.0 L 21.0,36.0 L 19.0,34.0 L 18.0,35.0 L 16.0,35.0 L 15.0,35.0 L 14.0,34.0 L 13.0,34.0 L 12.0,33.0 L 12.0,32.0 L 13.0,31.0 L 15.0,31.0 L 17.0,31.0 L 18.0,30.0 L 17.0,29.0 L 15.0,29.0 L 13.0,29.0 L 12.0,30.0 L 10.0,30.0 L 9.0,31.0 L 7.0,31.0 L 6.0,31.0 L 6.0,29.0 L 6.0,27.0 L 6.0,25.0 L 6.0,24.0 L 8.0,22.0 L 8.0,21.0 L 10.0,21.0 L 12.0,21.0 L 14.0,21.0 L 15.0,21.0 L 16.0,22.0 L 17.0,21.0 L 18.0,21.0 L 20.0,19.0 L 21.0,18.0 L 19.0,16.0 L 17.0,16.0 L 15.0,16.0 L 13.0,16.0 L 11.0,16.0 L 9.0,16.0 L 7.0,16.0 L 6.0,16.0 L 6.0,14.0 L 6.0,12.0 L 6.0,10.0 L 6.0,8.0 L 6.0,7.0 L 8.0,5.0 L 8.0,3.0 L 10.0,3.0 L 12.0,3.0 L 14.0,3.0 L 14.0,4.0 L 15.0,5.0 L 16.0,4.0 L 16.0,3.0 L 18.0,3.0 L 19.0,4.0 L 19.0,5.0 L 20.0,6.0 L 21.0,6.0 L 22.0,7.0 L 21.0,7.0 L 20.0,8.0 L 21.0,9.0 L 22.0,8.0 L 21.0,7.0 L 23.0,7.0 L 24.0,6.0 L 25.0,6.0 L 26.0,7.0 L 28.0,7.0 L 30.0,7.0 L 30.0,8.0 L 31.0,9.0 L 31.0,11.0 L 31.0,12.0 L 29.0,14.0 L 30.0,15.0 L 31.0,15.0 L 32.0,16.0 L 33.0,16.0 L 33.0,17.0 L 34.0,18.0 L 35.0,17.0 L 36.0,17.0 L 37.0,16.0 L 37.0,14.0 L 38.0,13.0 L 38.0,11.0 L 38.0,10.0 L 39.0,9.0 L 40.0,10.0 L 40.0,12.0 L 41.0,13.0 L 42.0,12.0 L 43.0,12.0 L 44.0,11.0 L 46.0,11.0 L 47.0,12.0 L 49.0,12.0 L 50.0,12.0 L 51.0,11.0 L 53.0,11.0 L 55.0,11.0 L 57.0,11.0 L 58.0,11.0 L 59.0,10.0 L 60.0,10.0 L 62.0,8.0 L 64.0,8.0 z");
		}

		@Test
		public void highlightsSi1OnHover(final FxRobot robot) throws InterruptedException {
			// GIVEN
			// WHEN
			FxAssert.verifyThat("#si1", NodeMatchers.isVisible());

			robot.moveTo("#si1").sleep(10).sleep(2000);
			// THEN
			FxAssert.verifyThat("#si1", isHovered());

		}
		
		@Test public void eventsOn(final FxRobot robot) {
			// GIVEN 
			final Flux<TerritoryView<Territory>> flux = board.eventsOn(MouseEvent.MOUSE_CLICKED);
			final List<TerritoryView<Territory>> territories = new ArrayList<>();
			flux.subscribe(territories::add);
			
			// WHEN 
			robot.moveTo("#si1").moveBy(0,1).clickOn().sleep(10);
			
			// THEN 
			Assertions.assertThat(territories.get(0)).matches(tv -> tv.getId().equals("si1"));
		}

	}
//	@Test 
//	void bigeuropeMapSkin(final FxRobot robot) {
//		// GIVEN 
//		final MapSkin skin = TestHelper.bigeuropeMapSkin();
//		board.setTerritoryViewFactory(t -> (TerritorySkin) t);
//		// WHEN 
//		robot.interact(() -> {
//			board.backgroundImageProperty().bind(skin.backgroundImageProperty());
//			board.setItems(skin.territories());
//		});
//		
//		// THEN 
//		FxAssert.verifyThat("#backgroundImageView", hasImage(board.getBackgroundImage()));
//		
//		
//		// WHEN 
//		robot.moveTo("#si1");
//	}

	private static Predicate<ImageView> hasImage(final Image image) {
		return iv -> {
			return Objects.equals(iv.getImage(), image);
		};
	}

	public Matcher<Node> isHovered() {
		return GeneralMatchers.typeSafeMatcher(Node.class, " is hovered",
				n -> (n.isHover()) ? "hovered" : "not hovered", n -> n.isHover());
	}

	public Matcher<ImageView> hasImageFrom(final String url) {
		String descriptionText = "has image from \"" + url + "\"";
		return GeneralMatchers.typeSafeMatcher(ImageView.class, descriptionText,
				imageView -> imageView.getImage().getUrl(),
				imageView -> Objects.equals(imageView.getImage().getUrl(), url));
	}
}
