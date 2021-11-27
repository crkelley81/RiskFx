package riskfx.util.role;

import java.util.Collection;
import java.util.Optional;

public interface As {

	public <T> Optional<T> as(final Class<T> clazz);
	
	public static <T> Optional<T> as(final Class<T> type, final Collection<Object> context) {
		return context.stream()
//				.peek(o -> System.err.println("Looking for " + type + " checking " + o.getClass() + " " + o.getClass().isAssignableFrom(type)))
				.filter(o -> type.isAssignableFrom(o.getClass()))
				.map(o -> type.cast(o))
				.findFirst();
	}
}
