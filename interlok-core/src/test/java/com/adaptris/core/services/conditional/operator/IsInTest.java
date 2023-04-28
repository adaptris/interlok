package com.adaptris.core.services.conditional.operator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;

public class IsInTest {

  private IsIn operator ;

  private AdaptrisMessage message;

  @BeforeEach
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
  
  @Test
  public void testToString() {
    assertTrue(operator.toString().contains("is in"));
  }
}