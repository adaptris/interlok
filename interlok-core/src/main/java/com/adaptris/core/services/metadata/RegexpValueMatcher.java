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
package com.adaptris.core.services.metadata;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairBag;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Match the metadata value against a regular expression for {@link MetadataValueBranchingService}.
 * 
 * <p>
 * The key portion of the mappings is the regular expression; the value portion is the intended service id.
   <pre>
   {@code
    <metadata-value-branching-service>
     <unique-id>CheckEmailDomain</unique-id>
     <metadata-key>MetadataKey_Containing_an_Email</metadata-key>
     <default-service-id>Not_Google_Apple_Microsoft</default-service-id>
     <value-matcher class="regex-value-matcher"/>
     <metadata-to-service-id-mappings>
      <key-value-pair>
       <key>.*google.com$</key>
       <value>FromGmail</value>
      </key-value-pair>
      <key-value-pair>
       <key>.*microsoft.com$</key>
       <value>FromMicrosoft</value>
      </key-value-pair>
      <key-value-pair>
       <key>.*apple.com$</key>
       <value>FromApple</value>
      </key-value-pair>
     </metadata-to-service-id-mappings>
    </metadata-value-branching-service>
   }
   </pre>
 * </p>
 * @config regex-value-matcher
 */
@XStreamAlias("regex-value-matcher")
public class RegexpValueMatcher implements MetadataValueMatcher {

  @Override
  public String getNextServiceId(String metadataValue, KeyValuePairBag mappings) {
    String result = null;
    for (KeyValuePair k : mappings) {
      if (metadataValue.matches(k.getKey())) {
        result = k.getValue();
        break;
      }
    }
    return result;
  }


}
