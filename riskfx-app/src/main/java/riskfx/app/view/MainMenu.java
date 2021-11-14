package riskfx.app.view;

import java.util.Objects;

import javax.inject.Inject;

import appfx.ui.UiContext;
import appfx.util.FxmlView;
import dagger.Lazy;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import riskfx.mapeditor.MapEditor;

public final class MainMenu extends FxmlView {

	private final UiContext context;

	private Navigation navigation;

	private final Lazy<NewGame> lazyNewGame;

	private final Lazy<MapEditor> lazyMapEditor;

	@Inject public MainMenu(final UiContext context,
			final Lazy<NewGame> lazyNewGame,
			final Lazy<MapEditor> lazyMapEditor) {
		this.context = Objects.requireNonNull(context);
		this.lazyNewGame = Objects.requireNonNull(lazyNewGame);
		this.lazyMapEditor = Objects.requireNonNull(lazyMapEditor);
	}
	
	@FXML public void initialize() {
		assert exitBtn != null;
//		exitBtn.setOnAction(evt -> requestQuit());
	}
	
	@FXML public final void requestQuit() {
//		System.err.println("Quit");
		
		context.requestExit();
	}
	
	@FXML public final void openSettings() {
//		context.switchView(new SettingsView());
	}
	
	@FXML public final void openMapEditor() {
		context.switchView(this.lazyMapEditor.get());
	}

	@FXML public final void newGame() {
		context.switchView(this.lazyNewGame.get());
	}
	
	@FXML public final void joinGame() {
		
	}

	@FXML public final void loadGame() {
		
	}
	
	@FXML
    private Button exitBtn;

    @FXML
    private Button joinGameBtn;

    @FXML
    private Button loadGameBtn;

    @FXML
    private Button mapEditorBtn;

    @FXML
    private Button newGameBtn;

    @FXML
    private AnchorPane root;

    @FXML
    private Button settingsBtn;

    @FXML
    private Text title;
}
