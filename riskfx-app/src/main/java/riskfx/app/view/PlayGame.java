package riskfx.app.view;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.reactivestreams.Publisher;

import appfx.util.FxmlView;
import appfx.window.GlassPane2;
import freetimelabs.io.reactorfx.flux.FxFlux;
import freetimelabs.io.reactorfx.schedulers.FxSchedulers;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import riskfx.engine.game.GameEvent;
import riskfx.engine.game.GamePlayer;
import riskfx.engine.game.GameState;
import riskfx.engine.display.Display;
import riskfx.engine.game.Game;
import riskfx.engine.model.Card;
import riskfx.engine.model.Hand;
import riskfx.engine.model.Moves;
import riskfx.engine.model.Moves.Attack;
import riskfx.engine.model.Moves.Claim;
import riskfx.engine.model.Moves.Fortify;
import riskfx.engine.model.Moves.Place;
import riskfx.engine.model.Moves.Reinforce;
import riskfx.engine.model.Moves.TurnInCards;
import riskfx.engine.model.Player;
import riskfx.engine.model.Territory;
import riskfx.ui.BoardView;
import riskfx.ui.HandView;
import riskfx.ui.History;
import riskfx.ui.TerritorySkin;
import riskfx.util.ui.UiContext;

public class PlayGame extends FxmlView implements GamePlayer {

	private UiContext<Node> context;
	private PlayGamePlayer delegate;
	/* package for testing */ final Notifier notifier = new Notifier();
	
	private List<AnchorPane> playerIndicators;
	private GlassPane2 theGlassPane;
	private HandView handView;
	
	@Inject
	public PlayGame(final UiContext<Node> context) {
		this.context = Objects.requireNonNull(context);
		this.setId("playGame");
		this.inflateView();
	}

	public void play(final Game game, final String skinUrl) {
		board.getStylesheets().add(skinUrl);
		board.items().addAll(game.map().territories());
//		game.state().map().setNotifier(notifier);

		updatePlayerIndicators(game.turnOrder());
		
//		game.allPlayers().stream().forEach(p -> game.assignGamePlayer(p, this));
		game.setNotifier(notifier, Platform::runLater);
		
		final History history = new History();
		historyView.setRoot(history.asTreeItem());
		game.events()
		.doOnNext(evt -> {System.err.println("Event: " + evt.describe());})
			.publishOn(FxSchedulers.fxThread())
			.subscribe(history::update);
		
		messageField.setText("Start");
		actionBtn.setText("Start");
		FxFlux.from(actionBtn, ActionEvent.ACTION).take(1).subscribe(evt -> game.start(Duration.ofMillis(500)));
	}

	@Override
	public Flux<Claim> claim(GameState state, Player player, int numTerritories) {
		return delegate.claim(state, player, numTerritories).index().doOnSubscribe(s -> {
			messageField.setText("Claim %s territories".formatted(numTerritories));
		}).doOnNext(m -> {
			messageField.setText("Claim %s territories".formatted(numTerritories - m.getT1()));
		}).doAfterTerminate(() -> {
			messageField.setText("");
		}).map(t -> t.getT2()).subscribeOn(FxSchedulers.fxThread());
	}

	@Override
	public Flux<Place> place(GameState state, Player player, int troopsToDeploy) {
		return delegate.place(state, player, troopsToDeploy).index().doOnSubscribe(s -> {
			messageField.setText("Place %s armies".formatted(troopsToDeploy));
		}).doOnNext(tuple -> {
			messageField.setText("Place %s armies".formatted(troopsToDeploy - tuple.getT1()));
		}).doAfterTerminate(() -> {
			messageField.setText("");
		}).map(Tuple2::getT2).subscribeOn(FxSchedulers.fxThread());
	}

	@Override
	public Mono<Attack> attack(GameState state, Player player) {
		return delegate.attack(state, player).doOnSubscribe(s -> {
			messageField.setText("Make an attack");
			actionBtn.setText("Done");
		}).doAfterTerminate(() -> {
			messageField.setText("");
		}).subscribeOn(FxSchedulers.fxThread());
	}

	private Publisher<?> stepDone() {
		return FxFlux.from(actionBtn, ActionEvent.ACTION).take(1);
	}

	@Override
	public Flux<Fortify> fortify(GameState state, Player player) {
		return delegate.fortify(state, player).doOnSubscribe(s -> {
			messageField.setText("Fortify your position");
			actionBtn.setText("Done");
		}).doAfterTerminate(() -> {

		}).subscribeOn(FxSchedulers.fxThread());
	}

	@Override
	public Flux<Reinforce> reinforce(final GameState state, final Player player, final Territory from,
			final Territory to) {
		return delegate.reinforce(state, player, from, to).doOnSubscribe(s -> {
			messageField.setText("Reinforce %s from %s".formatted(to.getId(), from.getId()));
			actionBtn.setText("Done");
			board.select(from);
			board.select(to);
		}).doAfterTerminate(() -> {
			messageField.setText("");
			board.deselect(from);
			board.deselect(to);
		}).subscribeOn(FxSchedulers.fxThread());
	}

	@Override
	public Mono<Void> beginTurn(GameState state, Player player, int turnNumber) {
		return delegate.beginTurn(state, player, turnNumber).doOnSubscribe(s -> {
			messageField.setText("Begin turn %s for %s".formatted(turnNumber, player.getDisplayName()));
			actionBtn.setText("Go");
		}).doOnNext(evt -> {
//					System.err.println("Click!");
		}).doAfterTerminate(() -> {
//					System.err.println("Done!");
		}).then().subscribeOn(FxSchedulers.fxThread());
	}
	

