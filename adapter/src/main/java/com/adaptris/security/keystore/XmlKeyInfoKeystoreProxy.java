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

package com.adaptris.security.keystore;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.exc.KeystoreException;

/**
 *
 * Keystore Proxy implementation that only handles a single XML KeyInfo element.
 *
 * @author $Author: lchan $
 */
class XmlKeyInfoKeystoreProxy extends SingleEntryKeystoreProxy {

  private X509Certificate x509Certificates[] = null;

  /**
   * Default Constructor.
   */
  public XmlKeyInfoKeystoreProxy() {
  }

  /**
   * Construct the object using the KeyStoreInfo object.
   *
   * @param k the KeyStoreInfo object
   * @see KeystoreLocation
   * @throws AdaptrisSecurityException if an error is encountered
   */
  public XmlKeyInfoKeystoreProxy(KeystoreLocation k)
                                                    throws AdaptrisSecurityException {
    this();
    setKeystoreLocation(k);
  }

  /**
   * Load the keystore.
   * <p>
   * Load the keystore ready for operations upon it
   * </p>
   *
   * @throws AdaptrisSecurityException if there was an error reading the
   *           contents of the keystore
   * @throws IOException if the keystore is not found
   */
  public void load() throws AdaptrisSecurityException, IOException {

    InputStream in = getKeystoreLocation().openInput();
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactoryBuilder.newInstance().configure(DocumentBuilderFactory.newInstance());
      dbf.setNamespaceAware(true);
      Document doc = dbf.newDocumentBuilder().parse(in);
      loadCertificates(doc);
    }
    // TODO add back this code one Cobertura can handle Java7 multicatch
    // catch (SAXException|ParserConfigurationException|MarshalException e) {
    // throw new KeystoreException(e);
    // }
    catch (SAXException e) {
      throw new KeystoreException(e);
    } catch (ParserConfigurationException e) {
      throw new KeystoreException(e);
    } catch (MarshalException e) {
      throw new KeystoreException(e);
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (Exception ignored) {
        ;
      }
    }
    return;
  }

  /**
   * Obtains all certificates from the given XML document. It is expected that
   * the xml document contain a KeyInfo element.
   *
   * @param doc - xml document to parse
   * @throws MarshalException - if we failed to unmarshall the xml keyinfo
   * @throws SAXException - if the KeyInfo element is not found
   */
  private void loadCertificates(Document doc) throws MarshalException,
                                             SAXException {
    List<X509Certificate> myCerts = new ArrayList<X509Certificate>();

    // Find KeyInfo element within XML file
    NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "KeyInfo");
    if (nl.getLength() == 0) {
      throw new SAXException("Cannot find KeyInfo element");
    }

    // Create a DOM KeyInfoFactory that will be used to unmarshal the
    // document containing the XML KeyInfo element
    KeyInfoFactory kfac = KeyInfoFactory.getInstance("DOM");

    // unmarshal the xml data into an object
    KeyInfo keyInfo = kfac.unmarshalKeyInfo(new DOMStructure(nl.item(0)));

    // Iterate through the keyinfo data an store any and all X509 certificates
    @SuppressWarnings("rawtypes")
    Iterator iter = keyInfo.getContent().iterator();
    while (iter.hasNext()) {
      XMLStructure kiType = (XMLStructure) iter.next();
      if (kiType instanceof X509Data) {
        X509Data xd = (X509Data) kiType;
        Object[] entries = xd.getContent().toArray();
        for (int i = 0; i < entries.length; i++) {
          if (entries[i] instanceof X509Certificate) {
            myCerts.add((X509Certificate) entries[i]);
          }
        }
      }
    }
    // Convert the list to an array
    x509Certificates = myCerts.toArray(new X509Certificate[0]);
  }

  /**
   * Method to extract a Partner's Private Key from their Keystore entry and
   * return a PrivateKey object to the caller.
   *
   * @param alias the alias in the keystore
   * @param keyPassword the associated password
   * @return the requested private key, or null if the alias does not exist/not
   *         a key entry
   * @throws AdaptrisSecurityException for any error
   */
  public PrivateKey getPrivateKey(String alias, char[] keyPassword)
                                                                   throws AdaptrisSecurityException {

    throw new KeystoreException(this.getClass() + " does not handle Private Keys");
  }

  /**
   * Return the certificate specified by the given alias.
   *
   * @param alias the alias of the Certificate
   * @return Certificate the requested certificate, or null if the alias does
   *         not exist/not a certificate
   * @throws AdaptrisSecurityException for any error
   */
  public Certificate getCertificate(String alias)
                                                 throws AdaptrisSecurityException {
    X509Certificate cert = null;
    if (getAliasName().equalsIgnoreCase(alias)) {
      cert = getFirstCertificate();
    } else {
      throw new KeystoreException(alias + " not found");
    }
    return cert;
  }

  /**
   * Returns the first certificate from the array of certificates or null. Adds
   * null protection checks.
   *
   * @return
   */
  private X509Certificate getFirstCertificate() {
    if (x509Certificates != null && x509Certificates.length > 0) {
      return x509Certificates[0];
    }
    return null;
  }

  /**
   * Return the certificate specified by the given alias.
   *
   * @param alias the alias of the Certificate
   * @return requested certificate chain, or null if the alias does not
   *         exist/not a certificate
   * @throws AdaptrisSecurityException for any error
   */
  public Certificate[] getCertificateChain(String alias)
                                                        throws AdaptrisSecurityException {
    X509Certificate[] cert = null;
    if (getAliasName().equalsIgnoreCase(alias)) {
      cert = x509Certificates;
    } else {
      throw new KeystoreException(alias + " not found");
    }
    return cert;
  }

  @Override
  protected KeyStore buildTemporaryKeystore() {
    KeyStore ks = null;
    try {
      ks = KeyStore.getInstance(DEFAULT_KS_IMPL);
      ks.load(null, null);
      ks.setCertificateEntry(getAliasName(), getFirstCertificate());
    } catch (Exception e) {
      logR.warn("Failed to create a temporary keystore [" + e.getMessage()
          + "], returning null");
      ks = null;
    }
    return ks;
  }

}
