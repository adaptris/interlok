/*
 * Copyright 2015 Adaptris Ltd.
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

package com.adaptris.core.transform;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairList;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ResolvableExpressionParameterTest extends TransformParameterCase
{
	private static final String CONTENT = "When you see a corona light, it means that a Pilsner meditates. Sometimes some Sam Adams for another bud light procrastinates, but a Lone Star near the Ellis Island IPA always writes a love letter to a Harpoon around the Kashmir IPA! The bill dumbly buries an air hocky table behind a wanker. A line dancer, a radioactive Citra Ninja, and some Hops Alligator Ale are what made America great! An incinerated Kashmir IPA barely requires assistance from the St. Pauli Girl.";
	public static final String EXPRESSION = "%message{%payload}";

	private AdaptrisMessage message;
	private KeyValuePairList expressions;
	private Map<Object, Object> existing;

	@Before
	public void setUp() throws Exception
	{
		message = createMessage();
		message.setContent(CONTENT, "UTF-8");

		expressions = new KeyValuePairList();
		expressions.add(new KeyValuePair("beer", CONTENT));

		existing = new HashMap<>();
		existing.put("key", "value");
	}

	@Test
	public void test1() throws Exception
	{
		ResolvableExpressionParameter p = new ResolvableExpressionParameter();
		p.setExpressions(expressions);
		assertEquals(expressions, p.getExpressions());
	}

	@Test
	public void test2() throws Exception
	{
		ResolvableExpressionParameter p = new ResolvableExpressionParameter();
		p.setExpressions(expressions);
		Map map = p.createParameters(message, null);
		assertNotNull(map);
		assertEquals(1, map.size());
		assertTrue(map.containsKey("beer"));
		assertEquals(CONTENT, map.get("beer"));
	}

	@Test
	public void test3() throws Exception
	{
		ResolvableExpressionParameter p = new ResolvableExpressionParameter();
		p.setExpressions(expressions);
		Map map = p.createParameters(message, existing);
		assertEquals(2, map.size());
	}
}
