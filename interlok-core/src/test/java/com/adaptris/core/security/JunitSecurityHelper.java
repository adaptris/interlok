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

package com.adaptris.core.security;

import java.io.File;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Properties;
import java.util.Random;

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;

import com.adaptris.security.certificate.CertificateBuilder;
import com.adaptris.security.certificate.CertificateBuilderFactory;
import com.adaptris.security.certificate.CertificateParameter;
import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.keystore.KeystoreProxy;
import com.adaptris.security.password.Password;
import com.adaptris.util.GuidGenerator;

public class JunitSecurityHelper {
  private static final Random random = new Random();

  public static final String KEYSTORE_URL = "security.keystoreUrl";
  public static final String KEYSTORE_PATH = "security.keystore.path";
  public static final String SECURITY_PASSWORD = "security.password";
  public static final String SECURITY_ALIAS = "security.alias";
  public static final String KEYSTORE_TYPE = "security.keystore.type";

  private Properties config;

  public JunitSecurityHelper(Properties p) {
    config = p;
  }

  public void createKeystore(String commonName) throws Exception {
    String ksUrl = config.getProperty(KEYSTORE_URL);
    newKeystore(ksUrl, commonName, Password.decode(config.getProperty(SECURITY_PASSWORD)).toCharArray());
  }

  /**
   * Create a new keystore.
   *
   * @return the URL of the keystore created.
   * @throws Exception
   */
  public String newKeystore() throws Exception {
    return newKeystore(String.valueOf(random.nextInt(1000)));
  }

  public String newKeystore(String commonName) throws Exception {
    File defaultKeystore = new File(config.getProperty(KEYSTORE_PATH));
    File keystoreDir = new File(defaultKeystore.getParentFile(), "keystores");
    keystoreDir.mkdirs();
    String ksType = config.getProperty(KEYSTORE_TYPE);
    String ksFilename = new GuidGenerator().safeUUID() + ".keystore";
    String ksUrl = "file:///" + keystoreDir.getCanonicalPath() + "/" + ksFilename + "?keystoreType=" + ksType;
    newKeystore(ksUrl, commonName, Password.decode(config.getProperty(SECURITY_PASSWORD)).toCharArray());
    return ksUrl;
  }

  private void newKeystore(String url, String commonName, char[] password) throws Exception {
    KeystoreProxy ksp = null;
    KeystoreLocation ksc = KeystoreFactory.getDefault().create(url,
        password);
    CertificateBuilder builder = getBuilder(commonName);
    Certificate selfCert = builder.createSelfSignedCertificate();
    PrivateKey privkey = builder.getPrivateKey();
    ksp = KeystoreFactory.getDefault().create(ksc);
    try {
      ksp.load();
    }
    catch (Exception e) {
      // Ignore the error...
    }
    String alias = config.getProperty(SECURITY_ALIAS);
    Certificate[] certChain = new Certificate[1];
    certChain[0] = selfCert;
    ksp.setPrivateKey(alias, privkey, password, certChain);
    ksp.commit();
  }

  public void createKeystore() throws Exception {
    String commonName = String.valueOf(random.nextInt(1000));
    createKeystore(commonName);
  }

  private static CertificateBuilder getBuilder(String commonName) throws Exception {

    CertificateBuilder builder = CertificateBuilderFactory.getInstance().createBuilder();
    CertificateParameter cp = new CertificateParameter();
    X500NameBuilder subject = new X500NameBuilder();
    subject.addRDN(X509ObjectIdentifiers.countryName, "GB");
    subject.addRDN(X509ObjectIdentifiers.stateOrProvinceName, "Middlesex");
    subject.addRDN(X509ObjectIdentifiers.localityName, "Uxbridge");
    subject.addRDN(X509ObjectIdentifiers.organization, "Adaptris");
    subject.addRDN(X509ObjectIdentifiers.organizationalUnitName, "JUNIT");
    subject.addRDN(X509ObjectIdentifiers.commonName, commonName);
    subject.addRDN(PKCSObjectIdentifiers.pkcs_9_at_emailAddress, "myname@adaptris.com");

    cp.setSignatureAlgorithm("SHA256WithRSAEncryption");
    // Changed to 1024 as the key size, otherwise jdk8_66 appears to have a fit
    // wrt to java.security limiting the certpath algorithms
    // jdk.certpath.disabledAlgorithms=MD2, RSA keySize < 1024 (it was like this in _40, but doesn't
    // apparently break things
    cp.setKeyAlgorithm("RSA", 1024);
    cp.setSubjectInfo(subject.build());
    builder.setCertificateParameters(cp);
    return builder;
  }

}
