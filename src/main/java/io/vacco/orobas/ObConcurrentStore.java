package io.vacco.orobas;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * Allows for concurrent action dispatching from multiple threads.
 * Note that subscribers on this store will be invoked from the store's
 * thread is driving the store's internal queue.
 *
 * @param <S> the store type.
 * @see BlockingQueue
 */
public class ObConcurrentStore<S> extends ObStore<S> implements Runnable {

  private final BlockingQueue<ObAction<?>> queue = new LinkedBlockingQueue<>();
  private boolean stopped = false;

  /**
   * @param reducer      root reducer
   * @param state        initial state
   * @param errorHandler error handler called upon a subscriber error, or store execution termination
   * @param middlewares  middlewares to register in store
   */
  @SafeVarargs @SuppressWarnings("varargs")
  public ObConcurrentStore(ObReducer<S> reducer, S state,
                           Consumer<Throwable> errorHandler,
                           ObMiddleware<S>... middlewares) {
    super(reducer, state, errorHandler, middlewares);
  }

  @Override public void dispatch(ObAction<?> action) { queue.offer(action); }

  public void run() {
    try { while (!stopped) { super.dispatch(queue.take()); } }
    catch (InterruptedException ex) {
      this.stop();
      this.errorHandler.accept(ex);
    }
    this.queue.clear();
  }

  public ObConcurrentStore<S> start() {
    new Thread(this, String.format("%s-Actor", getClass().getCanonicalName())).start();
    return this;
  }

  public void stop() { this.stopped = true; }

}
