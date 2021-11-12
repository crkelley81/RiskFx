package riskfx.app;

import java.util.Collection;

import com.google.inject.Module;

import appfx.ui.AppFxApplication;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RiskFxApplication extends AppFxApplication {

	public static void main(String[] args) {
		launch(args);
	}
	
	public static interface RiskFxApp {
		
	}
	

	@Override
	protected void initModules(final Collection<Module> modules) {
		// TODO Auto-generated method stub
		super.initModules(modules);
	}

	@Override
	protected void postInit(final Scene scene, final Stage primaryStage) {
		RiskFx riskFx = DaggerRiskFxApp.builder().build();
		riskFx.get();
	}

}
