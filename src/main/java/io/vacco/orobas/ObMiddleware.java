package io.vacco.orobas;

/**
 * Third-party extension point between dispatching an action and
 * the moment it reaches the root reducer.
 *
 * @param <S> State type
 */
public interface ObMiddleware<S> {

  /**
   * Intercepts an action as either:
   * <ul>
   *   <li>Do nothing.</li>
   *   <li>Transform <code>action</code>.</li>
   *   <li>Delegate <code>action</code> to the <code>next</code> middleware.</li>
   * </ul>
   *
   * @param store  allows for optional action dispatching (e. g. for asynchronous processing).
   * @param action the active action inside the store.
   * @param next   the next action handler in the processing chain.
   * @return a new action, or the same input as <code>action</code>.
   */
  ObAction<?> handle(ObStore<S> store, ObAction<?> action, ObActionHandler next);

}
