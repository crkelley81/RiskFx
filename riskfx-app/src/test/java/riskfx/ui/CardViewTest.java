package riskfx.ui;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import riskfx.engine.model.Card;

@ExtendWith(ApplicationExtension.class)
class CardViewTest {
	private CardView cardView;
	
	public void start(Stage stage, CardView cardView) {
		this.cardView = cardView;
		
		final StackPane pane = new StackPane();
		pane.setPrefSize(400, 400);
		pane.setStyle("-fx-background-color: white;");

		pane.getChildren().add(cardView);

		final Scene scene = new Scene(pane);
		stage.setScene(scene);
		stage.show();
	}
	
	
	@Nested @DisplayName("as a wildcard") 
	public class AsWildcard {
		@Start public void start(final Stage stage) {
			final CardView cardView = new CardView();
			cardView.setWildcard(true);
			CardViewTest.this.start(stage, cardView);
		}
		
		@Test public void wildcard(final FxRobot robot) {
			// GIVEN 
			// WHEN
			// THEN 
			Assertions.assertThat(cardView.getTitle()).isEqualTo("WILDCARD");
			Assertions.assertThat(cardView.getCardType()).isEqualTo(Card.Type.WILDCARD);
			FxAssert.verifyThat(".card-title", LabeledMatchers.hasText("WILDCARD"));
//			FxAssert.verifyThat(".card-subtitle", NodeMatchers.isInvisible());
			// TODO how to show image
		}
		
		
	}

	@Nested @DisplayName("as a territory card") 
	public class AsTerritory {
		@Start public void start(final Stage stage) {
			final CardView cardView = new CardView();
			CardViewTest.this.start(stage, cardView);
		}
		
