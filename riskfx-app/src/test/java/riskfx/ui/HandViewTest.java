package riskfx.ui;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.GeneralMatchers;
import org.testfx.matcher.base.NodeMatchers;

import javafx.geometry.HorizontalDirection;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import riskfx.engine.games.ClassicRiskGameConfig;
import riskfx.engine.model.Card;
import riskfx.engine.model.Hand;
import riskfx.engine.model.Map;
import riskfx.util.role.Selectable;

@ExtendWith(ApplicationExtension.class)
class HandViewTest {

	private HandView handView;

	private Map map = ClassicRiskGameConfig.classicRiskMap();

	private Card wildcard = Card.wildcard(1);
	private Card infantryEUS = Card.territoryCard(map.lookupTerritory("eastern-united-states"), Card.Type.INFANTRY);
	private Card infantryAlaska = Card.territoryCard(map.lookupTerritory("alaska"), Card.Type.INFANTRY);
	private Card artilleryWUS = Card.territoryCard(map.lookupTerritory("western-united-states"), Card.Type.ARTILLERY);
	private Card cavalryAlberta = Card.territoryCard(map.lookupTerritory("alberta"), Card.Type.CAVALRY);
	
	@Start
	public void start(final Stage stage) {
		handView = new HandView();

		final Scene scene = new Scene(handView);
		stage.setScene(scene);
		stage.show();
	}

	@Test
	void turnInCardsDisabledWhenEmpty() {
		// GIVEN
		// WHEN

		// THEN
		FxAssert.verifyThat("#turnInButton", NodeMatchers.isDisabled());
	}

	@Test
	void hideButtonDisabledWhenMustTurnInCards(final FxRobot robot) {
		// GIVEN
		Hand hand = new Hand();
		robot.interact(() -> {
			handView.update(hand, true);
		});

		// WHEN
		// THEN
		FxAssert.verifyThat("#hideButton", NodeMatchers.isDisabled());
	}

	@Test
	void hideButtonEnableWhenNotMustTurnInCards(final FxRobot robot) {
		// GIVEN
		// WHEN
		// THEN
		FxAssert.verifyThat("#hideButton", NodeMatchers.isEnabled());
	}

	@Test
	void showsCards(final FxRobot robot) {
		// GIVEN
		Hand hand = new Hand();
		hand.issue(wildcard, this.infantryEUS);
		robot.interact(() -> {
			handView.update(hand, false);
		});

		// WHEN
		// THEN
		FxAssert.verifyThat("#hideButton", NodeMatchers.isEnabled());

		FxAssert.verifyThat(robot.lookup(cardViewFor(wildcard)), NodeMatchers.isVisible());
		FxAssert.verifyThat(robot.lookup(cardViewFor(infantryEUS)), NodeMatchers.isVisible());

//		FxAssert.verifyThat(".card-title", LabeledMatchers.hasText("EASTERN UNITED STATES"));
//		FxAssert.verifyThat(".card-title", LabeledMatchers.hasText("WILDCARD"));
	}

	@Test
	void selectAndDeselectSingleCard(final FxRobot robot) {
		// GIVEN
		Hand hand = new Hand();
		hand.issue(wildcard, this.infantryEUS);
		robot.interact(() -> {
			handView.update(hand, false);
		});

		// WHEN
		CardView cardView = robot.lookup(cardViewFor(infantryEUS)).query();
		robot.clickOn(cardView).sleep(10);

		// THEN
		Assertions.assertThat(cardView.isSelected()).isTrue();
		FxAssert.verifyThat("#turnInButton", NodeMatchers.isDisabled());

		// WHEN
		cardView = robot.lookup(cardViewFor(infantryEUS)).query();
		robot.clickOn(cardView).sleep(10);

		Assertions.assertThat(cardView.isSelected()).isFalse();
		FxAssert.verifyThat("#turnInButton", NodeMatchers.isDisabled());
	}

	@Test
	void selectEnablesTurnInButton(final FxRobot robot) {
		// GIVEN
		Hand hand = new Hand();
		hand.issue(wildcard, infantryEUS, infantryAlaska);
		robot.interact(() -> {
			handView.update(hand, false);
		});

		// WHEN
		CardView cardView = robot.lookup(cardViewFor(infantryEUS)).query();
		robot.clickOn(cardView).sleep(10);
		cardView = robot.lookup(cardViewFor(infantryAlaska)).query();
		robot.clickOn(cardView).sleep(10);
		cardView = robot.lookup(cardViewFor(wildcard)).query();
		robot.clickOn(cardView).sleep(10);
		
		// THEN 
		FxAssert.verifyThat(robot.lookup(cardViewFor(wildcard)).queryAs(CardView.class), isSelected());
		FxAssert.verifyThat(robot.lookup(cardViewFor(infantryEUS)).queryAs(CardView.class), isSelected());
		FxAssert.verifyThat(robot.lookup(cardViewFor(infantryAlaska)).queryAs(CardView.class), isSelected());
		FxAssert.verifyThat("#turnInButton", NodeMatchers.isEnabled());
	}

