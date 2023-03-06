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

package com.adaptris.core.event;

import com.adaptris.util.text.DateFormatUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import com.adaptris.core.AdapterLifecycleEvent;
import com.adaptris.core.EventNameSpaceConstants;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* <p>
* <code>AdapterLifecycleEvent</code> indicating that this Adapter's license is about to expire.
* </p>
*
* @config license-expiry-warning-event
*/
@JacksonXmlRootElement(localName = "license-expiry-warning-event")
@XStreamAlias("license-expiry-warning-event")
public class LicenseExpiryWarningEvent extends AdapterLifecycleEvent {
private static final long serialVersionUID = 2014012301L;

private Date expiryDate;
private String licenseExpiry;
private transient SimpleDateFormat sdf = DateFormatUtil.strictFormatter("yyyy-MM-dd");

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