		@Test public void iceland(final FxRobot robot) {
			// GIVEN 
			// WHEN
			robot.interact(() -> {
				cardView.setTitle("ICELAND");
				cardView.setTerritoryOutline(
						"M 66,7 L 67.0,7.0 L 68.0,6.0 L 70.0,6.0 L 72.0,6.0 L 73.0,7.0 L 74.0,7.0 L 76.0,9.0 L 77.0,9.0 L 78.0,8.0 L 78.0,6.0 L 79.0,6.0 L 80.0,5.0 L 82.0,5.0 L 84.0,5.0 L 84.0,6.0 L 86.0,8.0 L 88.0,6.0 L 90.0,6.0 L 91.0,5.0 L 91.0,4.0 L 92.0,3.0 L 94.0,3.0 L 96.0,3.0 L 98.0,3.0 L 100.0,3.0 L 102.0,3.0 L 104.0,3.0 L 106.0,3.0 L 107.0,3.0 L 107.0,4.0 L 106.0,4.0 L 105.0,5.0 L 106.0,6.0 L 108.0,6.0 L 109.0,6.0 L 110.0,5.0 L 112.0,5.0 L 113.0,5.0 L 114.0,6.0 L 115.0,6.0 L 116.0,7.0 L 117.0,7.0 L 118.0,6.0 L 120.0,6.0 L 121.0,6.0 L 123.0,8.0 L 124.0,9.0 L 124.0,11.0 L 124.0,12.0 L 122.0,14.0 L 123.0,15.0 L 125.0,15.0 L 127.0,15.0 L 128.0,16.0 L 128.0,18.0 L 129.0,19.0 L 129.0,20.0 L 130.0,21.0 L 131.0,21.0 L 131.0,23.0 L 131.0,25.0 L 131.0,27.0 L 131.0,29.0 L 130.0,29.0 L 128.0,31.0 L 128.0,32.0 L 126.0,34.0 L 126.0,35.0 L 124.0,35.0 L 123.0,36.0 L 121.0,36.0 L 120.0,37.0 L 120.0,38.0 L 118.0,40.0 L 116.0,40.0 L 115.0,41.0 L 113.0,41.0 L 112.0,42.0 L 111.0,42.0 L 110.0,41.0 L 109.0,42.0 L 107.0,42.0 L 105.0,44.0 L 103.0,44.0 L 102.0,45.0 L 100.0,45.0 L 98.0,45.0 L 96.0,45.0 L 95.0,46.0 L 94.0,46.0 L 93.0,47.0 L 91.0,47.0 L 90.0,48.0 L 89.0,48.0 L 88.0,49.0 L 86.0,49.0 L 85.0,50.0 L 85.0,51.0 L 83.0,51.0 L 82.0,51.0 L 80.0,53.0 L 79.0,53.0 L 78.0,54.0 L 76.0,54.0 L 74.0,54.0 L 72.0,54.0 L 70.0,54.0 L 68.0,54.0 L 66.0,54.0 L 65.0,54.0 L 64.0,53.0 L 62.0,53.0 L 60.0,51.0 L 58.0,51.0 L 56.0,51.0 L 55.0,51.0 L 54.0,52.0 L 54.0,54.0 L 54.0,55.0 L 53.0,55.0 L 51.0,53.0 L 53.0,51.0 L 52.0,50.0 L 51.0,50.0 L 50.0,49.0 L 48.0,49.0 L 46.0,49.0 L 45.0,48.0 L 44.0,48.0 L 43.0,47.0 L 41.0,47.0 L 40.0,47.0 L 39.0,46.0 L 37.0,46.0 L 35.0,46.0 L 33.0,46.0 L 32.0,46.0 L 31.0,45.0 L 30.0,46.0 L 28.0,46.0 L 26.0,46.0 L 25.0,47.0 L 23.0,47.0 L 22.0,47.0 L 22.0,45.0 L 22.0,43.0 L 22.0,42.0 L 24.0,40.0 L 24.0,39.0 L 26.0,37.0 L 26.0,35.0 L 26.0,34.0 L 25.0,33.0 L 23.0,35.0 L 23.0,36.0 L 21.0,36.0 L 19.0,34.0 L 18.0,35.0 L 16.0,35.0 L 15.0,35.0 L 14.0,34.0 L 13.0,34.0 L 12.0,33.0 L 12.0,32.0 L 13.0,31.0 L 15.0,31.0 L 17.0,31.0 L 18.0,30.0 L 17.0,29.0 L 15.0,29.0 L 13.0,29.0 L 12.0,30.0 L 10.0,30.0 L 9.0,31.0 L 7.0,31.0 L 6.0,31.0 L 6.0,29.0 L 6.0,27.0 L 6.0,25.0 L 6.0,24.0 L 8.0,22.0 L 8.0,21.0 L 10.0,21.0 L 12.0,21.0 L 14.0,21.0 L 15.0,21.0 L 16.0,22.0 L 17.0,21.0 L 18.0,21.0 L 20.0,19.0 L 21.0,18.0 L 19.0,16.0 L 17.0,16.0 L 15.0,16.0 L 13.0,16.0 L 11.0,16.0 L 9.0,16.0 L 7.0,16.0 L 6.0,16.0 L 6.0,14.0 L 6.0,12.0 L 6.0,10.0 L 6.0,8.0 L 6.0,7.0 L 8.0,5.0 L 8.0,3.0 L 10.0,3.0 L 12.0,3.0 L 14.0,3.0 L 14.0,4.0 L 15.0,5.0 L 16.0,4.0 L 16.0,3.0 L 18.0,3.0 L 19.0,4.0 L 19.0,5.0 L 20.0,6.0 L 21.0,6.0 L 22.0,7.0 L 21.0,7.0 L 20.0,8.0 L 21.0,9.0 L 22.0,8.0 L 21.0,7.0 L 23.0,7.0 L 24.0,6.0 L 25.0,6.0 L 26.0,7.0 L 28.0,7.0 L 30.0,7.0 L 30.0,8.0 L 31.0,9.0 L 31.0,11.0 L 31.0,12.0 L 29.0,14.0 L 30.0,15.0 L 31.0,15.0 L 32.0,16.0 L 33.0,16.0 L 33.0,17.0 L 34.0,18.0 L 35.0,17.0 L 36.0,17.0 L 37.0,16.0 L 37.0,14.0 L 38.0,13.0 L 38.0,11.0 L 38.0,10.0 L 39.0,9.0 L 40.0,10.0 L 40.0,12.0 L 41.0,13.0 L 42.0,12.0 L 43.0,12.0 L 44.0,11.0 L 46.0,11.0 L 47.0,12.0 L 49.0,12.0 L 50.0,12.0 L 51.0,11.0 L 53.0,11.0 L 55.0,11.0 L 57.0,11.0 L 58.0,11.0 L 59.0,10.0 L 60.0,10.0 L 62.0,8.0 L 64.0,8.0 z");
				cardView.setTerritoryFill(Color.FORESTGREEN);
				cardView.setCardType(Card.Type.INFANTRY);
			});
			
			// THEN 
			Assertions.assertThat(cardView.getTitle()).isEqualTo("ICELAND");
			FxAssert.verifyThat(".card-title", LabeledMatchers.hasText("ICELAND"));
//			FxAssert.verifyThat(".card-subtitle", NodeMatchers.isInvisible());
			// TODO how to show image
			
			robot.sleep(3000);
		}
		
	}
	

}
