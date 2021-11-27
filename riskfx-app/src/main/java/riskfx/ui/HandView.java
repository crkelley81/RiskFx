package riskfx.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import appfx.util.FxmlView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import riskfx.engine.model.Card;
import riskfx.engine.model.Hand;

public final class HandView extends FxmlView {
	private final ObjectProperty<EventHandler<ActionEvent>> onHideProperty = new SimpleObjectProperty<>(this, "onHide");
	
	public final ObjectProperty<EventHandler<ActionEvent>> onHideProperty() {	return this.onHideProperty; }
	public final Optional<EventHandler<ActionEvent>> getOnHideOpt()			{	return Optional.ofNullable(this.onHideProperty.get()); }
	
	private Consumer<Collection<Card>> onTurnInCards;
	public final void setOnTurnInCards(final Consumer<Collection<Card>> action) {
		this.onTurnInCards = Optional.ofNullable(action).orElse( c -> {});
	}
	
	private final Map<Card, CardView> cardToCardViews = new HashMap<>();
	private final Set<Card> selectedCards = new HashSet<>();

	public HandView() {
		this.getStyleClass().add("hand-view");
		this.inflateView();
	}
	
	public final void clear() {
		this.cardsView.getChildren().clear();
		this.cardToCardViews.clear();
		this.selectedCards.clear();
	}
	
	public final void update(final Hand hand, final boolean mustTurnInCards) {
		clear();
	
		for (Card c : hand.cards()) {
			final CardView cv = cardToCardViews.computeIfAbsent(c, this::createCardView);
			cardsView.getChildren().add(cv);
		}
		
		hideButton.setDisable(mustTurnInCards);
		turnInButton.setDisable(true);
	}
	
	private CardView createCardView(final Card card) {
		final CardView view = new CardView(card);
		view.setOnMouseClicked(evt -> toggleSelect(card, view));
		return view;
	}
	
	private void toggleSelect(Card card, CardView view) {
		if (view.isSelected()) {
			deselect(card, view);
		} else if (canSelect(card, view)) {
			select(card, view);
		}
	}
	private boolean canSelect(Card card, CardView view) {
		if (selectedCards.size() < 2) {
			return true;
		}
		else if (card.isWildcard()) {
			return true;
		}
		else if (selectedCards.stream().allMatch(c -> card.isSameOrWilcard(c))) {
			return true;
		}
		else {
			return selectedCards.stream().map(Card::type).noneMatch(t -> Objects.equals(t, card.type()))
					&&
					selectedCards.stream().map(Card::type).distinct().count() > 1;
		}
	}
	/* package */ void select(final Card card, final CardView node) {
		node.select();
		selectedCards.add(card);
		updateTurnInCards();
	}
	/* package */ void deselect(final Card card, final CardView node) {
		node.deselect();
		selectedCards.remove(card);
		updateTurnInCards();
	}
	
	private void updateTurnInCards() {
		turnInButton.setDisable(selectedCards.size() < 3);
	}
	
	@FXML public void hide(final ActionEvent evt) {
		getOnHideOpt().ifPresent(handler -> handler.handle(evt));
	}
	
	@FXML public void turnInCards() {
		Optional.ofNullable(onTurnInCards).ifPresent(action -> action.accept(selectedCards));
	}
	
	
	@FXML public final void initialize() {
		turnInButton.setDisable(true);
	}
	
	@FXML
	private Label header;
	@FXML
	private Label instructions;
	@FXML
	private Button turnInButton;
	@FXML
	private Button hideButton;
	@FXML
	private HBox cardsView;
	
}