	@Override public Mono<TurnInCards> turnInCards(GameState state, Player player, boolean required) {
		
		return showCards(player, required)
//				.doOnNext(cards -> System.err.println("Cards: " + cards))
			.map(cards -> Moves.turnInCards(player, cards))
//			.doOnNext(move -> System.err.println("Move: " + move))
//			.doOnError(ex -> System.err.println("Exception: " + ex))
			.subscribeOn(FxSchedulers.fxThread())
			.publishOn(FxSchedulers.fxThread());
	}
	
	private Mono<Collection<Card>> showCards(final Player player, final boolean required) {
		return Mono.create(sink -> {
			final Consumer<Collection<Card>> onTurnIn = cards -> {
				handView.setOnTurnInCards(null);
				handView.onHideProperty().set(null);
				theGlassPane.hide();
				
				sink.success(cards);
			};
			final EventHandler<ActionEvent> onHide = evt -> {
				handView.setOnTurnInCards(null);
				handView.onHideProperty().set(null);
				theGlassPane.hide();
				
				sink.success();
			};
			
			handView.setOnTurnInCards(onTurnIn);
			handView.onHideProperty().set(onHide);
			handView.update(player.getHand(), required);
			
			theGlassPane.show();
		});
	}

	@FXML public void showCards() {
		theGlassPane.show();
	}
	
	@FXML
	public void initialize() {
		initializeBoard();
		initializeHistory();
		initializePlayerIndicators();
		initializeGlassPane();
	}
	
	private void initializeGlassPane() {
		scrim.setVisible(false);
		scrim.setManaged(false);
		glassPane.setVisible(false);
		glassPane.setManaged(false);
		
		theGlassPane = new GlassPane2();
		getChildren().add(theGlassPane);
		theGlassPane.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
		
		handView = new HandView();
		theGlassPane.setContent(handView);
//		handView.onHideProperty().set(evt -> theGlassPane.hide());
	}

	private void initializePlayerIndicators() {
		playerIndicators = Arrays.asList(playerIndicator1, playerIndicator2, playerIndicator3, playerIndicator4, playerIndicator5, playerIndicator6);
		updatePlayerIndicators(Collections.emptyList());
	}

	private void updatePlayerIndicators(final List<Player> players) {
		playerIndicators.forEach(a -> {
			a.setVisible(false);
			a.setManaged(false);
		});
		
		for (int i = 0; i < players.size(); i++) {
			final AnchorPane pane = playerIndicators.get(i);
			pane.setVisible(true);
			pane.setManaged(true);
			final Label label = (Label) pane.lookup(".player-indicator-label");
			label.setText(players.get(i).getDisplayName());
			pane.setStyle("-fx-background-color: " + players.get(i).getId() + ";");
		}
	}

	private void initializeBoard() {
//		assert false;
		boardPane.getChildren().setAll(board);
		board.setTerritoryViewFactory(t -> {
			final TerritorySkin<Territory> ts = TerritorySkin.forTerritory(t);
			Tooltip tip = new Tooltip();
			FxFlux.from(ts.hoveredProperty()).filter(p -> p)
					.filter(x -> Optional.of(ts).map(Node::getScene).map(Scene::getWindow).isPresent()).subscribe(o -> {
						tip.setText("%s [%s, %s, %s, %s]".formatted(t.getId(), t.getOwner().getId(), t.getArmies(),
								ts.backgroundColorProperty().get(), ts.getStyleClass()));
						tip.show(ts, 0, 0);
					});

			FxFlux.from(ts.hoveredProperty()).filter(p -> !p)
					.filter(x -> Optional.of(ts).map(Node::getScene).map(Scene::getWindow).isPresent()).subscribe(o -> {
						tip.hide();
					});
			return ts;
		});

		delegate = new PlayGamePlayer(board, FxFlux.from(actionBtn, ActionEvent.ACTION));
		
		FxFlux.from(visibleProperty())
		.filter(visible -> visible)
		.take(1)
		.delayElements(Duration.ofSeconds(1))
		.publishOn(FxSchedulers.fxThread())
		.subscribe(v -> {
			System.err.println("I'm visible!!");
			System.err.println("Pane: " + boardPane.getLayoutBounds());
			System.err.println("Board: " + board.getLayoutBounds());
			board.bounds();
		});	
	}
	
	private void initializeHistory() {
		historyView.setCellFactory(tv -> new HistoryTreeCell());
		historyView.setVisible(false);
		historyView.setManaged(false);
	}
	
	class Notifier implements Display {

		
		@Override
		public void onBeginTurn(Player player, int playerIdx, long turnNumber) {
			playerIndicators.forEach(ap -> ap.getStyleClass().remove("player-indicator-current"));
			playerIndicators.get(playerIdx).getStyleClass().add("player-indicator-current");
		}

		@Override
		public void onStart() {
			// TODO Auto-generated method stub
			
		}
		
	}

	private BoardView<Territory, TerritorySkin<Territory>> board = new BoardView<>();

	@FXML
	private Button actionBtn;
	@FXML private Button cardsBtn;
	
	@FXML
	private StackPane boardPane;
	@FXML
	private Label messageField;
	@FXML
	private AnchorPane playerIndicator1;
	@FXML
	private AnchorPane playerIndicator2;
	@FXML
	private AnchorPane playerIndicator3;
	@FXML
	private AnchorPane playerIndicator4;
	@FXML
	private AnchorPane playerIndicator5;
	@FXML
	private AnchorPane playerIndicator6;
	
	@FXML
	private TreeView<GameEvent> historyView;
	
	@FXML private Label deployPhase;
	@FXML private Label attackPhase;
	@FXML private Label reinforcePhase;
	@FXML private Region scrim;
	@FXML private StackPane glassPane;

}
