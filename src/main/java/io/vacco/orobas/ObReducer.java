package io.vacco.orobas;

/**
 * Describes how a store's state changes in response to actions.
 *
 * @param <S> State type.
 */
public interface ObReducer<S> {

  /**
   * Calculates a new state. It shouldn't modify actions, the current state or any 3rd party objects.
   * A reducer called 100 times must return 100 times the same result, based only on input parameters.
   * If a reducer decides to not change state of store, it must return the state unmodified, otherwise it should
   * return a new state object or a clone of the current state with applied modifications.
   *
   * @param action       action which describes what happened.
   * @param currentState current state of store.
   * @return new state of the store.
   */
  S reduce(ObAction<?> action, S currentState);

}