	@Test
	void clickTurnInTriggersTurnInButton(final FxRobot robot) {
		// GIVEN
		Hand hand = new Hand();
		final AtomicReference<Collection<Card>> ref = new AtomicReference<>();
		
		hand.issue(wildcard, infantryEUS, infantryAlaska, artilleryWUS, cavalryAlberta);
		handView.setOnTurnInCards(ref::set);
		
		robot.interact(() -> {
			handView.update(hand, false);
		});

		CardView cvWildcard = robot.lookup(cardViewFor(wildcard)).query();
		CardView cvInfantryEUS = robot.lookup(cardViewFor(infantryEUS)).query();
		CardView cvInfantryAlaska = robot.lookup(cardViewFor(infantryAlaska)).query();
		
		// WHEN
		robot.clickOn(cvInfantryEUS);
		robot.clickOn(cvInfantryAlaska);
		robot.clickOn(cvWildcard);
		
		// THEN 
		FxAssert.verifyThat("#turnInButton", NodeMatchers.isEnabled());
		
		// WHEN 
		robot.clickOn("#turnInButton").sleep(10);
		
		// THEN 
		Assertions.assertThat(ref.get()).isNotNull();
		Assertions.assertThat(ref.get()).contains(wildcard, infantryEUS, infantryAlaska);
	}
	
	@Test
	void selectOnlyValidCombinations(final FxRobot robot) {
		// GIVEN
		Hand hand = new Hand();
		hand.issue(wildcard, infantryEUS, infantryAlaska, artilleryWUS, cavalryAlberta);
		robot.interact(() -> {
			handView.update(hand, true);
		});
		FxAssert.verifyThat("#hideButton", NodeMatchers.isDisabled());

		CardView cvWildcard = robot.lookup(cardViewFor(wildcard)).query();
		CardView cvInfantryEUS = robot.lookup(cardViewFor(infantryEUS)).query();
		CardView cvInfantryAlaska = robot.lookup(cardViewFor(infantryAlaska)).query();
		CardView cvArtilleryWUS = robot.lookup(cardViewFor(artilleryWUS)).query();
		CardView cvCavalryAlberta = robot.lookup(cardViewFor(cavalryAlberta)).query();
		
		// WHEN
		robot.clickOn(cvInfantryEUS);
		robot.clickOn(cvInfantryAlaska);
		robot.clickOn(cvWildcard);
		
		// THEN 
		FxAssert.verifyThat("#turnInButton", NodeMatchers.isEnabled());
		
		// WHEN 
		robot.clickOn(cvWildcard);
		robot.clickOn(cvArtilleryWUS).sleep(10);
		
		// THEN 
		Assertions.assertThat(cvArtilleryWUS.isSelected()).isFalse();
		FxAssert.verifyThat("#turnInButton", NodeMatchers.isDisabled());
		
		// WHEN 
		robot.clickOn(cvInfantryAlaska);
		robot.clickOn(cvArtilleryWUS);
		robot.clickOn(cvWildcard);
		
		// THEN 
		FxAssert.verifyThat("#turnInButton", NodeMatchers.isEnabled());

		// WHEN 
		robot.clickOn(cvWildcard);
		final ScrollPane scrollPane = robot.lookup(".scroll-pane").query();
		robot.interact(() -> {
			scrollPane.requestFocus();
			scrollPane.setHvalue(scrollPane.getHvalue() + 100);
		});
		robot.scroll(10, HorizontalDirection.RIGHT);
		robot.clickOn(cvCavalryAlberta).sleep(10);
		
		// THEN 
		FxAssert.verifyThat("#turnInButton", NodeMatchers.isEnabled());
	}

	
	private static <T extends Selectable> Matcher<Selectable> isSelected() {
		return GeneralMatchers.typeSafeMatcher(Selectable.class, 
				"is selected",
				s -> s.isSelected());
	}

	private static <T extends CardView> Matcher<CardView> cardViewFor(final Card card) {
		return GeneralMatchers.typeSafeMatcher(CardView.class, "card view for " + card.getId(), s -> "",
				c -> Objects.equals(c.cardProperty().get(), card));
	}
}
