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

package com.adaptris.http.test;



import java.util.Properties;



/**

 * @author lchan

 * @author $Author: lchan $

 */

class ProcessorConfig {



  private String className = "";

  private String url = "";

  private String id = "";

  private Properties config;

    

  ProcessorConfig(String propertyKey) {

    if (propertyKey.lastIndexOf(".") > 0) {

      id = propertyKey.substring(0, propertyKey.lastIndexOf("."));

    } else {

      id = propertyKey;

    }

    config = new Properties();

  }



  void set(String propertyKey, String value) {

    if (propertyKey.indexOf(".class") > 0) {

      className = value;

    } else if (propertyKey.indexOf(".url") > 0) {

      url = value;

    } else {

      int dot = propertyKey.lastIndexOf(".");

      if (dot > 0) {

        propertyKey = propertyKey.substring(dot + 1);

      }      

      config.setProperty(propertyKey, value);

    }

  }



  String getClassName() {

    return className;

  }



  String getId() {

    return id;

  }

  

  String getUrl() {

    return url;

  }

  

  

  Properties getConfig() {

    return config;

  }

    

  public boolean equals(Object o) {

    if (!this.getClass().equals(o.getClass())) {

      return false;

    } else {

      return id.equals(((ProcessorConfig) o).getId());

    }

  }



  public int hashCode() {

    return id.hashCode();

  }

}

