package io.vacco.gemory;

import com.esotericsoftware.jsonbeans.Json;
import io.vacco.orobas.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class ObSpec {

  private static final Json j = new Json();

  public static class CounterAction extends ObAction<Integer> {}
  public static class Increase extends CounterAction {}
  public static class Decrease extends CounterAction {}

  public static final ObMiddleware<Integer> print1 = ((store, action, next) -> {
    System.out.println("Print 1 start");
    action = next.apply(action);
    System.out.println("Print 1 end");
    return action;
  });

  public static final ObMiddleware<Integer> print2 = ((store, action, next) -> {
    System.out.println("Print 2 start");
    action = next.apply(action);
    System.out.println("Print 2 end");
    return action;
  });

  public static final ObMiddleware<Integer> logger = new ObLogger<>(a -> System.out.printf("%s%n", a), ObSpec::print);

  private static void print(Object o) {
    System.out.println(j.toJson(o));
  }

  static {
    it("Logs a counter's increase/decrease actions", () -> {
      ObStore<Integer> counterStore = new ObStore<>(
          (action, currentState) -> new ObMatch<Integer, CounterAction>()
              .on(Increase.class, inc -> currentState + 1)
              .on(Decrease.class, dec -> currentState - 1)
              .orElse(() -> currentState)
              .apply(action), 0, Throwable::printStackTrace, logger, print1, print2
      );
      counterStore.subscribe(count -> System.out.printf("State listener update: [%s]%n", count));
      counterStore.dispatch(new Increase().withPayload(1));
      counterStore.dispatch(new Decrease().withPayload(1));
    });
  }

  public static class Ingredient {}
  public static final class Tomato extends Ingredient {}
  public static final class Lettuce extends Ingredient {}
  public static final class Onion extends Ingredient {}
  public static final class Beef extends Ingredient {}
  public static final class Chicken extends Ingredient {}
  public static final class Ketchup extends Ingredient {}
  public static final class Mustard extends Ingredient {}
  public static final class Mayo extends Ingredient {}

  public enum Bread {Wheat, Flat}

  public static final class Sandwich {
    public Bread breadType;
    public List<Ingredient> ingredients = new ArrayList<>();
  }

  public static class AddIngredient extends ObAction<Ingredient> {}
  public static class SelectBread extends ObAction<Bread> {}

  static {
    it("Makes a sandwich", () -> {
      ObConcurrentStore<Sandwich> sandwichStore = new ObConcurrentStore<>(
          ObSelector.combineSelectors(
              new ObSelector<>(
                  sandwich -> sandwich.breadType,
                  (action, bread) -> new ObMatch<Bread, SelectBread>()
                      .on(SelectBread.class, sb -> sb.payload)
                      .orElse(() -> bread)
                      .apply(action),
                  (sandwich, bread) -> {
                    sandwich.breadType = bread;
                    return sandwich;
                  }
              ),
              new ObSelector<>(
                  sandwich -> sandwich.ingredients,
                  (action, ingredients) -> new ObMatch<List<Ingredient>, AddIngredient>()
                      .on(AddIngredient.class, add -> {
                        ingredients.add(add.payload);
                        return ingredients;
                      })
                      .orElse(() -> ingredients)
                      .apply(action),
                  (sandwich, ingredients) -> {
                    sandwich.ingredients = ingredients;
                    return sandwich;
                  }
              )
          ), new Sandwich(),
          Throwable::printStackTrace,
          new ObLogger<>(
              a -> System.out.printf("%s - %s%n", Thread.currentThread().getName(), a),
              ObSpec::print
          )
      );

      sandwichStore.dispatch(new SelectBread().withPayload(Bread.Flat));
      for (Ingredient ing : new Ingredient[]{new Beef(), new Mayo(), new Onion(), new Lettuce(), new Tomato(), new Ketchup()}) {
        sandwichStore.dispatch(new AddIngredient().withPayload(ing));
      }
      sandwichStore.dispatch(new SelectBread().withPayload(Bread.Wheat));

      sandwichStore.start();
      Thread.sleep(5000);
      sandwichStore.stop();

      print(sandwichStore.getState());
    });
  }

}
