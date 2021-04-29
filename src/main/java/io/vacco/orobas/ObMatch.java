package io.vacco.orobas;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Minimal pattern matching support for actions.
 * @param <S> the state type.
 * @param <A> the action type.
 */
public class ObMatch<S, A extends ObAction<?>> {

  private final Map<Class<? extends A>, Function<A, S>> matchFn = new HashMap<>();
  private Supplier<S> defaultFn;

  public ObMatch<S, A> on(Class<? extends A> pattern, Function<A, S> cn) {
    matchFn.put(pattern, cn);
    return this;
  }

  public ObMatch<S, A> orElse(Supplier<S> defaultFn) {
    this.defaultFn = defaultFn;
    return this;
  }

  @SuppressWarnings("unchecked")
  public S apply(ObAction<?> action) {
    Function<A, S> fn = matchFn.get(action.getClass());
    if (fn != null) { return fn.apply((A) action); }
    else if (defaultFn != null) { return defaultFn.get(); }
    throw new IllegalStateException(String.format("No pattern matched for action [%s]", action));
  }

}
