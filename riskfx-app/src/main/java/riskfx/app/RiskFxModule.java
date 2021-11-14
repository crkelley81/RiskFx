package riskfx.app;

import java.util.Objects;

import javax.inject.Inject;

import appfx.ui.UiContext;
import dagger.Provides;

@dagger.Module
public class RiskFxModule {

	private final UiContext context;
	
	@Inject public RiskFxModule(final UiContext context) {
		this.context = Objects.requireNonNull(context);
	}
	
	@Provides  public UiContext providesUiContext() {
		return this.context;
	}
}
