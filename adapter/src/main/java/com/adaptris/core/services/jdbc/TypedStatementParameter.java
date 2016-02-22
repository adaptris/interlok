/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.core.services.jdbc;

/**
 * Abstract class preserving backwards config compatibility from {@link StatementParameter}.
 * 
 * @author lchan
 *
 */
public abstract class TypedStatementParameter extends StatementParameterImpl {

  // ~This is just here to avoid @XStream unmarshalling errors, due to the hierarchy change.
  // Should never be output as it should be null; doesn't have a getter/setter so the UI doesn't know about it.
  // Eventually we can remove this and sub-classes can just extend StatementParameterImpl directly.
  private String queryClass;

  public TypedStatementParameter() {
    super();
  }

  public TypedStatementParameter(String query, QueryType type, Boolean nullConvert, String name) {
    super(query, type, nullConvert, name);
  }


}
