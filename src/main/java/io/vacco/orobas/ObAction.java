package io.vacco.orobas;

/**
 * Describes an intent to change store state. Extend this class to define
 * your application's actions.
 * <p>
 * <b>Thread safety note:</b> In a {@link ObConcurrentStore}, the {@code payload}
 * field is typically written on a producer thread (before dispatch) and read
 * on the store's consumer thread. The payload itself should be effectively
 * immutable or at least safe for read-only access across threads. Avoid
 * modifying a payload after dispatching it.
 *
 * @param <V> the payload value type carried by this action.
 */
public abstract class ObAction<V> {

  public V payload;

  public ObAction<V> withPayload(V payload) {
    this.payload = payload;
    return this;
  }

  @Override
  public String toString() {
    return getClass().getCanonicalName();
  }

}
