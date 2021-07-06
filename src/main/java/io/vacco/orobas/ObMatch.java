package io.vacco.orobas;

import java.util.*;
import java.util.function.*;

/**
 * Minimal pattern matching support for actions.
 * @param <S> the state type.
 */
public class ObMatch<S> {

  private final Map<Class<?>, Function<?, S>> matchFn = new HashMap<>();
  private Supplier<S> defaultFn;

  public <A extends ObAction<?>> ObMatch<S> on(Class<? extends A> pattern, Function<A, S> cn) {
    matchFn.put(pattern, cn);
    return this;
  }

  public ObMatch<S> orElse(Supplier<S> defaultFn) {
    this.defaultFn = defaultFn;
    return this;
  }

  @SuppressWarnings("unchecked")
  public <A extends ObAction<?>> S apply(A action) {
    Function<A, S> fn = (Function<A, S>) matchFn.get(action.getClass());
    if (fn != null) { return fn.apply(action); }
    else if (defaultFn != null) { return defaultFn.get(); }
    throw new IllegalStateException(String.format("No pattern matched for action [%s]", action));
  }

}
