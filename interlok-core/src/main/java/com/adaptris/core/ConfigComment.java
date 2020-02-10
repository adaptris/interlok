/*
 * Copyright 2019 Adaptris Ltd.
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
package com.adaptris.core;

/**
 * Provides the ability for the UI to have <strong>notes</strong> associated with an configuration item.
 * <p>
 * This has no purpose at runtime but will enable alternate behaviours within the UI.
 * </p>
 * 
 * @since 3.9.3
 */
public interface ConfigComment {

  void setComments(String s);

  String getComments();
}
