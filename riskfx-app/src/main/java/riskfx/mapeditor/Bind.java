package riskfx.mapeditor;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextInputControl;

public class Bind {

	public static <T> void bind(final TextInputControl control, final ObservableValue<T> property,
			final Function<T, StringProperty> mapper) {
		Objects.requireNonNull(control);
		Objects.requireNonNull(property);
		Objects.requireNonNull(mapper);

		final AtomicReference<StringProperty> propRef = new AtomicReference<>();
		final InvalidationListener invalidationListener = o -> {
			final StringProperty old = propRef.get();
			if (old != null) {
				control.textProperty().unbindBidirectional(old);
			}

			final T value = property.getValue();
			if (value != null) {
				final StringProperty sProperty = mapper.apply(value);
				if (property != null) {
					propRef.set(sProperty);
					control.textProperty().bindBidirectional(sProperty);
				}
			}
		};
		property.addListener(invalidationListener);
		invalidationListener.invalidated(property);
	}

	public static <T, V, P extends Property<V>> void bind(ObjectProperty<V> valueProperty,
			final ObservableValue<T> property, Function<T, P> mapper) {
		final InvalidationListener invalidationListener = o -> {
			valueProperty.unbind();

			final T value = property.getValue();
			if (value != null) {
				final P prop = mapper.apply(value);
				if (prop != null) {
					valueProperty.bindBidirectional(prop);
				}
			}
		};
		property.addListener(invalidationListener);
		invalidationListener.invalidated(property);
	}

}
