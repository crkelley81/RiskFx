package riskfx.mapeditor;

import javax.inject.Singleton;

import appfx.ui.UiContext;
import dagger.Provides;
import riskfx.mapeditor.io.MapIO;

@dagger.Module
public class MapEditorModule {

	@Provides @Singleton public MapIO providesMapIO() {
		return MapIO.create();
	}
	
	@Provides @Singleton public MapEditorModel providerMapEditorModel() {
		return new MapEditorModel();
	}
	
	@Provides @Singleton public MapEditor provideMapEditor(final UiContext context, final MapEditorModel model, final MapIO io) {
		final MapEditor editor = new MapEditor(context, model, io);
		editor.inflateView();
		return editor;
	}
}
