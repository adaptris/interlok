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

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * Factory to build Certificate Handlers.
 * <p>
 * The only type of certificate handler supported is X509.
 * </p>
 */
public class CertificateHandlerFactory {

  private static CertificateHandlerFactory f = new CertificateHandlerFactory();

  protected CertificateHandlerFactory() {
  }

  /**
   * Get a factory for handling X509 certificates.
   * 
   * @return the instance.
   */
  public static CertificateHandlerFactory getInstance() {
    return f;
  }

  /**
   * Create a CertificateHandler instance from the specified bytes.
   * <p>
   * The byte array is expected to contain the certificate in either DER format
   * or PEM format. The contents of the byte array is expected to only contain a
   * single certificate.
   * 
   * @param bytes the bytes representing the certificate
   * @throws CertificateException if the certificate could not be parsed
   * @throws IOException if there was an IO error
   * @return a certificate Handler
   * @see CertificateHandler
   */
  public CertificateHandler generateHandler(byte[] bytes)
      throws CertificateException, IOException {
    return (new X509Handler(bytes));
  }

  /**
   * Create a CertificateHandler instance from a pre-existing Certificate.
   * 
   * @param c the Certificate
   * @throws CertificateException if the certificate could not be parsed
   * @throws IOException if there was an IO error
   * @return a certificate Handler
   * @see CertificateHandler
   */
  public CertificateHandler generateHandler(Certificate c)
      throws CertificateException, IOException {
    return (new X509Handler(c));
  }

  /**
   * Create a CertificateHandler instance from the supplied inputstream.
   * <p>
   * The inputstream is expected to contain the certificate in either DER format
   * or PEM format. The contents of the inputstream is expected to only contain
   * a single certificate.
   * 
   * @param i the inputstream containing the certificate
   * @throws CertificateException if the certificate could not be parsed
   * @throws IOException if there was an IO error
   * @return a certificate Handler
   * @see CertificateHandler
   */
  public final CertificateHandler generateHandler(InputStream i)
      throws CertificateException, IOException {
    return (new X509Handler(i));
  }
}
