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

package com.adaptris.core.management.jetty;

import java.util.Properties;

public class WebServerProperties {
  
  private static final String WEB_SERVER_PORT_CFG_KEY = "webServerPort";
  private static final String WEB_SERVER_PORT_DEFAULT = "8080";
  
  private static final String WEB_SERVER_CONFIG_FILE_NAME_CGF_KEY = "webServerConfigUrl";
  private static final String WEB_SERVER_CONFIG_FILE_NAME_DEFAULT = "jetty.xml";
   
  private static final String WEB_SERVER_WEBAPP_URL_CFG_KEY = "webServerWebappUrl";
  private static final String WEB_SERVER_WEBAPP_URL_DEFAULT = "webapps";
  
  private static final String WEB_SERVER_VENDOR_DEFAULT = WebServerVendor.JETTY.getValue();
  
  public static enum WebServerVendor {
    JETTY("jetty");
    
    private String value;

    WebServerVendor(String value) {
      this.setValue(value);
    }
    
    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }
  
  public static enum WebServerPropertiesEnum {
  
    PORT (WEB_SERVER_PORT_DEFAULT, WEB_SERVER_PORT_CFG_KEY),
    
    CONFIG_FILE(WEB_SERVER_CONFIG_FILE_NAME_DEFAULT, WEB_SERVER_CONFIG_FILE_NAME_CGF_KEY),

    WEBAPP_URL(WEB_SERVER_WEBAPP_URL_DEFAULT, WEB_SERVER_WEBAPP_URL_CFG_KEY);

    private String defaultValue;
    private String overridingBootstrapPropertyKey;

    WebServerPropertiesEnum(String defaultValue, String overridingBootstrapPropertyKey) {
      this.defaultValue = defaultValue;
      this.overridingBootstrapPropertyKey = overridingBootstrapPropertyKey;
    }
    
    public String getValue(Properties bootstrapProperties) {
      String propertyValue = bootstrapProperties.getProperty(this.getOverridingBootstrapPropertyKey());
      if(propertyValue == null)
        propertyValue = this.getDefaultValue();
      
      return propertyValue;
    }
    
    public String getValue(Properties bootstrapProperties, String defaultValue) {
      String propertyValue = bootstrapProperties.getProperty(this.getOverridingBootstrapPropertyKey());
      if(propertyValue == null)
        propertyValue = defaultValue;
      
      return propertyValue;
    }

    public String getDefaultValue() {
      return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
    }

    public String getOverridingBootstrapPropertyKey() {
      return overridingBootstrapPropertyKey;
    }

    public void setOverridingBootstrapPropertyKey(
        String overridingBootstrapPropertyKey) {
      this.overridingBootstrapPropertyKey = overridingBootstrapPropertyKey;
    }
    
  }
    
}
