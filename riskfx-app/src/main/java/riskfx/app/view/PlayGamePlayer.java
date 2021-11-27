package riskfx.app.view;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import freetimelabs.io.reactorfx.schedulers.FxSchedulers;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import riskfx.engine.game.GamePlayer;
import riskfx.engine.game.GameState;
import riskfx.engine.model.Moves;
import riskfx.engine.model.Moves.Attack;
import riskfx.engine.model.Moves.Claim;
import riskfx.engine.model.Moves.Fortify;
import riskfx.engine.model.Moves.Place;
import riskfx.engine.model.Moves.Reinforce;
import riskfx.engine.model.Player;
import riskfx.engine.model.Territory;
import riskfx.ui.BoardView;
import riskfx.ui.TerritorySkin;
import riskfx.ui.TerritoryView;
import riskfx.util.Try;

public class PlayGamePlayer implements GamePlayer {
	private final BoardView<Territory, TerritorySkin<Territory>> board;
	private final Flux<ActionEvent> actions;
	
	private Territory lastAttack = null;
	
	public PlayGamePlayer(final BoardView<Territory, TerritorySkin<Territory>> board, final Flux<ActionEvent> actions) {
		this.board = Objects.requireNonNull(board);
		this.actions = Objects.requireNonNull(actions);
	}
	
	@Override
	public Flux<Claim> claim(GameState state, Player player, int numTerritories) {
//		final Flux<Territory> flux = Flux.empty();

		return board.eventsOn(MouseEvent.MOUSE_CLICKED)
				.map(TerritoryView::item)
				.filter(t1 -> t1.isOwnedBy(Player.none()))
				.distinct()
				.map(t -> Moves.claim(t, player))
				.take(numTerritories);
	}

	@Override
	public Flux<Place> place(GameState state, Player player, int troopsToDeploy) {
		return board.eventsOn(MouseEvent.MOUSE_PRESSED)
				.switchMap(t -> {
					return Flux.just(t)
							.concatWith(Flux.interval(Duration.ofMillis(100))
									.map(i -> t)
									.takeUntilOther(board.eventsOn(MouseEvent.MOUSE_RELEASED))
									.publishOn(FxSchedulers.fxThread()));
				})
				.map(TerritoryView::item)
				.filter(t -> t.isOwnedBy(player))
				.index()
				.map(tuple -> Moves.place(tuple.getT2(), player, 1))
				.take(troopsToDeploy)
				.subscribeOn(FxSchedulers.fxThread())
				.publishOn(FxSchedulers.fxThread());
	}

	@Override
	public Mono<Attack> attack(GameState state, Player player) {
		final Flux<Optional<Territory>> fromFlux = board.eventsOn(MouseEvent.MOUSE_CLICKED)
				.map(TerritorySkin::item)
				.filter(t -> t.isOwnedBy(player))
				.map(Optional::of)
				.startWith(Optional.ofNullable(lastAttack))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.doOnNext(t -> System.err.println("T: " + t.getId()))
				.scan(Optional.empty(), (prev, cur) -> {
					if (prev.isEmpty()) {
						if (canMakeAttack(cur, player)) {
							board.select(cur);
							return Optional.of(cur);
						}
						else {
							return Optional.empty();
						}
					}
					else { 
						final Territory p = prev.get();
						if (Objects.equals(p, cur)) {
							board.deselect(cur);
							return Optional.empty();
						}
						else if (canMakeAttack(cur, player)){
							board.deselect(p);
							board.select(cur);
							return Optional.of(cur);
						}
						else {
							return Optional.empty();
						}
					}
				});
		final Flux<Territory> toFlux = board.eventsOn(MouseEvent.MOUSE_CLICKED)
				.map(TerritorySkin::item)
				.filter(t -> ! t.isOwnedBy(player));
		return toFlux.withLatestFrom(fromFlux, (to, from) -> tryAttack(player, from, to))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.next()
				.doOnNext(attack -> {
					this.lastAttack = attack.territory(); 
					System.err.println("Last attack: " + attack.territory().getId());
				})
				.takeUntilOther(actions)	
				.doOnSuccess(attack -> {
					if (attack == null) {
						board.clearSelection();
					}
				})
				.subscribeOn(FxSchedulers.fxThread())
				.publishOn(FxSchedulers.fxThread());
				
	}
	
	private Optional<Attack> tryAttack(final Player player, final Optional<Territory> fromOpt, final Territory to) {
		if (fromOpt.isEmpty()) return Optional.empty();
		
		final Territory from = fromOpt.get();
		if (canMakeAttack(from, player) && canReceiveAttack(to, from, player)) {
			return Optional.of(Moves.attack(player, from, to));
		}
		return Optional.empty();
	}

