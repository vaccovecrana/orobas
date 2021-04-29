package io.vacco.orobas;

/**
 * Base action handler definition.
 */
public interface ObActionHandler {

  /**
   * @param action the active action.
   * @return a new action, or the same input as <code>action</code>.
   */
  ObAction<?> apply(ObAction<?> action);

}
