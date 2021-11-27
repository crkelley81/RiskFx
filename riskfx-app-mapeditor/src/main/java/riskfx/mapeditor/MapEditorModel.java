package riskfx.mapeditor;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import riskfx.mapeditor.model.MapSkin;
import riskfx.ui.TerritorySkin;

/**
 * 
 * @author christopher
 *
 */
public class MapEditorModel {

	public static enum SortDirection {
		DESCENDING() {
			@Override
			public <T> Comparator<T> modify(Comparator<T> orig) {
				return orig;
			}
		}, ASCENDING() {
			@Override
			public <T> Comparator<T> modify(Comparator<T> orig) {
				return orig.reversed();
			}
		};
		
		public abstract <T> Comparator<T> modify(final Comparator<T> orig);
	}
	
	private final ReadOnlyObjectWrapper<MapSkin> mapProperty = new ReadOnlyObjectWrapper<>(this, "map");
	public final ReadOnlyObjectProperty<MapSkin> mapProperty()	{	return this.mapProperty.getReadOnlyProperty(); }
	public final MapSkin getMap()								{	return this.mapProperty.get(); }
	
	private final ObjectProperty<Path> currentPathProperty = new SimpleObjectProperty<>(this, "currentPath");
	public final ReadOnlyObjectProperty<Path> currentPathProperty() {	return this.currentPathProperty; }
	public final Path getCurrentPath()							{	return this.currentPathProperty.get(); }
	public final Optional<Path> getCurrentPathOpt()				{	return Optional.of(getCurrentPath()); }
	
	private final ReadOnlyObjectWrapper<TerritorySkin<?>> selectedTerritoryProperty = new ReadOnlyObjectWrapper<TerritorySkin<?>>(this, "selectedTerritory");
	public final ReadOnlyObjectProperty<TerritorySkin<?>> selectedTerritoryProperty() {
		return this.selectedTerritoryProperty.getReadOnlyProperty();
	}
	
	private final ReadOnlyObjectWrapper<SortDirection> sortDirectionProperty = new ReadOnlyObjectWrapper<>(this, "sortDirection", SortDirection.DESCENDING);
	public final SortDirection getSortDirection() 				{	return this.sortDirectionProperty.getValue(); }
	public final void setSortDirection(final SortDirection sort) {
		this.sortDirectionProperty.set(Optional.ofNullable(sort).orElse(SortDirection.DESCENDING));
	}
	private final Binding<Comparator<TerritorySkin<?>>> sortProperty = Bindings.createObjectBinding(() -> {
		final SortDirection dir = sortDirectionProperty.get();
		return dir.modify(Comparator.comparing(TerritorySkin::getId));
	}, sortDirectionProperty);

	private final ListProperty<TerritorySkin<?>> territoriesProperty = new SimpleListProperty<>(this, "territories", FXCollections.observableArrayList());
	private final FilteredList<TerritorySkin<?>> filtered = territoriesProperty.filtered(ts -> true);
	private final SortedList<TerritorySkin<?>> sorted = filtered.sorted();

	@Inject public MapEditorModel() {
		sorted.comparatorProperty().bind(sortProperty);
	}
	
	public final void toggleSort() {
		setSortDirection( Objects.equals(getSortDirection(), SortDirection.ASCENDING) ? SortDirection.DESCENDING : SortDirection.ASCENDING);
		
	}
	
	public final void edit(final MapSkin map) {
		edit(map, Optional.empty());
	}
	
	public final void edit(final MapSkin map, final Optional<Path> path) {
		mapProperty.set(map);
		territoriesProperty.set(map.territories());
		path.ifPresentOrElse(currentPathProperty::set, () -> currentPathProperty.set(null));
	}
	
	public final void select(final TerritorySkin<?> skin) {
		if (! Objects.equals(selectedTerritoryProperty.get(), skin) ) {
			this.selectedTerritoryProperty.set(skin);
		}
	}
	
	public final ObservableList<TerritorySkin<?>> territories() {
		return this.sorted;
	}
	public riskfx.ui.TerritorySkin<?> newTerritory(final String id, final String name) {
		return getMap().newTerritorySkin(id, name);
	}
	public final void search(final String search) {
		filtered.setPredicate(ts -> {
			return ts.idProperty().get().contains(search);
		});
	}
}