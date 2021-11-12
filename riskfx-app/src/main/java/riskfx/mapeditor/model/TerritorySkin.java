package riskfx.mapeditor.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import appfx.role.As;
import appfx.role.CssRenderable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import riskfx.app.util.role.MutableDisplayable;
import riskfx.app.util.role.MutableIdentifiable;

public class TerritorySkin implements MutableIdentifiable, MutableDisplayable, As, CssRenderable {

	private final Collection<Object> lookups;
	
	
	private final StringProperty idProperty = new SimpleStringProperty(this, "id", "untitled");
	public final StringProperty idProperty() 			{	return idProperty; }
	
	private final StringProperty displayNameProperty = new SimpleStringProperty(this, "id", "Untitled");
	public final StringProperty displayNameProperty()	{	return this.displayNameProperty; }

	private final DoubleProperty indicatorXProperty = new SimpleDoubleProperty(this, "indicatorX", 0.0);
	public final DoubleProperty indicatorXProperty()	{	return this.indicatorXProperty; }
	public final double getIndicatorX()					{	return this.indicatorXProperty.get(); }
	public final void setIndicatorX(final double x)		{	this.indicatorXProperty.set(x); }
	
	private final DoubleProperty indicatorYProperty = new SimpleDoubleProperty(this, "indicatorY", 0.0);
	public final DoubleProperty indicatorYProperty()	{	return this.indicatorYProperty; }
	public final double getIndicatorY()					{	return this.indicatorYProperty.get(); }
	public final void setIndicatorY(final double y)		{	this.indicatorYProperty.set(y); }
	
	private final StringProperty backgroundShapeProperty = new SimpleStringProperty(this, "backgroundShape");
	public final StringProperty backgroundShapeProperty() {	return this.backgroundShapeProperty; }
	public final String getBackgroundShape()			{	return this.backgroundShapeProperty.getValue(); }
	public final void setBackgroundShape(final String s) {	this.backgroundShapeProperty.setValue(s); }
	
	public TerritorySkin(final String id, final String name, final Object...lookups) {
		Objects.requireNonNull(id);
		Objects.requireNonNull(name);
		
		setId(id);
		setDisplayName(name);
		
		this.lookups = Arrays.asList(lookups);
	}
	
	@Override
	public void renderCss(StringBuilder buffer) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public final <T> Optional<T> as(final Class<T> clazz) {
		return As.as(clazz, lookups);
	}
}
	
