package com.adaptris.core.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import com.adaptris.core.AdapterLifecycleEvent;
import com.adaptris.core.EventNameSpaceConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * <code>AdapterLifecycleEvent</code> indicating that this Adapter's license is about to expire.
 * </p>
 * 
 * @config license-expiry-warning-event
 */
@XStreamAlias("license-expiry-warning-event")
public class LicenseExpiryWarningEvent extends AdapterLifecycleEvent {
  private static final long serialVersionUID = 2014012301L;

  private Date expiryDate;
  private String licenseExpiry;
  private transient SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public LicenseExpiryWarningEvent() {
    super(EventNameSpaceConstants.LICENSE_EXPIRY);
    setExpiryDate(new Date());
  }

  /** Set the date when this license expires.
   *
   * @param d the date.
   * @throws ParseException if the date string could not be parsed.
   */
  public void setLicenseExpiry(String d) throws ParseException {
    expiryDate = sdf.parse(d);
    licenseExpiry = d;
  }

  /** Get the date of the license expiry.
   *
   * @return the date.
   */
  public String getLicenseExpiry() {
    return sdf.format(expiryDate);
  }

  /** Get the expiry date as an actual Date object.
   * @return the expiry date.
   */
  public Date when() {
    Date result = (Date) expiryDate.clone();
    Date licenseExpiryDate = toDate(licenseExpiry);
    if (!DateUtils.isSameDay(expiryDate, licenseExpiryDate)) {
        // use the earlier date...
        result = (Date) min(expiryDate, licenseExpiryDate).clone();
    }
    return result;
  }

  /** Set the expiry date using an actual Date object.
   * @param d the expiry date
   */
  public void setExpiryDate(Date d) {
    expiryDate = (Date) d.clone();
    licenseExpiry = sdf.format(expiryDate);
  }

  public Date getExpiryDate() {
    return expiryDate;
  }

  private boolean matching(String licExpiry, Date expDate) {
    return sdf.format(expDate).equals(licExpiry);
  }

  private Date toDate(String s) {
    Date result = new Date();
    try {
      result = sdf.parse(s);
    }
    catch (ParseException e) {
      return new Date();
    }
    return result;
  }

  private static Date min(Date d1, Date d2) {
    return (d1.before(d2)) ? d1 : d2;
  }
}
