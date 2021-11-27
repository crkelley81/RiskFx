package riskfx.util;

import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

public /* sealed */ interface Try<T> /* permits Success, Failure */ {

	public static <T> Try<T> success(final T value) {
		return new Success<>(value);
	}
	
	public static <T> Try<T> failed(final Throwable ex) {
		return new Failure<>(ex);
	}
	
	public static <T> Try<T> tryGet(final Callable<T> callable) {
		try {
			final T value = callable.call();
			return Try.success(value);
		}
		catch (Throwable ex) {
			return Try.failed(ex);
		}
	}
	
	public boolean isSuccess();
	public boolean isFailure();
	public T get();
	
	static final class Success<T> implements Try<T> {
		private final T value;
		
		public Success(final T value) {
			this.value = value;
		}

		@Override
		public boolean isSuccess() {
			return true;
		}

		@Override
		public boolean isFailure() {
			return false;
		}

		@Override
		public T get() {
			return this.value;
		}
	}
	
	static final class Failure<T> implements Try<T> {
		private final Throwable ex;
		
		public Failure(final Throwable ex) {
			this.ex = ex;
		}

		@Override
		public boolean isSuccess() {
			return false;
		}

		@Override
		public boolean isFailure() {
			return true;
		}

		@Override
		public T get() {
			throw new NoSuchElementException();
		}
	}
}
