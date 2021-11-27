package riskfx.app;

import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Provides;
import javafx.scene.Node;
import riskfx.app.view.MainMenu;
import riskfx.app.view.Navigation;
import riskfx.app.view.NewGame;
import riskfx.util.ui.UiContext;

@dagger.Module
public class RiskFxModule {

	private final UiContext<Node> context;
	
	@Inject public RiskFxModule(final UiContext<Node> context) {
		this.context = Objects.requireNonNull(context);
	}
	
	@Provides public UiContext<Node> providesUiContext() {
		return this.context;
	}
	
	@Provides @Singleton public Optional<MapEditorProvider> provideMapEditorProvider() {
		return MapEditorProvider.findProvider();
	}
	
	@Provides @Singleton public Navigation provideNavigation(final UiContext<Node> uiContext, 
			final Provider<MainMenu> mainMenuProvider,
			final Provider<NewGame> newGameProvider) {
		return new Navigation() {

			@Override
			public void goToMainMenu() {
				uiContext.switchView(mainMenuProvider.get());
			}

			@Override
			public void goToMapEditor() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void openLoadGame() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void openJoinGame() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void openNewGame() {
				uiContext.switchView(newGameProvider.get());
			}

			@Override
			public void openMapEditor() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void openSettings() {
				// TODO Auto-generated method stub
				
			}};
	}
}
