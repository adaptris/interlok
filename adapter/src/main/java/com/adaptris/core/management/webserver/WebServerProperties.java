package com.adaptris.core.management.webserver;

import java.util.Properties;

public class WebServerProperties {
  
  private static final String WEB_SERVER_PORT_CFG_KEY = "webServerPort";
  private static final String WEB_SERVER_PORT_DEFAULT = "8080";
  
  private static final String WEB_SERVER_CONFIG_FILE_NAME_CGF_KEY = "webServerConfigUrl";
  private static final String WEB_SERVER_CONFIG_FILE_NAME_DEFAULT = "jetty.xml";
  
  private static final String WEB_SERVER_HOST_NAME_CFG_KEY = "webServerHostName";
  private static final String WEB_SERVER_HOST_NAME_DEFAULT = "localhost";
  
  private static final String WEB_SERVER_HOST_IP_CFG_KEY = "webServerHostIP";
  private static final String WEB_SERVER_HOST_IP_DEFAULT = "127.0.0.1";
  
  private static final String WEB_SERVER_WEBAPP_URL_CFG_KEY = "webServerWebappUrl";
  private static final String WEB_SERVER_WEBAPP_URL_DEFAULT = "webapps";
  
  private static final String WEB_SERVER_BASE_DIR_CFG_KEY = "webServerBaseDir";
  private static final String WEB_SERVER_BASE_DIR_DEFAULT = "WebServerBase";
  
  private static final String WEB_SERVER_VENDOR_CFG_KEY = "webServerVendor";
  private static final String WEB_SERVER_VENDOR_DEFAULT = WebServerVendor.JETTY.getValue();
  
  public static enum WebServerVendor {
    JETTY("jetty");
    
    WebServerVendor(String value) {
      this.setValue(value);
    }
    
    private String value;

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
    
    HOST_NAME(WEB_SERVER_HOST_NAME_DEFAULT, WEB_SERVER_HOST_NAME_CFG_KEY),
    
    HOST_IP(WEB_SERVER_HOST_IP_DEFAULT, WEB_SERVER_HOST_IP_CFG_KEY),
    
    WEBAPP_URL(WEB_SERVER_WEBAPP_URL_DEFAULT, WEB_SERVER_WEBAPP_URL_CFG_KEY),
    
    BASE_DIR(WEB_SERVER_BASE_DIR_DEFAULT, WEB_SERVER_BASE_DIR_CFG_KEY),
    
    VENDOR(WEB_SERVER_VENDOR_DEFAULT, WEB_SERVER_VENDOR_CFG_KEY);

    WebServerPropertiesEnum(String defaultValue, String overridingBootstrapPropertyKey) {
      this.defaultValue = defaultValue;
      this.overridingBootstrapPropertyKey = overridingBootstrapPropertyKey;
    }
    
    private String defaultValue;
    private String overridingBootstrapPropertyKey;
    
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
