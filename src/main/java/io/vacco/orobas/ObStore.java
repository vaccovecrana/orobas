package io.vacco.orobas;

import java.util.*;
import java.util.function.Consumer;

/**
 * Brings actions and reducers together, and holds state of the app.
 * It allows to get current state, subscribe to state changes, and to update state by dispatching actions.
 * It is the single point of truth in your application.
 *
 * @param <S> State type. Describes the state of your app. Should be immutable.
 */
public class ObStore<S> {

  private S currentState;
  private final ObReducer<S> reducer;
  private final List<Consumer<S>> subscribers = new ArrayList<>();

  private   final ObActionHandler rootSink;
  private   final Map<Integer, ObActionHandler> sinkCache = new HashMap<>();
  protected final Consumer<Throwable> errorHandler;

  /**
   * @param reducer      root reducer
   * @param state        initial state
   * @param errorHandler error handler called upon a subscriber error
   * @param middlewares  middlewares to register in store
   */
  @SuppressWarnings("varargs")
  @SafeVarargs public ObStore(ObReducer<S> reducer, S state,
                              Consumer<Throwable> errorHandler,
                              ObMiddleware<S> ... middlewares) {
    this.reducer = reducer;
    this.currentState = state;
    this.rootSink = next(0, middlewares);
    this.errorHandler = errorHandler;
  }

  private void notify(S currentState) {
    try { subscribers.forEach(c -> c.accept(currentState)); }
    catch (Exception e) { errorHandler.accept(e); }
  }

  private ObActionHandler next(int index, ObMiddleware<S>[] middlewares) {
    return sinkCache.computeIfAbsent(index, k -> index == middlewares.length ? action -> {
      currentState = reducer.reduce(action, currentState);
      notify(currentState);
      return action;
    } : action -> middlewares[index].handle(this, action, next(index + 1, middlewares)));
  }

  public void dispatch(ObAction<?> action) {
    rootSink.apply(action);
  }

  public S getState() { return this.currentState; }

  /**
   * Subscribe to store state changes.
   *
   * @param consumer will be called on store's state change.
   * @return a handle to unsubscribe <code>consumer</code> from this store's state changes.
   */
  public Runnable subscribe(Consumer<S> consumer) {
    this.subscribers.add(consumer);
    return () -> subscribers.remove(consumer);
  }

}
