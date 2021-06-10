package com.adaptris.core.util;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.util.ObjectScanner;
import lombok.Getter;
import lombok.Setter;

public class ObjectScannerTest {


  @Test
  public void testScan_Array() throws Exception {
    LeafScanner scanner = new LeafScanner();
    Flower root = new Flower(10, 10);
    Object[] children = root.getChildren().toArray();
    Collection<Leaf> matched = scanner.scan(children);
    assertEquals(10, matched.size());
  }

  @Test
  public void testScan_Nested() throws Exception {
    LeafScanner scanner = new LeafScanner();
    Flower root = new Flower(10, 10);
    Collection<Leaf> matched = scanner.scan(root);
    assertEquals(10, matched.size());
  }

  @Test
  public void testScan_Nested_SelfReferential() throws Exception {
    LeafScanner scanner = new LeafScanner();
    SelfReferential root = new SelfReferential(10, 10);
    Collection<Leaf> matched = scanner.scan(root);
    assertEquals(10, matched.size());
  }

  @Test
  public void testScan_Null() throws Exception {
    LeafScanner scanner = new LeafScanner();
    assertEquals(0, scanner.scan(null).size());
  }

  // Object tree that has self referential items.
  // And obvious identity duplicates of each other.
  // So we use this to check that toBeVisted/visited is fit for purpose.
  private class SelfReferential {
    @Getter
    @Setter
    private List<Object> children = new ArrayList<>();

    public SelfReferential(int petals, int leaves) {
      Flower p = new Flower(petals, leaves);
      p.getChildren().add(p);
      children.add(p);
      children.add(p);
      children.addAll(p.getChildren());
    }
  }


  private class Flower {
    @Getter
    @Setter
    private List<Object> children = new ArrayList<>();
    private static final String CONSTANT = "";
    @Getter
    @Setter
    private String someString;
    @Getter
    @Setter
    private int somePrimitive;

    public Flower(int petals, int leaves) {
      for (int i = 0; i < petals; i++) {
        children.add(new Petal());
      }
      for (int i = 0; i < leaves; i++) {
        children.add(new Leaf());
      }
    }
  }

  private class Petal {
    private transient Logger logger = LoggerFactory.getLogger(ObjectScannerTest.class);
    private static final String PETAL = "";
  }

  private class Leaf {
    private transient Logger logger = LoggerFactory.getLogger(ObjectScannerTest.class);
    private static final String LEAF = "";
  }

  private class LeafScanner extends ObjectScanner<Leaf> {

    @Override
    protected Function<Object, Boolean> objectMatcher() {
      return (object) -> Leaf.class.isAssignableFrom(object.getClass());
    }
  }
}
