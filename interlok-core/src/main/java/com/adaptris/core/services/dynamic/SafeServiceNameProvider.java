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

package com.adaptris.core.services.dynamic;

import com.adaptris.annotation.Removal;
import com.adaptris.core.CoreException;
import com.adaptris.core.TradingRelationship;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Extension of <code>DefaultServiceNameProvider</code> which strips the following characters from
 * any component of the Trading Relationship:
 * <p>
 * <code>/,\,?,*,:,|, ,&amp;,",&lt;,&gt;,'</code>
 * </p>
 * Of particular use for ebXML where it is feasible that URLs might be used to identify the parties
 * or message type.
 * 
 * @config safe-service-name-provider
 * 
 * @author Stuart Ellidge
 * @deprecated since 3.8.4 use {@link DynamicServiceExecutor} with a URL based
 *             {@link ServiceExtractor} instead.
 * 
 */
@Deprecated
@XStreamAlias("safe-service-name-provider")
@Removal(version = "3.11.0")
public final class SafeServiceNameProvider extends DefaultServiceNameProvider {
  public SafeServiceNameProvider() {
	super();
  }

  @Override
  protected String retrieveName(TradingRelationship t) throws CoreException {
    Args.notNull(t, "tradingRelationship");


    String name = super.retrieveName(t);
    name = name.replaceAll("\\/", "");
    name = name.replaceAll("\\\\", "");
    name = name.replaceAll("\\?", "");
    name = name.replaceAll("\\*", "");
    name = name.replaceAll("\\:", "");
    name = name.replaceAll(" ", "");
    name = name.replaceAll("\\|", "");
    name = name.replaceAll("&", "");
    name = name.replaceAll("\\\"", "");
    name = name.replaceAll("\\'", "");
    name = name.replaceAll("<", "");
    name = name.replaceAll(">", "");
    return name;
  }
}
