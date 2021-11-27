package riskfx.app;

import javax.inject.Singleton;

import dagger.Component;
import riskfx.app.view.Navigation;

@Singleton
@Component(modules = { RiskFxModule.class, NetworkModule.class })
public interface RiskFxApp {
	public Navigation navigation();

}
