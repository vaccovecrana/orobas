package io.vacco.orobas;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Manages a slice of a global state. Multiple selectors can be combined
 * into a single reducer.
 *
 * @param <R> the type of the root state.
 * @param <S> the type of this selector's state slice.
 */
public class ObSelector<R, S> implements ObReducer<R> {

  private final BiFunction<R, S, R> merger;
  private final Function<R, S> mapper;
  private final ObReducer<S> reducer;

  /**
   * @param mapper a function which selects the root state's target slice.
   * @param reducer the reducer logic to operate on the target state slice.
   * @param merger a function to merge back the slice into the global root state.
   */
  public ObSelector(Function<R, S> mapper, ObReducer<S> reducer, BiFunction<R, S, R> merger) {
    this.mapper = mapper;
    this.reducer = reducer;
    this.merger = merger;
  }

  @Override public R reduce(ObAction<?> action, R rootState) {
    return merger.apply(rootState, reducer.reduce(action, mapper.apply(rootState)));
  }

  /**
   * Combines multiple selectors for a root state.
   *
   * @param selectors the selectors to combine.
   * @param <A> an action type to which all selectors align.
   * @param <R> type of the root state.
   * @return a reducer which sequentially applies the provided selectors onto the root state.
   */
  @SafeVarargs
  public static <A extends Enum<?>, R> ObReducer<R> combineSelectors(ObSelector<R, ?>... selectors) {
    return ((action, currentState) -> {
      for (ObSelector<R, ?> sel : selectors) {
        currentState = sel.reduce(action, currentState);
      }
      return currentState;
    });
  }
}
