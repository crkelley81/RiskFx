package riskfx.ui;

import java.util.Objects;
import java.util.function.Predicate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import riskfx.mapeditor.TestHelper;
import riskfx.mapeditor.model.MapSkin;

@ExtendWith(ApplicationExtension.class)
class BoardViewTest {

	private BoardView<TerritorySkin, TerritorySkin> board;

	@Start
	public void start(final Stage stage) {
		board = new BoardView<>();
		board.setPrefSize(800, 600);

		final Scene scene = new Scene(board);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}

	@Test 
	void bigeuropeMapSkin(final FxRobot robot) {
		// GIVEN 
		final MapSkin skin = TestHelper.bigeuropeMapSkin();
		board.setTerritoryViewFactory(t -> (TerritorySkin) t);
		// WHEN 
		robot.interact(() -> {
			board.backgroundImageProperty().bind(skin.backgroundImageProperty());
			board.setItems(skin.territories());
		});
		
		// THEN 
		FxAssert.verifyThat("#backgroundImageView", hasImage(board.getBackgroundImage()));
		
		
		// WHEN 
		robot.moveTo("#si1");
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
		final String url = TestHelper.bigeuropeImageUrl().toExternalForm();

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
//		FxAssert.verifyThat("#backroundImageView", NodeMatchers.isNull());

		// WHEN
		robot.interact(() -> {
			board.setBackgroundImageVisible(true);
		}).sleep(100);

		// THEN
		FxAssert.verifyThat("#backgroundImageView", NodeMatchers.isVisible());

	}

	private static Predicate<ImageView> hasImage(final Image image) {
		return iv -> {
			return Objects.equals(iv.getImage(), image);
		};
	}
}
