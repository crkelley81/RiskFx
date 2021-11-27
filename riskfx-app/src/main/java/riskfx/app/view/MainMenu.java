package riskfx.app.view;

import java.util.Objects;

import javax.inject.Inject;

import appfx.util.FxmlView;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import riskfx.util.ui.UiContext;

public final class MainMenu extends FxmlView {

	private final UiContext<Node> context;
	private final Navigation navigation;

	@Inject
	public MainMenu(final UiContext<Node> context, final Navigation navigation) {
		this.context = Objects.requireNonNull(context);
		this.navigation = Objects.requireNonNull(navigation);
		this.inflateView();
	}

	@FXML
	public void initialize() {
		assert exitBtn != null;
//		exitBtn.setOnAction(evt -> requestQuit());
	}

	@FXML
	public final void requestQuit() {
//		System.err.println("Quit");

		context.requestExit();
	}

	@FXML
	public final void openSettings() {
		navigation.openSettings();
	}

	@FXML
	public final void openMapEditor() {
		navigation.openMapEditor();
	}

	@FXML
	public final void newGame() {
		navigation.openNewGame();
	}

	@FXML
	public final void joinGame() {
		navigation.openJoinGame();
	}

	@FXML
	public final void loadGame() {
		navigation.openLoadGame();
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
