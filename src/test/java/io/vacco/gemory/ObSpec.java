package io.vacco.gemory;

import com.esotericsoftware.jsonbeans.Json;
import io.vacco.orobas.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static j8spec.J8Spec.it;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class ObSpec {

  private static final Json j = new Json();

  public static class CounterAction extends ObAction<Integer> {
  }

  public static class Increase extends CounterAction {
  }

  public static class Decrease extends CounterAction {
  }

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
        (action, currentState) -> new ObMatch<Integer>()
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

  public static class Ingredient {
  }

  public static final class Tomato extends Ingredient {
  }

  public static final class Lettuce extends Ingredient {
  }

  public static final class Onion extends Ingredient {
  }

  public static final class Beef extends Ingredient {
  }

  public static final class Chicken extends Ingredient {
  }

  public static final class Ketchup extends Ingredient {
  }

  public static final class Mustard extends Ingredient {
  }

  public static final class Mayo extends Ingredient {
  }

  public enum Bread {Wheat, Flat}

  public static final class Sandwich {
    public Bread breadType;
    public List<Ingredient> ingredients = new ArrayList<>();
  }

  public static class AddIngredient extends ObAction<Ingredient> {
  }

  public static class SelectBread extends ObAction<Bread> {
  }

  static {
    it("Makes a sandwich", () -> {
      ObConcurrentStore<Sandwich> sandwichStore = new ObConcurrentStore<>(
        ObSelector.combineSelectors(
          new ObSelector<>(
            sandwich -> sandwich.breadType,
            (action, bread) -> new ObMatch<Bread>()
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
            (action, ingredients) -> new ObMatch<List<Ingredient>>()
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

  static {
    it("Makes sandwiches concurrently from 3 threads", () -> {
      ObConcurrentStore<Sandwich> sandwichStore = new ObConcurrentStore<>(
        ObSelector.combineSelectors(
          new ObSelector<>(
            sandwich -> sandwich.breadType,
            (action, bread) -> new ObMatch<Bread>()
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
            (action, ingredients) -> new ObMatch<List<Ingredient>>()
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

      sandwichStore.start();
      int threadCount = 3;
      CountDownLatch latch = new CountDownLatch(threadCount);

      Thread t1 = new Thread(() -> {
        latch.countDown();
        try {
          latch.await();
        } catch (InterruptedException e) {
          return;
        }
        sandwichStore.dispatch(new SelectBread().withPayload(Bread.Flat));
        sandwichStore.dispatch(new AddIngredient().withPayload(new Beef()));
        sandwichStore.dispatch(new AddIngredient().withPayload(new Mayo()));
        sandwichStore.dispatch(new AddIngredient().withPayload(new Lettuce()));
      });

      Thread t2 = new Thread(() -> {
        latch.countDown();
        try {
          latch.await();
        } catch (InterruptedException e) {
          return;
        }
        sandwichStore.dispatch(new SelectBread().withPayload(Bread.Wheat));
        sandwichStore.dispatch(new AddIngredient().withPayload(new Chicken()));
        sandwichStore.dispatch(new AddIngredient().withPayload(new Ketchup()));
        sandwichStore.dispatch(new AddIngredient().withPayload(new Onion()));
      });

      Thread t3 = new Thread(() -> {
        latch.countDown();
        try {
          latch.await();
        } catch (InterruptedException e) {
          return;
        }
        sandwichStore.dispatch(new AddIngredient().withPayload(new Tomato()));
        sandwichStore.dispatch(new AddIngredient().withPayload(new Mustard()));
      });

      t1.start();
      t2.start();
      t3.start();
      t1.join();
      t2.join();
      t3.join();

      Thread.sleep(2000);
      sandwichStore.stop();

      Sandwich sandwich = sandwichStore.getState();
      print(sandwich);
      assert sandwich.ingredients.size() == 8 : "Expected 8 ingredients, got " + sandwich.ingredients.size();
      assert sandwich.ingredients.stream().anyMatch(i -> i instanceof Tomato) : "Missing Tomato";
      assert sandwich.ingredients.stream().anyMatch(i -> i instanceof Lettuce) : "Missing Lettuce";
      assert sandwich.ingredients.stream().anyMatch(i -> i instanceof Onion) : "Missing Onion";
      assert sandwich.ingredients.stream().anyMatch(i -> i instanceof Beef) : "Missing Beef";
      assert sandwich.ingredients.stream().anyMatch(i -> i instanceof Chicken) : "Missing Chicken";
      assert sandwich.ingredients.stream().anyMatch(i -> i instanceof Ketchup) : "Missing Ketchup";
      assert sandwich.ingredients.stream().anyMatch(i -> i instanceof Mustard) : "Missing Mustard";
      assert sandwich.ingredients.stream().anyMatch(i -> i instanceof Mayo) : "Missing Mayo";
    });
  }

}
