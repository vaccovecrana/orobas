package io.vacco.orobas;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Manages a slice of a root state. Each selector defines how to extract a
 * sub-state, reduce it, and merge it back. Multiple selectors can be combined
 * into a single root reducer via {@link #combineSelectors(ObSelector[])}.
 *
 * @param <R> the root state type.
 * @param <S> this selector's state slice type.
 */
public class ObSelector<R, S> implements ObReducer<R> {

  private final BiFunction<R, S, R> merger;
  private final Function<R, S> mapper;
  private final ObReducer<S> reducer;

  /**
   * @param mapper  extracts the relevant slice from the root state.
   * @param reducer reduces actions against the extracted slice.
   * @param merger  merges the reduced slice back into the root state.
   */
  public ObSelector(Function<R, S> mapper, ObReducer<S> reducer, BiFunction<R, S, R> merger) {
    this.mapper = mapper;
    this.reducer = reducer;
    this.merger = merger;
  }

  @Override
  public R reduce(ObAction<?> action, R rootState) {
    return merger.apply(rootState, reducer.reduce(action, mapper.apply(rootState)));
  }

  /**
   * Combines multiple selectors into a single root reducer. Each selector is
   * applied sequentially in declaration order, allowing independent state
   * slices to be managed by their own reducers.
   *
   * @param selectors the selectors to combine.
   * @param <R>       the root state type.
   * @return a reducer that applies each selector in order.
   */
  @SafeVarargs
  public static <R> ObReducer<R> combineSelectors(ObSelector<R, ?>... selectors) {
    return ((action, currentState) -> {
      for (ObSelector<R, ?> sel : selectors) {
        currentState = sel.reduce(action, currentState);
      }
      return currentState;
    });
  }

}