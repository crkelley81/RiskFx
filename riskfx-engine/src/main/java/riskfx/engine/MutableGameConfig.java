package riskfx.engine;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import riskfx.engine.game.BattleCalculator;
import riskfx.engine.game.GameState;
import riskfx.engine.model.BattleResult;
import riskfx.engine.model.Map;
import riskfx.engine.model.Moves.Attack;
import riskfx.engine.model.Player;

/**
 * 
 * @author christopher
 *
 */
public abstract class MutableGameConfig implements GameConfig {

	
	
	@Override
	public boolean useCapitals() {
		return getGameType().equals(Type.CAPITAL);
	}
	@Override
	public boolean useMissions() {
		return getGameType().equals(Type.MISSION);
	}
	@Override
	public final GameConfig immutableCopy() {
		return new ImmutableGameConfig(this);
	}

	private final String id;
	@Override
	public final String getId()								{	return this.id; }
	
	private final String name;
	@Override
	public final String getName()							{	return this.name; }
	
	private URL imageUrl;
	public final URL getPreviewImageUrl()					{	return this.imageUrl; }
	
	private final ReadOnlyObjectWrapper<Type> gameTypeProperty = new ReadOnlyObjectWrapper<>(this, "gameType", Type.DOMINATION);
	public final ReadOnlyObjectProperty<Type> gameTypeProperty()	{	return this.gameTypeProperty; }
	@Override
	public final Type getGameType() 						{	return this.gameTypeProperty().getValue(); }
	public final void setGameType(final Type type) 			{	
		Objects.requireNonNull(type);
		this.gameTypeProperty.setValue(type);
	}
	
	private final ReadOnlyObjectWrapper<CardStyle> cardStyleProperty = new ReadOnlyObjectWrapper<>(this, "cardStyle", CardStyle.FIXED);
	public final ReadOnlyObjectProperty<CardStyle> cardStyleProperty() {	return this.cardStyleProperty.getReadOnlyProperty(); }
	@Override
	public final CardStyle getCardStyle() 				{	return this.cardStyleProperty.get(); }
	
	public void setCardStyle(final CardStyle cardStyle) {
		Objects.requireNonNull(cardStyle);
		this.cardStyleProperty.set(cardStyle);
	}

	private final BooleanProperty autoAssignProperty = new SimpleBooleanProperty(this, "autoAssign", false);
	public final BooleanProperty autoAssignProperty()		{	return this.autoAssignProperty; }
	@Override
	public final boolean isAutoAssign()						{	return this.autoAssignProperty.get(); }
	public final void setAutoAssign(final boolean b)		{	this.autoAssignProperty.set(b); }

	private final BooleanProperty autoPlaceProperty = new SimpleBooleanProperty(this, "autoPlace", false);
	public final BooleanProperty autoPlaceProperty()		{	return this.autoPlaceProperty; }
	@Override
	public final boolean isAutoPlace()						{	return this.autoPlaceProperty.get(); }
	public final void setAutoPlace(final boolean b)			{ 	this.autoPlaceProperty.set(b); }
	
	private final BooleanBinding readyProperty;
	public final BooleanBinding readyProperty()				{	return this.readyProperty; }
	public final boolean isReady()							{	return this.readyProperty.get(); }
	
	private BattleCalculator battleCalculator = MutableGameConfig::calculateBattle;
	
	public final BattleCalculator getBattleCalculator()		{	return this.battleCalculator; }
	public final void setBattleCalculator(final BattleCalculator calculator)	{ this.battleCalculator = calculator; }
	
	private final ObservableList<PlayerAssociation> playerAssociations;
	public final ObservableList<PlayerAssociation> playerAssociations()	{	return playerAssociations; }
	
	protected MutableGameConfig(final String id, final String name, final String imageUrl, final Stream<Player> players) {
		this.id = Objects.requireNonNull(id);
		this.name = Objects.requireNonNull(name);
		this.imageUrl = Objects.requireNonNull(getClass().getResource(imageUrl));
		
		final List<PlayerAssociation> pa = players.map(PlayerAssociation::of).collect(Collectors.toList());
		this.playerAssociations = FXCollections.unmodifiableObservableList(FXCollections.observableList(pa));
	
		readyProperty = Bindings.createBooleanBinding(this::computeReady, playerAssociations);
	}
	
	private boolean computeReady() {
		return playerAssociations.stream().filter(pa -> !pa.typeProperty().get().isNone()).count() >= 2;
	}
	
	public final GameState createInitialState() {
		final Set<Player> allPlayers = playerAssociations.stream()
				.filter(pa -> ! pa.typeProperty().getValue().isNone())
				.map(pa -> pa.getPlayer())
				.collect(Collectors.toUnmodifiableSet());
		final List<Player> turnOrder = createTurnOrder(allPlayers);
		Objects.requireNonNull(turnOrder);
		
		final Map map = createMap();
		Objects.requireNonNull(map);
		
		GameState state = createInitialState(allPlayers, turnOrder, map);
		Objects.requireNonNull(state);
		
		return state;
	}
	
	private List<Player> createTurnOrder(final Set<Player> allPlayers) {
		final List<Player> players = new ArrayList<>(allPlayers);
		Collections.shuffle(players);
		return Collections.unmodifiableList(players);
	}
	protected abstract GameState createInitialState(final Set<Player> allPlayers, final List<Player> turnOrder, final Map map);
	
	protected  void postInit(GameState state) {
		// Do nothing 
	}
	protected abstract Map createMap();
	

	static BattleResult calculateBattle(final GameState state, final Attack attack) {
		final Random random = new Random();

		final long attackerDice = Math.min(3, attack.territory().getArmies());
		final long defenderDice = Math.min(2, attack.to.getArmies());
		final long compareDice = Math.min(attackerDice, defenderDice);
		final List<Long> attackerResults = random.longs(1, 6).limit(attackerDice).boxed()
				.sorted((a, b) -> -1 * Long.compare(a, b)).collect(Collectors.toUnmodifiableList());
		final List<Long> defenderResults = random.longs(1, 6).limit(defenderDice).boxed()
				.sorted((a, b) -> -1 * Long.compare(a, b)).collect(Collectors.toUnmodifiableList());

		long attackLosses = 0;
		long defenderLosses = 0;

		for (int i = 0; i < compareDice; i++) {
			if (attackerResults.get(0) > defenderResults.get(0)) {
				defenderLosses++;
			} else {
				attackLosses++;
			}
		}

		return new BattleResult(attack, defenderLosses >= attack.to.getArmies(), attackerResults, defenderResults, attackLosses, defenderLosses);
	}
	
}
