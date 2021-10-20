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

package com.adaptris.taglet;

import java.util.Map;
import jdk.javadoc.doclet.Taglet;

/**
 * Simple taglet that allows us to quickly specify the config requirements.
 *
 * @deprecated since 4.2.0, we have switched to using annotations instead.
 */
@Deprecated(forRemoval = true, since = "4.2.0")
@SuppressWarnings("deprecation")
public class ConfigTaglet extends AbstractTaglet {
  private static final String NAME = "config";
  private static final String START = " <p>In the adapter configuration file this class is aliased as <b>";
  private static final String END = "</b> which is the preferred alternative to the fully qualified classname "
      + "when building your configuration.</p>";
  /**
   * Return the name of this custom tag.
   */
  @Override
  public String getName() {
      return NAME;
  }

  @Override
  public String getStart() {
    return START;
  }

  @Override
  public String getEnd() {
    return END;
  }

  @SuppressWarnings("unchecked")
  public static void register(Map tagletMap) {
    ConfigTaglet tag = new ConfigTaglet();
     Taglet t = (Taglet) tagletMap.get(tag.getName());
     if (t != null) {
         tagletMap.remove(tag.getName());
     }
     tagletMap.put(tag.getName(), tag);
  }

}
