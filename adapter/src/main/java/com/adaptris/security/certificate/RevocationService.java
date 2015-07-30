/*
 * $Author: lchan $
 * $RCSfile: RevocationService.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/01/27 09:25:12 $
 */
package com.adaptris.security.certificate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.exc.CertException;
import com.adaptris.security.util.Constants;

/**
 * Abstract class representing a service that checks X509Certificates against an external source for revocations.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class RevocationService {

  protected transient Logger logR = LoggerFactory.getLogger(RevocationService.class);

  private static CrlRevocationService crlRev = new CrlRevocationService();

  protected RevocationService() {
  }

  /**
   * Get the default instance of the RevocationService.
   *
   * @return the default instance
   */
  public static RevocationService getDefaultInstance() {
    return crlRev;
  }

  /**
   * Check if the specified certification has been revoked.
   *
   * @param cert the X509 Certificate
   * @return true if the cert has been revoked
   * @throws AdaptrisSecurityException wrapping the underlying exception
   * @see com.adaptris.security.util.Constants#REV_CHECK_MUST_COMPLETE
   *
   */
  public abstract boolean isRevoked(X509Certificate cert) throws AdaptrisSecurityException;

  /**
   * Get the last time this certificate was checked.
   *
   * @param cert the certificate to check
   * @return the Calendar representing the time.
   */
  public abstract Calendar getLastRevocationCheck(X509Certificate cert);

  /**
   * Perform revocation checks against a CRL.
   *
   *
   */
  private static class CrlRevocationService extends RevocationService {
    private List<Entry> certArray;

    CrlRevocationService() {
      certArray = new ArrayList<Entry>();
    }

    /**
     *
     * @see RevocationService#isRevoked(X509Certificate)
     */
    @Override
    public synchronized boolean isRevoked(X509Certificate cert) throws AdaptrisSecurityException {

      boolean rc = false;
      try {
        Entry entry = this.queryCachedEntries(cert);
        if (entry.needsChecking()) {
          if (Constants.DEBUG) {
            logR.trace("Actual Revocation check forced for SerialNumber:" + cert.getSerialNumber());
          }
          this.setRevocationStatus(entry);
          entry.setChecked();
        }
        else {
          if (Constants.DEBUG) {
            logR.trace("Using cached Revocation status for Serial Number:" + cert.getSerialNumber());
          }
        }
        rc = entry.isRevoked();
      }
      catch (Exception e) {
        if (Boolean.valueOf(System.getProperty(Constants.REV_CHECK_MUST_COMPLETE, "true"))) {
          throw new CertException("Could not check CRL for SerialNumber: " + cert.getSerialNumber(), e);
        } else {
          logR.debug("Ignoring " + e.getMessage() + " during revocation checks");
        }
      }
      return rc;
    }

    /**
     *
     * @see RevocationService#getLastRevocationCheck(X509Certificate)
     */
    @Override
    public synchronized Calendar getLastRevocationCheck(X509Certificate cert) {
      Entry entry = this.queryCachedEntries(cert);
      return entry.getCheckDate();
    }

    /**
     * Add an entry to the internal list or return the already existing entry.
     *
     * @param cert the certificate
     * @return the latest addition to the cache, or the already existing entry
     */
    private Entry queryCachedEntries(X509Certificate cert) {

      Entry entry = new Entry(cert);
      if (certArray.contains(entry)) {
        entry = (Entry) certArray.get(certArray.indexOf(entry));
      }
      else {
        certArray.add(entry);
      }
      return entry;
    }

    /**
     * Do the actual revocation check.
     *
     * @param entry the entry to check for revocation
     * @throws Exception if an exception occurred.
     */
    private void setRevocationStatus(Entry entry) throws Exception {

      boolean revocation = false;
      X509Certificate certificate = entry.getCertificate();
      List<String> crlDistributionPoints = getCrlDistributionPoints(certificate);

      logR.debug("Checking CRL information for SerialNumber: " + certificate.getSerialNumber());

      for (String dp : crlDistributionPoints) {
        X509CRL x509crl = getCRL(dp);
        // once revoked, always revoked
        revocation = revocation ? revocation : x509crl.isRevoked(entry.getCertificate());
        if (revocation) {
          X509CRLEntry crlEntry = x509crl.getRevokedCertificate(entry.getCertificate());
          logR.error("This certificate SerialNumber: " + entry.getCertificate().getSerialNumber() + " was revoked on "
              + crlEntry.getRevocationDate());
        }
      }

      entry.setRevoked(revocation);
      return;
    }

    /**
     * Extract a list of CRL URLs from the certificate.
     * 
     * @param cert The certificate
     * @return A list of CRL URL Strings
     * @throws IOException Thrown if details cannot be parsed from the certificate
     */
    private List<String> getCrlDistributionPoints(X509Certificate cert) throws IOException {
        List<String> crlUrls = new ArrayList<>();
        ASN1InputStream in1 = null;
        ASN1InputStream in2 = null;
        try {
          byte[] crldpExt = cert.getExtensionValue(Extension.cRLDistributionPoints.getId());
          if (crldpExt != null) {
            in1 = new ASN1InputStream(new ByteArrayInputStream(crldpExt));
            byte[] bytes = ((DEROctetString) in1.readObject()).getOctets();
            in2 = new ASN1InputStream(new ByteArrayInputStream(bytes));
            CRLDistPoint distPoint = CRLDistPoint.getInstance(in2.readObject());
            for (DistributionPoint dp : distPoint.getDistributionPoints()) {
              DistributionPointName dpn = dp.getDistributionPoint();
              if (dpn != null && dpn.getType() == DistributionPointName.FULL_NAME) {
                for (GeneralName generalName : GeneralNames.getInstance(dpn.getName()).getNames()) {
                  if (generalName.getTagNo() == GeneralName.uniformResourceIdentifier) {
                    crlUrls.add(DERIA5String.getInstance(generalName.getName()).getString());
                  }
                }
              }
            }
          }
        }
        finally {
          if (in1 != null) {
            try {
              in1.close();
            }
            catch (IOException ignore) {
              ;
            }
          }
          if (in2 != null) {
            try {
              in2.close();
            }
            catch (IOException ignore) {
              ;
            }
          }
        }
        return crlUrls;
      }

    /**
     * Get the CRL from the specified URL
     *
     * @param urlString the url
     * @throws Exception if an error occured
     * @return a X509CRL
     */
    private X509CRL getCRL(String urlString) throws Exception {
      try (InputStream crlStream = new URL(urlString).openStream()) {
        return (X509CRL) CertificateFactory.getInstance("X.509").generateCRL(crlStream);
      }
    }
  }

  /**
   * A private class that stores information about an entry in the revocation service.
   * <p>
   * One day we might want to write all these to store, so implement serializable just in case. Not that I like to serialize java
   * objects, but this one could be...
   *
   * @author $Author: lchan $
   */
  private static class Entry implements Serializable {

    /**
     * the serial version UID for Serialization
     *
     */
    public static final long serialVersionUID = 2005060301L;
    private X509Certificate thisCert;
    private Calendar expectedCheck;
    private Calendar lastCheck;
    private boolean revoked;

    /** Default Constructor */
    private Entry() {
      expectedCheck = null;
      lastCheck = null;
      revoked = false;
    }

    /**
     * Constructor
     *
     * @param c the Certificate
     */
    Entry(X509Certificate c) {
      this();
      thisCert = c;
    }

    /**
     * Get the last known revocation status for this entry.
     *
     * @return true if the certificate was revoked
     */
    boolean isRevoked() {
      return revoked;
    }

    /**
     * Set the revocation status for this entry.
     *
     * @param b true or false
     */
    void setRevoked(boolean b) {
      revoked = b;
    }

    /**
     * Should the CertifcateService check for revocation now.
     *
     * @return true if expectedCheck is earlier than right now or null
     */
    boolean needsChecking() {
      if (expectedCheck == null) {
        return true;
      }

      Calendar now = Calendar.getInstance();
      return now.after(expectedCheck);
    }

    /**
     * Set the next expected check for this entry.
     * <p>
     * Currently forced to be checked once a day...
     */
    void setChecked() {
      lastCheck = Calendar.getInstance();
      expectedCheck = Calendar.getInstance();

      int day = expectedCheck.get(Calendar.DAY_OF_YEAR);

      // what about yearly roll-over, this should handle it I hope...
      // But then again, there are 366 days in a leap year.
      if (day >= 365) {
        expectedCheck.set(Calendar.DAY_OF_YEAR, 1);
        expectedCheck.add(Calendar.YEAR, 1);
      }
      else {
        expectedCheck.add(Calendar.DAY_OF_YEAR, 1);
      }
      return;
    }

    /**
     * Return the X509 certificate
     *
     * @return the certificate
     */
    X509Certificate getCertificate() {
      return thisCert;
    }

    /**
     * Get the date this was last checked.
     *
     * @return the last date this entry was checked
     */
    Calendar getCheckDate() {
      return lastCheck;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Entry)) {
        return false;
      }

      X509Certificate c = ((Entry) o).getCertificate();
      return thisCert.equals(c);
    }

    /**
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      return thisCert.hashCode();
    }
  }

}