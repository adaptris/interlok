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

package com.adaptris.annotation;

/**
 * Annotation to create a standard BeanInfo java class for any given class.
 * <p>
 * Use this to force XStream to use the setters and getters rather than the member variables directly. This is generally useful if
 * the setters and getters have behaviour associated with them that are not simple <code>this.x = x</code>.
 * </p>
 * 
 * 
 */
public @interface GenerateBeanInfo {

}