	@Override
	public Flux<Fortify> fortify(GameState state, Player player) {
		return board.eventsOn(MouseEvent.MOUSE_CLICKED)
				.map(TerritoryView::item)
				.scan(new FortifyState(false, player, null, null, 0), FortifyState::scan)
				.doOnNext(s -> System.err.println(s))
				.filter(FortifyState::emit)
				.map(s -> Moves.fortify(player, s.from, s.to, 1))
				.takeUntilOther(actions)
				.subscribeOn(FxSchedulers.fxThread())
				.publishOn(FxSchedulers.fxThread());
	}

	@Override public Flux<Reinforce> reinforce(final GameState state, final Player player, final Territory from, final Territory to) {
		return board.eventsOn(MouseEvent.MOUSE_CLICKED)
				.map(TerritoryView::item)
				.filter(t -> Objects.equals(from, t) || Objects.equals(to, t))
				.map(t -> {
					if (Objects.equals(from, t)) {
						return Moves.reinforce(player, to, from, 1);
					}
					else {
						return Moves.reinforce(player, from, to, 1);
					}
				})
				.filter(Try::isSuccess)
				.map(Try::get)
				.takeUntilOther(actions)
				.subscribeOn(FxSchedulers.fxThread())
				.publishOn(FxSchedulers.fxThread());
	}

	@Override
	public Mono<Void> beginTurn(GameState state, Player player, int turnNumber) {
		return actions.take(1).subscribeOn(FxSchedulers.fxThread())
				.then();
	}

	private static boolean canStartFortify(Territory t, Player player) {
		return t.isOwnedBy(player) && t.getArmies() > 1 && t.neighborsOwnedBy(player).count() > 0;
	}
	
	private boolean canMakeAttack(final Territory t, final Player p) {
		return t.isOwnedBy(p) && t.getArmies() > 1 && t.neighborsNotOwnedBy(p).count() > 0;
	}

	private boolean canReceiveAttack(final Territory t, final Territory from, final Player p) {
		return t.isNeighborOf(from) && !t.isOwnedBy(p);
	}
	
	private static boolean canReceiveFortify(Territory to, Territory from, Player player) {
		return to.isOwnedBy(player) && from.isNeighborOf(to);
	}

	private class AttackState {
		private final Player player;
		private final Territory from;
		private final Territory to;
		
		public AttackState scan(final Territory other) {
			if ((from == null) && canMakeAttack(other, player)) {
				board.select(other);
				return new AttackState(player, other, null);
			}
			else if ((from != null) && Objects.equals(from, other)) {
				board.deselect(other);
				return new AttackState(player, null, null);
			}
			else if ((from != null) && (other != null) && canReceiveAttack(other, from, player)) {
				return new AttackState(player, from, other);
			}
			else {
				return new AttackState(player, from, to);
			}
		}
		
		public AttackState(Player player, Territory from, Territory to) {
			super();
			this.player = player;
			this.from = from;
			this.to = to;
		}

		public boolean emit() {
			return from != null && to != null;
		}
	}
	
	private class FortifyState {
		private final Player player;
		private final Territory from;
		private final Territory to;
		private final boolean emit;
		private final long limit;
		
		public FortifyState(boolean emit, Player player, Territory from, Territory to, long limit) {
			this.emit = emit;
			this.player = player;
			this.from = from;
			this.to = to;
			this.limit = limit;
		}

		public FortifyState scan(final Territory other) {
			if ((from == null) && canStartFortify(other, player)) {
				board.select(other);
				return new FortifyState(false, player, other, null, other.getArmies() - 1);
			}
			else if ((from != null) && Objects.equals(from, other)) {
				board.deselect(from);
				return new FortifyState(false, player, null, null, -1);
			}
			else if ((from != null) && (other != null) && canReceiveFortify(other, from, player)) {
				return new FortifyState(limit > 0, player, from, other, limit - 1);
			}
			else {
				return new FortifyState(false, player, from, to, limit);
			}
		}
		
		public boolean emit() {
			return emit;
		}
		
		public String toString() {
			return "FortifyState[%s, %s, %s, %s, %s]".formatted(emit, player.getId(), 
					Optional.ofNullable(from).map(Territory::getId).orElse(null),
					Optional.ofNullable(to).map(Territory::getId).orElse(null),
					limit
					);
		}
	}
}
