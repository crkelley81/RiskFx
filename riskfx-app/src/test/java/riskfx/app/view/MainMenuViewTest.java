package riskfx.app.view;

import java.util.Arrays;
import java.util.List;

import javax.inject.Singleton;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import appfx.ui.UiContext;
import javafx.scene.Scene;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class MainMenuViewTest {

//	private DaggerContext di = new GuiceContext(this, () -> provideModules());
	
	private UiContext context = Mockito.mock(UiContext.class);
	private MainMenu presenter;
	
	@Start public void setup(final Stage stage) {
		
		presenter = new MainMenu(context, null, null);
		presenter.inflateView();
		
		final Scene scene = new Scene(presenter);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}
	
	@AfterEach public void cleanup() {
//		di.dispose();
//		Injector.forgetAll();
//	
		
	}
	
	@Test
	void test(final FxRobot robot) {
		// GIVEN
		// WHEN 
		robot.clickOn("#exitBtn").sleep(100);
		
		// THEN
		Mockito.verify(context).requestExit();
	}

	private List<com.google.inject.Module> provideModules() {
		final com.google.inject.Module module = new AbstractModule() {
			
			protected void configure() {}
			@Provides @Singleton UiContext providesUiContext() {
				return context;
			}
		};
		return Arrays.asList(module);
		
	}
}
