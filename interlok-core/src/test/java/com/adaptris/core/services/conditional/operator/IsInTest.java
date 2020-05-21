package com.adaptris.core.services.conditional.operator;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IsInTest {

  private IsIn operator ;

  private AdaptrisMessage message;

  @Before
  public void setUp() throws Exception {
    operator = new IsIn();
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }

  @Test
  public void testIsIn() {
    List<String> isInList = Arrays.asList("abc", "def", "ghi");
    operator.setValues(isInList);
    assertTrue(operator.apply(message, "abc"));
  }

  @Test
  public void testNotIn() {
    List<String> isInList = Arrays.asList("abc", "def", "ghi");
    operator.setValues(isInList);
    assertFalse(operator.apply(message, "xyz"));
  }

  @Test
  public void testNotInTrailingSpace() {
    List<String> isInList = Arrays.asList("abc", "def", "ghi");
    operator.setValues(isInList);
    assertFalse(operator.apply(message, "def "));
  }

  @Test
  public void testNotInLeadingSpace() {
    List<String> isInList = Arrays.asList("abc", "def", "ghi");
    operator.setValues(isInList);
    assertFalse(operator.apply(message, " ghi"));
  }
  @Test
  public void testNotInEmpty() {
    List<String> isInList = Arrays.asList("abc", "def", "ghi");
    operator.setValues(isInList);
    assertFalse(operator.apply(message, " "));
  }
  @Test
  public void testNotInEmptyArray() {
    assertFalse(operator.apply(message, "xyz"));
  }



}