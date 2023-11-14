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

package com.adaptris.util.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class HashTranslatorTest {
	
	@Test
	public void testGenerateHashCode() {
		String string1 = new String("test");
		String string2 = new String("test");

		assertEquals(string1.hashCode(), HashTranslator.getHashCode(string2));
	}
	
	@Test
	public void testGenerateHashCodeNotEquals() {
		String string1 = new String("test");
		String string2 = new String("doesn't match");

		assertNotEquals(string1.hashCode(), HashTranslator.getHashCode(string2));
	}
	
	@Test
	public void testGenerateHashCodeNull() {
		Object obj1 = null;
		
		assertEquals(0, HashTranslator.getHashCode(obj1));
	}

}
