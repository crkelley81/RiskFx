package riskfx.ui;


import java.util.List;
import java.util.logging.Logger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Paint;
import riskfx.engine.model.Card;
import riskfx.util.role.Selectable;

public class CardView extends Control implements Selectable {
	private static final Logger LOG = Logger.getLogger(CardView.class.getName());
	
	private static final StyleablePropertyFactory<CardView> FACTORY = new StyleablePropertyFactory<>(
			Control.getClassCssMetaData());
	private static final PseudoClass PSUEDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");
	
	private final ObjectProperty<Card> cardProperty = new SimpleObjectProperty<>(this, "card") {
		@Override protected void invalidated() {
			final Card card = get();
			if (card != null) {
				setId(card.getId());
				switch (card.type()) {
				case WILDCARD:
					setWildcard(true);
					setCardType(Card.Type.WILDCARD);
					setTitle("WILDCARD");
//					setSubtitle("WILDCARD");
					break;
				default: 
					setWildcard(false);
					setCardType(card.type());
					setTitle(card.getDisplayName().toUpperCase());
//					setSubtitle(card.type().name().toUpperCase());
					break;
				}
			}
			else {
				setId(null);
			}
		}
	};
	public final ObjectProperty<Card> cardProperty()		{	return this.cardProperty; }
	public final void setCard(final Card card)				{	this.cardProperty.set(card); }
	// fill
	private final ObjectProperty<Card.Type> cardTypeProperty = new SimpleObjectProperty<>(this, "type");
	public final ObjectProperty<Card.Type> cardTypeProperty()	{ return this.cardTypeProperty; }
	public final Card.Type getCardType()			{	return this.cardTypeProperty.get(); }
	public final void setCardType(final Card.Type p) {	this.cardTypeProperty.set(p); }
	
	private final BooleanProperty wildcardProperty = new SimpleBooleanProperty(this, "wildcard", false) {
		@Override protected void invalidated() {
			boolean wildcard = get();
			if (wildcard) {
				setTitle("WILDCARD");
				setCardType(Card.Type.WILDCARD);
			}
		}
	};
	public final BooleanProperty wildcardProperty()	{	return this.wildcardProperty; }
	public final boolean isWildcard()				{	return this.wildcardProperty.get(); }
	public final void setWildcard(final boolean b)	{	this.wildcardProperty.set(b); }
	
	// title
	private final StringProperty titleProperty = new SimpleStringProperty(this, "title");
	public final StringProperty titleProperty()		{	return this.titleProperty; }
	public final String getTitle()					{	return this.titleProperty.get(); }
	public final void setTitle(final String title)	{	this.titleProperty.set(title); }
	
	// outline
	private final StringProperty outlineProperty = new SimpleStringProperty(this, "outline");
	public final StringProperty territoryOutlineProperty()		{	return this.outlineProperty; }
	public final String getTerritoryOutline()					{	return this.outlineProperty.get(); }
	public final void setTerritoryOutline(final String title)	{	this.outlineProperty.set(title); }
		
	// fill
	private final ObjectProperty<Paint> fillProperty = new SimpleObjectProperty<>(this, "fill");
	public final ObjectProperty<Paint> territoryFillProperty()	{ return this.fillProperty; }
	public final Paint getTerritoryFill()			{	return this.fillProperty.get(); }
	public final void setTerritoryFill(final Paint p) {	this.fillProperty.set(p); }
	
	private final ReadOnlyBooleanWrapper selected = new ReadOnlyBooleanWrapper(this, "selected" ,false) {
		@Override protected void invalidated() {
			CardView.this.pseudoClassStateChanged(PSUEDO_CLASS_SELECTED, get());
		}	
	};
	@Override public final ReadOnlyBooleanProperty selectedProperty() {
		return this.selected.getReadOnlyProperty();
	}
	
	@Override
	public void select() {
		selected.set(true);
	}
	@Override
	public void deselect() {
		selected.set(false);
	}
	
	public CardView() {
		super();
		
		getStyleClass().add("card");
	}
	
	public CardView(Card card) {
		this();
		setCard(card);
	}
	
	@Override
	public final String getUserAgentStylesheet() {
		return getClass().getResource("card.css").toExternalForm();
	}

	@Override
	public final List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
		return FACTORY.getCssMetaData();
	}
	@Override
	protected Skin<?> createDefaultSkin() {
		return new CardViewSkin(this);
	}
	
}
