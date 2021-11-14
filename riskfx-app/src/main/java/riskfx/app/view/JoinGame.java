package riskfx.app.view;

import java.util.Objects;

import javax.inject.Inject;

import appfx.ui.UiContext;
import appfx.util.FxmlView;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.util.converter.IntegerStringConverter;
import riskfx.engine.network.Connection;
import riskfx.engine.network.ConnectionFactory;
import riskfx.engine.network.ConnectionFactory.Auth;

public class JoinGame extends FxmlView {
	private final BooleanProperty connecting = new SimpleBooleanProperty(this, "connecting", false);

	private final UiContext context;
	private final ConnectionFactory connectionFactory;

	private final TextFormatter<Integer> portFormatter = new TextFormatter<>(new IntegerStringConverter());
	
	@Inject public JoinGame(final UiContext context, final ConnectionFactory connectionFactory) {
		this.context = Objects.requireNonNull(context);
		this.connectionFactory = Objects.requireNonNull(connectionFactory);
	}
	
	@FXML public void initialize() {
		connectBtn.disableProperty().bind(connecting);
	
		progressIndicator.visibleProperty().bind(connecting);
		progressIndicator.progressProperty().bind(Bindings.when(connecting).then(-1.0).otherwise(0.0));
	
		portField.setTextFormatter(portFormatter);
		
		passwordField.setDisable(true);
	}
	
	@FXML
    public final void connect() {
		final String name = this.nameField.getText();
		final String host = this.hostField.getText();
		final int port = portFormatter.getValue();
		final ConnectionFactory.Notifier notifier = new ConnectionFactory.Notifier() {
			
			@Override
			public void notifySuccess(Connection capture) {
				endConnecting("");
			}
			
			@Override
			public void notifyFailed(String reason) {
				endConnecting(reason);
			}
			
			@Override
			public void authenticate(final Auth auth) {
				passwordField.setOnAction(evt -> {
					auth.password(passwordField.getText());
					passwordField.setOnAction(null);
					passwordField.setOnKeyPressed(null);
				});
				
				passwordField.setOnKeyPressed(evt -> {
					if (KeyCode.ESCAPE.equals(evt.getCode())) {
						auth.cancel();
						passwordField.setOnAction(null);
						passwordField.setOnKeyPressed(null);
					}
					
				});
				
				passwordField.setDisable(false);
				passwordField.requestFocus();
				passwordField.selectAll();
			}
		};
		
		beginConnecting("Connecting to %s".formatted(host));
		connectionFactory.connect(host, port, name, notifier);
    }

    private void beginConnecting(String formatted) {
		connecting.setValue(true);		
		messageField.setText(formatted);
    }

	protected void endConnecting(String string) {
		connecting.setValue(false);
		
		messageField.setText(string);
		
		passwordField.setText("");
		passwordField.setDisable(true);
		passwordField.setOnAction(null);
		passwordField.setOnKeyPressed(null);
	}

	@FXML
    public final void goBack() {
    	context.goBack();
    }
	
	@FXML
    private Button connectBtn;

    @FXML
    private TextField hostField;

    @FXML
    private Label messageField;

    @FXML
    private TextField nameField;

    @FXML
    private Button navIconBtn;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField portField;

    @FXML
    private ProgressIndicator progressIndicator;

    
}
