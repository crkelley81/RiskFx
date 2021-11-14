package riskfx.app;

import javax.inject.Singleton;

import dagger.Component;
import riskfx.app.view.MainMenu;
import riskfx.mapeditor.MapEditorModule;

@Singleton @Component(modules= { RiskFxModule.class, MapEditorModule.class })
public interface RiskFxApp {
	public MainMenu mainMenu();
}
