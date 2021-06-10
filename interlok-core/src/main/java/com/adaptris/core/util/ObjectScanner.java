package com.adaptris.core.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ClassUtils;
import com.adaptris.interlok.util.Args;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Synchronized;

/**
 * Breadth first object tree traversal to find all the matches within a tree.
 * <p>
 * Not especially clever really. Since our "components" might implement equals() we use an
 * IdentityHashMap with the object as the key, so even if say a KeyValuePairSet contains the "same"
 * elements they'll be different for the purposes of the scanner.
 * </p>
 */
@NoArgsConstructor
public abstract class ObjectScanner<T> {


  private enum Scanner {
    ARRAY {
      @Override
      public boolean traversable(Object o, Class<?> clazz) {
        return clazz.isArray();
      }
      @Override
      public void traverse(Class<?> clazz, Object obj, BiConsumer<Object, Class<?>> consumer) {
        final int len = Array.getLength(obj);
        for (int i = 0; i < len; i++) {
          Object o = Array.get(obj, i);
          consumer.accept(o, o.getClass());
        }
      }
    },
    ITERABLE {
      @Override
      public boolean traversable(Object o, Class<?> clazz) {
        return Iterable.class.isAssignableFrom(o.getClass());
      }
      @Override
      public void traverse(Class<?> clazz, Object obj, BiConsumer<Object, Class<?>> consumer) {
        for (Object o : (Iterable<?>) obj) {
          consumer.accept(o, o.getClass());
        }
      }
    },
    FALLBACK {
      @Override
      public boolean traversable(Object o, Class<?> clazz) {
        return true;
      }
      @Override
      public void traverse(Class<?> clazz, Object obj, BiConsumer<Object, Class<?>> consumer) {
        List<Field> fields = getAllFields(new ArrayList<>(), obj.getClass());
        for (Field field : fields) {
          if (skipField(field.getModifiers())) {
            continue;
          }
          Class<?> fieldType = field.getType();
          try {
            field.setAccessible(true);
            Object value = field.get(obj);
            // The object might be null here, so we use fieldType.
            consumer.accept(value, fieldType);
          } catch (IllegalAccessException e) {
            // Ignore the exception
          }
        }
      }
    };

    abstract void traverse(Class<?> clazz, Object obj, BiConsumer<Object, Class<?>> consumer);
    abstract boolean traversable(Object obj, Class<?> clazz);

  }

  private final transient Map<Object, Class<?>> visited = new IdentityHashMap<>();
  private final transient Queue<Object> toVisit = new ArrayDeque<>();

  private final transient List<T> matches = new ArrayList<>();
  private transient Function<Object, Boolean> objectMatcher;
  private final transient Object locker = new Object();

  /**
   * Scan the tree for a match.
   *
   * @param root the top of the tree
   * @return a collection of matching objects.
   */
  @Synchronized(value = "locker")
  public Collection<T> scan(Object root) {
    if (root == null) {
      return Collections.emptyList();
    }
    // Reset the state
    visited.clear();
    toVisit.clear();
    matches.clear();
    objectMatcher = objectMatcher();
    addIfNotVisited(root, root.getClass());
    doScan();
    return Collections.unmodifiableCollection(matches);
  }

  /**
   * Return the function doing the match.
   *
   */
  protected abstract Function<Object, Boolean> objectMatcher();

  private void addIfNotVisited(Object object, Class<?> clazz) {
    if (object != null && !visited.containsKey(object)) {
      toVisit.add(object);
      visited.put(object, clazz);
    }
  }

  private static Scanner handlerFor(Object o, Class<?> clazz) {
    Scanner result = null;
    for (Scanner h : Scanner.values()) {
      if (h.traversable(o, clazz)) {
        result = h;
        break;
      }
    }
    return Args.notNull(result, "handler-for-class");
  }

  private static List<Field> getAllFields(@NonNull List<Field> fields, @NonNull Class<?> clazz) {
    fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
    if (clazz.getSuperclass() != null) {
      getAllFields(fields, clazz.getSuperclass());
    }
    return Collections.unmodifiableList(fields);
  }

  private void doScan() {
    while (!toVisit.isEmpty()) {
      Object obj = toVisit.remove();
      Class<?> clazz = visited.get(obj);
      if (objectMatcher.apply(obj)) {
        matches.add((T) obj);
      }
      if (consideredPrimitive(clazz))
        continue;

      handlerFor(obj, clazz).traverse(clazz, obj, (o, c) -> addIfNotVisited(o, c));
    }
  }

  private static boolean consideredPrimitive(Class<?> clazz) {
    return BooleanUtils.or(new boolean[] {ClassUtils.isPrimitiveOrWrapper(clazz),
        String.class.isAssignableFrom(clazz)});
  }

  private static boolean skipField(int fieldModifier) {
    if ((fieldModifier & Modifier.STATIC) == Modifier.STATIC)
      return true;
    if ((fieldModifier & Modifier.TRANSIENT) == Modifier.TRANSIENT)
      return true;
    return false;
  }
}
