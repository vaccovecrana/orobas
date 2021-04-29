package io.vacco.orobas;

import java.util.function.Consumer;

/** Middleware which logs all actions and state changes in store. */
public class ObLogger<S> implements ObMiddleware<S> {

  private final Consumer<ObAction<?>> onAction;
  private final Consumer<S> onState;

  public ObLogger(Consumer<ObAction<?>> onAction, Consumer<S> onState) {
    this.onAction = onAction;
    this.onState = onState;
  }

  @Override public ObAction<?> handle(ObStore<S> store, ObAction<?> action, ObActionHandler next) {
    this.onAction.accept(action);
    ObAction<?> out = next.apply(action);
    this.onState.accept(store.getState());
    return out;
  }

}
