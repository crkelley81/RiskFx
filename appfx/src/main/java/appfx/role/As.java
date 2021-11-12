package appfx.role;

import java.util.Collection;
import java.util.Optional;

public interface As {

	public <T> Optional<T> as(final Class<T> clazz);
	
	public static <T> Optional<T> as(final Class<T> type, final Collection<Object> context) {
		return context.stream()
				.filter(o -> o.getClass().isAssignableFrom(type))
				.map(o -> type.cast(o))
				.findFirst();
	}
}
