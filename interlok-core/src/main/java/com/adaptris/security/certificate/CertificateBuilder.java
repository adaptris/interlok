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

import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import com.adaptris.security.exc.AdaptrisSecurityException;

/**
 * The interface for the creation of certificates.
 * <p>
 * The default type of certificate is an X509 certificate, however, there are a number of considerations when using this interface
 * to generate self-signed certificates.
 * <p>
 * <ul>
 * <li>The Certificate is valid from "now" until 12months from now.</li>
 * <li>The unique serial id for the certificate is a random number between 0 and 10000</li>
 * <li>Subject unique ID or Issuer unique id is not set</li>
 * <li>No V3 extensions are added.</li>
 * <li>The base version of the certificate is 1</li>
 * </ul>
 * </p>
 * <p>
 * Example Use
 * </p>
 * 
 * <pre>
 * {@code 
    CertificateParameter cp = new CertificateParameter();
    X500NameBuilder subject = new X500NameBuilder();

    subject.addRDN(X509ObjectIdentifiers.countryName, "GB");
    subject.addRDN(X509ObjectIdentifiers.stateOrProvinceName, "Middlesex");
    subject.addRDN(X509ObjectIdentifiers.localityName, "Uxbridge");
    subject.addRDN(X509ObjectIdentifiers.organization, "Adaptris");
    subject.addRDN(X509ObjectIdentifiers.organizationalUnitName, "Development");
    subject.addRDN(X509ObjectIdentifiers.commonName, "My Name");
    subject.addRDN(PKCSObjectIdentifiers.pkcs_9_at_emailAddress, "myname@mycompany.com");
    cp.setSignatureAlgorithm("Md5WithRSAencryption");
    cp.setKeyAlgorithm("RSA", 2048);
    cp.setSubjectInfo(subject.build());
 
    // Now actually create the certificate
    CertificateBuilder cm = CertificateBuilderFactory.getInstance().createBuilder();
    cm.setCertificateParameters(cp);
    Certificate cert = cm.createSelfSignedCertificate();
 
    // Just print out some information.
    System.out.println(cert.toString());
    System.out.println(cm.createCertificateRequest());
    System.out.println(cm.getPublicKey());
    System.out.println(cm.getPrivateKey());
   }
 * </pre>
 * 
 * @see CertificateParameter
 * @see PublicKey
 * @see PrivateKey
 */
public interface CertificateBuilder {
  /**
   * Set the certificate parameters for this maker object.
   * <p>
   * The certificate parameters determine the x.500 information that goes into the certificate
   * </p>
   * 
   * @param cm the Certifcate Parameters
   * @see CertificateParameter
   */
  void setCertificateParameters(CertificateParameter cm);

  /**
   * Return the private key associated with the recently created certificate.
   * 
   * @return the private key, null if no certificate has been created
   */
  PrivateKey getPrivateKey();

  /**
   * Return the public key associated with the recently created certificate.
   * 
   * @return the public key, null if no certificate has been created
   */
  PublicKey getPublicKey();

  /**
   * Reset the internal state, ready to create a new certificate.
   * <p>
   * It must be invoked if attempting to create more one certificate using the same object. It does not necessarily have to be
   * called if this is the first time creating a certificate
   * </p>
   */
  void reset();

  /**
   * Create a self signed certificate, and write it to the supplied oututStream.
   * <p>
   * The default implementation writes out the certificate as a DER encoded ASN.1 data structure
   * </p>
   * 
   * @param out the OutputStream to write to
   * @throws AdaptrisSecurityException if any error occurs
   * @see AdaptrisSecurityException
   */
  void createSelfSignedCertificate(OutputStream out) throws AdaptrisSecurityException;

  /**
   * Create a self-signed certificate, and return it as a {@link java.security.cert.Certificate} object.
   * 
   * @return the created certificate
   * @throws AdaptrisSecurityException if any error occurs
   * @see AdaptrisSecurityException
   */
  Certificate createSelfSignedCertificate() throws AdaptrisSecurityException;
}
