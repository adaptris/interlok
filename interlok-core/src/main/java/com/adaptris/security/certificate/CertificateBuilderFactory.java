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

package com.adaptris.security.certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.util.SecurityUtil;

/** Another factory for building certificates.
 *  <p>Only X.509 certificates are supported, but in the future, maybe more
 *  who knows</p>
 */
public final class CertificateBuilderFactory {

  protected transient static final Logger log = LoggerFactory.getLogger(CertificateBuilderFactory.class.getName());
  
  private static CertificateBuilderFactory f = new CertificateBuilderFactory();

  private CertificateBuilderFactory() {
    SecurityUtil.addProvider();
  }

  /** Get the instance of the CertificateBuilderFactory.
   * 
   * @return a certificate builder.
   */
  public static CertificateBuilderFactory getInstance() {
    return f;
  }

  /** Return the default instance of a Certificatebuilder.
   *  <p>The default instance is one suitable for X.509 certificates
   *  @see CertificateBuilder
   *  @return something to build certificates with
   */
  public CertificateBuilder createBuilder() {
    return new X509Builder();
  }

}
