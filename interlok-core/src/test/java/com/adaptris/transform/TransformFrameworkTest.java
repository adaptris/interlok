/*
 * Copyright 2018 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.adaptris.transform;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TransformFrameworkTest extends TransformFramework {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testIndexOfRule() throws Exception {
    TransformFramework tf = new TransformFrameworkTest();
    Source source = new Source(new StringReader("hello"));
    tf.addRule(source);
    assertEquals(0, tf.indexOfRule(source));
    assertEquals(1, tf.getNumRules());
  }

  @Test
  public void testRemove() throws Exception {
    TransformFramework tf = new TransformFrameworkTest();
    Source source = new Source(new StringReader("hello"));
    tf.addRule(source);
    tf.removeRule(source);
    assertEquals(0, tf.getNumRules());
    tf.addRule(source);
    tf.removeRule(0);
    assertEquals(0, tf.getNumRules());
    tf.addRule(source);
    tf.removeRules();
    assertEquals(0, tf.getNumRules());
  }

  @Override
  public void transform(Source in, Source rule, Target out) throws Exception {
  }

  @Override
  public void addRule(Source rule) throws Exception {
    ruleList.add(rule, rule);
  }

}
