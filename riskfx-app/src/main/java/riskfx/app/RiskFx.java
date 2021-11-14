package riskfx.app;

import java.util.Objects;

import javax.inject.Inject;

import dagger.Lazy;
import riskfx.app.view.MainMenu;

public class RiskFx {

	private Lazy<MainMenu> mainMenu;

	@Inject public RiskFx() {
		this.mainMenu = Objects.requireNonNull(mainMenu);
	}
	
	public final MainMenu get() {
		return mainMenu.get();
	}
}
