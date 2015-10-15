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

package com.adaptris.util.license;



/**
 * The License interface.
 */
public interface License {
  public static enum LicenseType {
    Restricted, Basic, Standard, Enterprise;
  }

  /**
   * Check if this particular license type is enabled in the license.
   * 
   * @param type the type.
   * @return true if enabled.
   */
  boolean isEnabled(LicenseType type);
  
  /**
   * Verify the license.
   * 
   * @throws LicenseException if a fatal error occurs querying the license.
   */
  void verify() throws LicenseException;

  /**
   * Has the license expired.
   * 
   * @return true if the license has expired.
   * @throws LicenseException if there was error querying the license.
   */
  boolean hasExpired() throws LicenseException;

  /**
   * Get the date the license expires.
   * 
   * @return the date the license expires.
   * @throws LicenseException if there was an error querying the license.
   */
  java.util.Date getExpiry() throws LicenseException;

}
