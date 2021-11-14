package appfx.window;

public class NotificationHelper {
/*	
	@FunctionalInterface
	private static interface Notication {
		public void apply(final NotificationPane pane);
	}
	
	private final Sinks.Many<Notication> notifications = Sinks.many().unicast().onBackpressureBuffer();
	
	private final NotificationPane pane;
	
	public NotificationHelper(final NotificationPane pane, final Duration hideDuration) {
		this.pane = Objects.requireNonNull(pane);
		
		notifications.asFlux()
			.publishOn(FxSchedulers.fxThread())
			.subscribe(n -> {
				n.apply(pane);
			});
		FxFlux.from(pane.showingProperty())
			.filter(b -> b == true)
			.switchMap(b -> Flux.interval(hideDuration)
					.take(1)
					.publishOn(FxSchedulers.fxThread()))
			.subscribe(l -> {
				pane.hide();
			});
	}
	
	public final void notify(final UserNotification n) {
		notifications.tryEmitNext(pane -> {
			pane.show(n.message());
		});
	}
*/
}
