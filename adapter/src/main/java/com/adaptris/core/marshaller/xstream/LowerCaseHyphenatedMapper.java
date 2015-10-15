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

package com.adaptris.core.marshaller.xstream;

import static com.adaptris.core.marshaller.xstream.XStreamUtils.createParentFields;
import static com.adaptris.core.marshaller.xstream.XStreamUtils.setContainsAnyOf;
import static com.adaptris.core.marshaller.xstream.XStreamUtils.toFieldName;
import static com.adaptris.core.marshaller.xstream.XStreamUtils.toXmlElementName;

import java.util.Collection;
import java.util.Set;

import com.adaptris.annotation.AnnotationConstants;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * XStream Mapper that aids in name format conversion and handling of implicit
 * collections.
 * 
 * @author bklair
 */
public class LowerCaseHyphenatedMapper extends MapperWrapper {

  private transient Set<String> xstreamImplicits;

  public LowerCaseHyphenatedMapper(Mapper wrapped, Set<String> pXstreamImplicits) {
    super(wrapped);
    this.xstreamImplicits = pXstreamImplicits;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public String realMember(Class type, String serialized) {
    // redmineID #4508, check for class#xml-name in your list of xstreamimplicit fields...
    String javaFieldName = serialized;
    Collection<String> checks = createParentFields(type, serialized,
        AnnotationConstants.STANDARD_FIELD_SEPARATOR);
    if (!setContainsAnyOf(xstreamImplicits, checks)) {
      javaFieldName = toFieldName(serialized);
    }
    return super.realMember(type, javaFieldName);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public String serializedMember(Class type, String memberName) {
    // redmineID #4508 - Not entirely sure what to do here; if they've
    // used xstreamImplicit on it... we should be ok, because it should have
    // removed the camel
    // casiness.
    String serializedName = toXmlElementName(memberName);
    return super.realMember(type, serializedName);
  }

}
