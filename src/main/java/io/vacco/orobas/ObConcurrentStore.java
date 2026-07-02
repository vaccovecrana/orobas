package io.vacco.orobas;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * Allows for concurrent action dispatching from multiple threads.
 * <p>
 * <b>Action producers</b> — {@link #dispatch(ObAction)} is non-blocking and
 * thread-safe. Any number of threads may call it concurrently without external
 * synchronization.
 * <p>
 * <b>Reducers and subscribers</b> — All action processing (reduction, middleware
 * execution, and subscriber notification) happens on a single consumer thread
 * driven by the store's internal queue. Therefore reducers and subscribers
 * don't need to be thread-safe themselves, but they <b>must not block</b> or
 * perform long-running work (doing so stalls the entire store pipeline).
 * <p>
 * <b>State visibility</b> — Reducers operate on a snapshot of the current
 * state. The state object referenced by {@link #getState()} is updated on the
 * consumer thread after each action is fully processed. External threads calling
 * {@code getState()} are guaranteed to see a consistent (though possibly stale)
 * snapshot. For predictable concurrent reads, prefer immutable state objects.
 * <p>
 * Subscribers on this store will be invoked from the store's consumer thread.
 *
 * @param <S> the store type.
 * @see BlockingQueue
 */
public class ObConcurrentStore<S> extends ObStore<S> implements Runnable {

  private final BlockingQueue<ObAction<?>> queue = new LinkedBlockingQueue<>();
  private volatile boolean stopped = false;

  /**
   * @param reducer      root reducer
   * @param state        initial state
   * @param errorHandler error handler called upon a subscriber error, or store execution termination
   * @param middlewares  middlewares to register in store
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public ObConcurrentStore(ObReducer<S> reducer, S state,
                           Consumer<Throwable> errorHandler,
                           ObMiddleware<S>... middlewares) {
    super(reducer, state, errorHandler, middlewares);
  }

  @Override
  public void dispatch(ObAction<?> action) {
    queue.offer(action);
  }

  public void run() {
    try {
      while (!stopped) {
        super.dispatch(queue.take());
      }
    } catch (InterruptedException ex) {
      this.stop();
      this.errorHandler.accept(ex);
    }
    this.queue.clear();
  }

  public ObConcurrentStore<S> start() {
    new Thread(this, String.format("%s-Actor", getClass().getCanonicalName())).start();
    return this;
  }

  public void stop() {
    this.stopped = true;
  }

}
