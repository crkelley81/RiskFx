package riskfx.app.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import appfx.ui.UiContext;

class MainMenuPresenterTest {
	private MainMenu presenter;
	private UiContext context;
	
	@BeforeEach public void setup() {
		context = Mockito.mock(UiContext.class);
//		presenter = new MainMenu(context);
	}
	
	@Test
	void test() {
		// GIVEN 
		// WHEN
		presenter.requestQuit();
		
		// THEN
		Mockito.verify(context).requestExit();
	}

	@Test void settings() {
		// GIVEN 
		// WHEN 
		presenter.openSettings();
		
		// THEN 
		Mockito.verify(context).switchView(Mockito.any(SettingsView.class));
	}
}
