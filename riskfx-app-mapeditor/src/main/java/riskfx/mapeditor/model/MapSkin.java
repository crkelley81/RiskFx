package riskfx.mapeditor.model;

import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import riskfx.ui.TerritorySkin;
import riskfx.util.role.As;
import riskfx.util.role.CssRenderable;
import riskfx.util.role.Dirtyable;
import riskfx.util.role.MutableDisplayable;
import riskfx.util.role.MutableIdentifiable;

public class MapSkin implements As, MutableIdentifiable, MutableDisplayable, Dirtyable, CssRenderable {

	private final ObservableList<TerritorySkin<?>> territories = FXCollections.observableArrayList();
	
	private final Collection<Object> lookups;
	
	private final StringProperty idProperty = new SimpleStringProperty(this, "id");
	public final StringProperty idProperty()	{	return this.idProperty; }
	
	private final StringProperty displayNameProperty = new SimpleStringProperty(this, "name");
	public final StringProperty displayNameProperty()	{	return this.displayNameProperty; }
	
	private final StringProperty authorProperty = new SimpleStringProperty(this, "author");
	public final StringProperty authorProperty()	{	return this.authorProperty; }
	public final String getAuthor()				{	return this.authorProperty.getValue(); }
	public final void setAuthor(final String author) {	this.authorProperty.setValue(author); }
	
	private final StringProperty descriptionProperty = new SimpleStringProperty(this, "description");
	public final StringProperty descriptionProperty()	{	return this.descriptionProperty; }
	public final String getDescription()		{	return this.descriptionProperty.getValue(); }
	public final void setDescription(final String d) {	this.descriptionProperty.setValue(d); }

	private final ObjectProperty<URL> backgroundImageUrlProperty = new SimpleObjectProperty<>(this, "backgroundImageUrl");
	public final ObjectProperty<URL> backgroundImageUrlPropery() {
		return this.backgroundImageUrlProperty;
	}
	public final URL getBackgroundImageUrl()		{	return this.backgroundImageUrlProperty.getValue(); }
	public final void setBackgroundImageUrl(final URL url) {	this.backgroundImageUrlProperty.setValue(url); }
	
	private final ObjectProperty<Image> backgroundImageProperty = new SimpleObjectProperty<>(this, "backgroundImage");
	public final ObjectProperty<Image> backgroundImageProperty() {	return this.backgroundImageProperty; }
	public final Image getBackgroundImage()			{	return this.backgroundImageProperty.getValue(); }
	public final void setBackgroundImage(final Image image)	{	this.backgroundImageProperty.setValue(image); }
	
	private final ObjectProperty<URL> picImageUrlProperty = new SimpleObjectProperty<>(this, "picImageUrl");
	public final ObjectProperty<URL> picImageUrlProperty() {
		return this.picImageUrlProperty;
	}
	public final URL getPicImageUrl()		{	return this.picImageUrlProperty.getValue(); }
	public final void setPicImageUrl(final URL url) {	this.picImageUrlProperty.setValue(url); }
	
	private final ObjectProperty<Image> picImageProperty = new SimpleObjectProperty<>(this, "picImage");
	public final ObjectProperty<Image> picImageProperty() {	return this.picImageProperty; }
	public final Image getPicImage()			{	return this.picImageProperty.getValue(); }
	public final void setPicImage(final Image image)	{	this.picImageProperty.setValue(image); }
	
	
	public MapSkin() {
		this("untitled", "Untitled");
	}
	
	public MapSkin(final String id, final String name, final Object... lookups) {
		setId(Objects.requireNonNull(id));
		setDisplayName(Objects.requireNonNull(name));
		this.lookups = Arrays.asList(lookups);
	}
	
	public final ObservableList<TerritorySkin<?>> territories() {
		return FXCollections.unmodifiableObservableList(territories);
	}
	
	public final <T> Optional<T> as(final Class<T> type) {
		return As.as(type, lookups);
	}
	
	public void renderCss(final StringBuilder buffer) {
		final String NEWLINE = "\n";
		buffer.append("/*").append(NEWLINE);
		buffer.append(" * Id: ").append(getId()).append(NEWLINE);
		buffer.append(" * Name: ").append(getDisplayName()).append(NEWLINE);
		buffer.append(" * Author: ").append(Optional.ofNullable(getAuthor()).orElse("")).append(NEWLINE);
		buffer.append(" * Description: ").append(getDescription()).append(NEWLINE);
		buffer.append(" * Last Updated: ").append(Instant.now()).append(NEWLINE);
		buffer.append(" */").append(NEWLINE).append(NEWLINE);
		
		buffer.append(".board {").append(NEWLINE);
		buffer.append("\t").append("-background-image: url(\"%s\");".formatted("")).append(NEWLINE);
		buffer.append("}").append(NEWLINE).append(NEWLINE);
		
		territories.forEach(t -> t.renderCss(buffer));
	}

	public TerritorySkin<?> newTerritorySkin(String id, String name, Object... lookups) {
		final TerritorySkin<?> ts = new TerritorySkin<>(id, name, lookups);
		territories.add(ts);
		return ts;
	}
	
	private String getBackgroundImageFileName() {
		return getBackgroundImageUrl().getFile();
	}
}
