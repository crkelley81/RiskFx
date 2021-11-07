package appfx.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.inject.Singleton;

import com.gluonhq.ignite.guice.GuiceContext;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import appfx.util.UserNotification;
import appfx.util.UserNotificationWithAction;
import appfx.window.MainWindow;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppFxApplication extends Application {
	private GuiceContext context;
	private MainWindow mainWindow;
	
	@Override
	public final void start(final Stage primaryStage) throws Exception {
		context = new GuiceContext(this, () -> provideModules(primaryStage, this.getHostServices(), this.getParameters()));
		context.init();
		
		mainWindow = context.getInstance(MainWindow.class);
		
		final Scene scene = new Scene(mainWindow);
		primaryStage.setScene(scene);
		
		postInit(scene, primaryStage);	
		primaryStage.show();
	}

	
	
	@Override
	public void stop() throws Exception {
		context.dispose();
	}



	private Collection<com.google.inject.Module> provideModules(final Stage primaryStage, final HostServices hostServices, final Parameters parameters) {
		final List<com.google.inject.Module> modules = new ArrayList<>();
		modules.add(new AppFxModule(primaryStage, hostServices, parameters));
		final List<com.google.inject.Module> addlModules = new ArrayList<>();
		initModules(addlModules);
		
		modules.addAll(addlModules);
		
		return modules;
	}

	protected void initModules(final Collection<com.google.inject.Module> modules) {
		
	}

	protected void postInit(final Scene scene, final Stage primaryStage) {
		
	}
	
	private static class AppFxModule extends AbstractModule {

		private Stage primaryStage;
		private HostServices hostServiecs;
		private Parameters parameters;

		public AppFxModule(Stage primaryStage, HostServices hostServices, Parameters parameters) {
			this.primaryStage = Objects.requireNonNull(primaryStage);
			this.hostServiecs = Objects.requireNonNull(hostServices);
			this.parameters = Objects.requireNonNull(parameters);
			
		}

		@Override
		protected void configure() {
			this.bind(Parameters.class).toInstance(parameters);
			this.bind(Stage.class).toInstance(primaryStage);
		}
		
		@Provides @Singleton public UiContext provideContext(final MainWindow window) {
			return new UiContext() {

				@Override
				public void lockView(UserNotification n) {
					window.lockView(n);
				}

				@Override
				public void unlockView() {
					window.unlockView();
				}

				@Override
				public void notify(UserNotification n) {
					window.notify(n);
				}

				@Override
				public void notify(UserNotificationWithAction n) {
//					window.notify(n);
					
				}
				
			};
		}
		
	}
}
