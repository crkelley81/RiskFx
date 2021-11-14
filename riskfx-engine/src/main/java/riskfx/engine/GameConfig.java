package riskfx.engine;

import java.net.URL;
import java.util.List;
import java.util.Objects;
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
import riskfx.engine.model.Player;

/**
 * 
 * @author christopher
 *
 */
public abstract class GameConfig {

	public static enum Type { 
		DOMINATION, CAPITAL, MISSION;
	}
	
	public static enum CardStyle {
		FIXED, INCREASING, ITALIAN;
	}
	
	private final String id;
	public final String getId()								{	return this.id; }
	
	private final String name;
	public final String getName()							{	return this.name; }
	
	private URL imageUrl;
	public final URL getPreviewImageUrl()					{	return this.imageUrl; }
	
	private final ReadOnlyObjectWrapper<Type> gameTypeProperty = new ReadOnlyObjectWrapper<>(this, "gameType", Type.DOMINATION);
	public final ReadOnlyObjectProperty<Type> gameTypeProperty()	{	return this.gameTypeProperty; }
	public final Type getGameType() 						{	return this.gameTypeProperty().getValue(); }
	public final void setGameType(final Type type) 			{	
		Objects.requireNonNull(type);
		this.gameTypeProperty.setValue(type);
	}
	
	private final ReadOnlyObjectWrapper<CardStyle> cardStyleProperty = new ReadOnlyObjectWrapper<>(this, "cardStyle", CardStyle.FIXED);
	public final ReadOnlyObjectProperty<CardStyle> cardStyleProperty() {	return this.cardStyleProperty.getReadOnlyProperty(); }
	public final CardStyle getCardStyle() 				{	return this.cardStyleProperty.get(); }
	
	public void setCardStyle(final CardStyle cardStyle) {
		Objects.requireNonNull(cardStyle);
		this.cardStyleProperty.set(cardStyle);
	}

	private final BooleanProperty autoAssignProperty = new SimpleBooleanProperty(this, "autoAssign", false);
	public final BooleanProperty autoAssignProperty()		{	return this.autoAssignProperty; }
	public final boolean isAutoAssign()						{	return this.autoAssignProperty.get(); }
	public final void setAutoAssign(final boolean b)		{	this.autoAssignProperty.set(b); }

	private final BooleanProperty autoPlaceProperty = new SimpleBooleanProperty(this, "autoPlace", false);
	public final BooleanProperty autoPlaceProperty()		{	return this.autoPlaceProperty; }
	public final boolean isAutoPlace()						{	return this.autoPlaceProperty.get(); }
	public final void setAutoPlace(final boolean b)			{ 	this.autoPlaceProperty.set(b); }
	
	private final BooleanBinding readyProperty;
	public final BooleanBinding readyProperty()				{	return this.readyProperty; }
	public final boolean isReady()							{	return this.readyProperty.get(); }
	
	
	private final ObservableList<PlayerAssociation> playerAssociations;
	public final ObservableList<PlayerAssociation> playerAssociations()	{	return playerAssociations; }
	
	protected GameConfig(final String id, final String name, final String imageUrl, final Stream<Player> players) {
		this.id = Objects.requireNonNull(id);
		this.name = Objects.requireNonNull(name);
		this.imageUrl = Objects.requireNonNull(getClass().getResource(imageUrl));
		
		final List<PlayerAssociation> pa = players.map(PlayerAssociation::of).collect(Collectors.toList());
		this.playerAssociations = FXCollections.unmodifiableObservableList(FXCollections.observableList(pa));
	
		readyProperty = Bindings.createBooleanBinding(this::computeReady, playerAssociations);
	}
	
	private boolean computeReady() {
		return false;
	}
}
