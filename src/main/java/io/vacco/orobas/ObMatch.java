package io.vacco.orobas;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Minimal pattern-matching dispatcher for actions. Intended for use inside
 * reducers to route an action to the appropriate handler based on its class.
 * <p>
 * Usage:
 * <pre>{@code
 * new ObMatch<State>()
 *     .on(Increment.class, inc -> state + inc.payload)
 *     .on(Decrement.class, dec -> state - dec.payload)
 *     .orElse(() -> state)
 *     .apply(action);
 * }</pre>
 *
 * @param <S> the return type (typically the store's state or a state slice).
 */
public class ObMatch<S> {

  private final Map<Class<?>, Function<?, S>> matchFn = new HashMap<>();
  private Supplier<S> defaultFn;

  /**
   * Register a handler for a specific action class. Matches on the exact
   * class (not subtypes). Can be called multiple times to build up patterns.
   *
   * @param pattern the exact action class to match.
   * @param fn      function that produces a result from a matched action.
   * @param <A>     the action type.
   * @return this instance, for chaining.
   */
  public <A extends ObAction<?>> ObMatch<S> on(Class<? extends A> pattern, Function<A, S> fn) {
    matchFn.put(pattern, fn);
    return this;
  }

  /**
   * Set a fallback supplier used when no registered pattern matches the action.
   *
   * @param defaultFn supplier for the default result.
   * @return this instance, for chaining.
   */
  public ObMatch<S> orElse(Supplier<S> defaultFn) {
    this.defaultFn = defaultFn;
    return this;
  }

  /**
   * Dispatch an action to the matching handler. If no handler matches and no
   * default was set via {@link #orElse(Supplier)}, an
   * {@link IllegalStateException} is thrown.
   *
   * @param action the action to dispatch.
   * @param <A>    the action type.
   * @return the result produced by the matched handler or the default supplier.
   */
  @SuppressWarnings("unchecked")
  public <A extends ObAction<?>> S apply(A action) {
    Function<A, S> fn = (Function<A, S>) matchFn.get(action.getClass());
    if (fn != null) {
      return fn.apply(action);
    } else if (defaultFn != null) {
      return defaultFn.get();
    }
    throw new IllegalStateException(String.format("No pattern matched for action [%s]", action));
  }

}
