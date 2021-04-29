package io.vacco.orobas;

public abstract class ObAction<V> {

  public V payload;

  public ObAction<V> withPayload(V payload) {
    this.payload = payload;
    return this;
  }

  @Override public String toString() { return getClass().getCanonicalName(); }

}
