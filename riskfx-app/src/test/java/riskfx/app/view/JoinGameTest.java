package riskfx.app.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

import appfx.ui.UiContext;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import riskfx.engine.network.ConnectionFactory;
import riskfx.engine.network.ConnectionFactory.Auth;
import riskfx.engine.network.ConnectionFactory.Notifier;

@ExtendWith(ApplicationExtension.class)
class JoinGameTest {

	private UiContext context = Mockito.mock(UiContext.class);
	private ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
	private JoinGame joinGame;
	
	private ArgumentCaptor<ConnectionFactory.Notifier> notifier;

	@Start public void start(final Stage stage) {
		joinGame = new JoinGame(context, connectionFactory);
		joinGame.inflateView();
		
		final Scene scene = new Scene(joinGame);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();

		notifier = ArgumentCaptor.forClass(ConnectionFactory.Notifier.class);
//		Mockito.when(connectionFactory).connect(Mockito.eq("127.0.0.1"), Mockito.eq(1099), Mockito.eq("Christopher"), notifier.capture());

	
	}
	
	@Test void goBack(final FxRobot robot) {
		// GIVEN 
		// WHEN 
		robot.clickOn("#navIconBtn");
		
		// THEN 
		Mockito.verify(context).goBack();
	}

	@Test void cancelsAuthOnEscape(final FxRobot robot) {
		// GIVEN 
		final Auth auth = Mockito.mock(Auth.class);
		
		// WHEN 
		final ConnectionFactory.Notifier notifier = fillInFieldsAndConnect(robot);
		robot.interact(() -> {
			notifier.authenticate(auth);
		});
		
		// THEN 
		FxAssert.verifyThat("#passwordField", NodeMatchers.isEnabled());
		FxAssert.verifyThat("#passwordField", NodeMatchers.isFocused());
		
		// WHEN 
		robot.type(KeyCode.ESCAPE).sleep(20);
		
		// THEN 
		Mockito.verify(auth).cancel();
		robot.interact(() -> {
			notifier.notifyFailed("Cancelled");
		});
		FxAssert.verifyThat("#progressIndicator", NodeMatchers.isInvisible());
//		FxAssert.verifyThat("#connectBtn", NodeMatchers.isDisabled());
		FxAssert.verifyThat("#passwordField", NodeMatchers.isDisabled());
		FxAssert.verifyThat("#passwordField", TextInputControlMatchers.hasText(""));
		
	}
	
	@Test void auth(final FxRobot robot) {
		// GIVEN 
		final Auth auth = Mockito.mock(Auth.class);
		
		// WHEN 
		final ConnectionFactory.Notifier notifier = fillInFieldsAndConnect(robot);
		robot.interact(() -> {
			notifier.authenticate(auth);
		});
		
		// THEN 
		FxAssert.verifyThat("#passwordField", NodeMatchers.isEnabled());
		FxAssert.verifyThat("#passwordField", NodeMatchers.isFocused());
		
		// WHEN 
		robot.write("password").type(KeyCode.ENTER).sleep(10);
		
		// THEN 
		Mockito.verify(auth).password("password");
		robot.interact(() -> {
			notifier.notifySuccess(null);
		});
		FxAssert.verifyThat("#progressIndicator", NodeMatchers.isInvisible());
//		FxAssert.verifyThat("#connectBtn", NodeMatchers.isDisabled());
		FxAssert.verifyThat("#passwordField", NodeMatchers.isDisabled());
		FxAssert.verifyThat("#passwordField", TextInputControlMatchers.hasText(""));
		
	}
	
	@Test void showsMessageOnFailure(final FxRobot robot) {
		// GIVEN 
		
		
		// WHEN 
		final ConnectionFactory.Notifier notifier = fillInFieldsAndConnect(robot);
			
		// WHEN 
		robot.interact(() -> {
			notifier.notifyFailed("Could not find server");
		}).sleep(20);
	
		// THEN 
		FxAssert.verifyThat("#progressIndicator", NodeMatchers.isInvisible());
//		FxAssert.verifyThat("#connectBtn", NodeMatchers.isDisabled());
		FxAssert.verifyThat("#messageField", NodeMatchers.isVisible());
		FxAssert.verifyThat("#messageField", LabeledMatchers.hasText("Could not find server"));
		
	}
	
	private void fillInFields(FxRobot robot) {
		robot.clickOn("#nameField").write("Christopher").type(KeyCode.TAB);
		robot.clickOn("#hostField").write("127.0.0.1").type(KeyCode.TAB);
		robot.clickOn("#portField").write("1099").type(KeyCode.TAB);
	}

	@Test
	void connect(final FxRobot robot) {
		// GIVEN 
		
		// WHEN 
		final ConnectionFactory.Notifier notifier = fillInFieldsAndConnect(robot);
		
		// WHEN 
		robot.interact(() -> {
			notifier.notifySuccess(null);
		}).sleep(10);

		// THEN 
		FxAssert.verifyThat("#progressIndicator", NodeMatchers.isInvisible());
		FxAssert.verifyThat("#connectBtn", NodeMatchers.isEnabled());
		
		Mockito.verifyNoMoreInteractions(connectionFactory);
	}

	private Notifier fillInFieldsAndConnect(FxRobot robot) {
		// WHEN 
		robot.clickOn("#nameField").write("Christopher").type(KeyCode.TAB);
		robot.clickOn("#hostField").write("127.0.0.1").type(KeyCode.TAB);
		robot.clickOn("#portField").write("1099").type(KeyCode.TAB);
		robot.clickOn("#connectBtn");
		
		// THEN 
		FxAssert.verifyThat("#progressIndicator", NodeMatchers.isVisible());
		FxAssert.verifyThat("#connectBtn", NodeMatchers.isDisabled());
		Mockito.verify(connectionFactory).connect(Mockito.eq("127.0.0.1"), Mockito.eq(1099), Mockito.eq("Christopher"), notifier.capture());
		
		return notifier.getValue();
	}

}
