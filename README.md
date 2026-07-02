# orobas

[Orobas](https://en.wikipedia.org/wiki/Orobas) is a Java implementation of the [Redux state container](https://redux.js.org/). It brings predictable state management to your JVM applications with a minimal, no-dependency library.

Available in [Maven Central](https://mvnrepository.com/artifact/io.vacco.orobas/orobas)

## Example

```java
// 1. Define your actions
public static class Increment extends ObAction<Integer> {}
public static class Decrement extends ObAction<Integer> {}
public static class Reset extends ObAction<Void> {}

// 2. Define your reducer (pure function: (action, state) -> new state)
ObReducer<Integer> counterReducer = (action, currentState) ->
    new ObMatch<Integer>()
        .on(Increment.class, inc -> currentState + inc.payload)
        .on(Decrement.class, dec -> currentState - dec.payload)
        .on(Reset.class,   rst -> 0)
        .orElse(() -> currentState)
        .apply(action);

// 3. Create the store
ObStore<Integer> store = new ObStore<>(counterReducer, 0, Throwable::printStackTrace);

// 4. Subscribe to state changes
store.subscribe(count -> System.out.println("Count: " + count));

// 5. Dispatch actions
store.dispatch(new Increment().withPayload(1)); // Count: 1
store.dispatch(new Increment().withPayload(2)); // Count: 3
store.dispatch(new Decrement().withPayload(1)); // Count: 2
store.dispatch(new Reset());                     // Count: 0
```

## Key concepts

| Concept | What it is |
|---|---|
| **Action** (`ObAction`) | A description of something that happened. Carries an optional payload. |
| **Reducer** (`ObReducer`) | A pure function that takes the current state and an action and returns the next state. Should not mutate anything or have side effects. |
| **Store** (`ObStore`) | Holds the state, dispatches actions, and notifies subscribers. The single source of truth. |
| **Middleware** (`ObMiddleware`) | A chain of interceptors between dispatch and the reducer. Great for logging, async, etc. |
| **Selector** (`ObSelector`) | Manages a slice of the state tree. Combine multiple selectors to handle different parts of the state independently. |

## Concurrent dispatching

Orobas provides `ObConcurrentStore`, which queues actions from any number of producer threads and processes them sequentially on a single consumer thread:

```java
ObConcurrentStore<Integer> store = new ObConcurrentStore<>(
    counterReducer, 0, Throwable::printStackTrace
).start();

// Can be called from any thread — fully thread-safe
store.dispatch(new Increment().withPayload(1));

// Later, when done
store.stop();
```

Subscribers are always called on the store's consumer thread, so you never need locks inside your reducer or subscriber logic. Just make sure your state objects are safe for concurrent reads (immutable objects work best).
