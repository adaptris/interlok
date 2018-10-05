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

package com.adaptris.naming.adapter;


public class NamingEntry {
  
  private String name;
  
  private Object object;
  
  private NamingEntryType type;
  
  public NamingEntry() {
  }
  
  public NamingEntry(String name, Object object, NamingEntryType type) {
    this.setName(name);
    this.setObject(object);
    this.setType(type);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Object getObject() {
    return object;
  }

  public void setObject(Object object) {
    this.object = object;
  }

  public NamingEntryType getType() {
    return type;
  }

  public void setType(NamingEntryType type) {
    this.type = type;
  }

}
