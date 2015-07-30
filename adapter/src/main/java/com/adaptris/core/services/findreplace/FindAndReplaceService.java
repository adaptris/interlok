/*
 * $RCSfile: FindAndReplaceService.java,v $
 * $Revision: 1.5 $
 * $Date: 2006/05/31 00:09:55 $
 * $Author: hfraser $
 */
package com.adaptris.core.services.findreplace;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Implementation of {@link com.adaptris.core.Service} which allows find and replace operations on the message payload.
 * <p>
 * You can specify one or more {@link com.adaptris.core.services.findreplace.FindAndReplaceUnit}'s, each of which defines the value
 * to "find" and the value to "replace" with. Each {@link com.adaptris.core.services.findreplace.FindAndReplaceUnit} will be applied
 * in order and can be used to replace the first instance or all instances.
 * </p>
 * 
 * @config find-and-replace-service
 * @license BASIC
 * @see ConfiguredReplacementSource
 * @see MetadataReplacementSource
 */
@XStreamAlias("find-and-replace-service")
public class FindAndReplaceService extends ServiceImp {

  @XStreamImplicit(itemFieldName = "find-replace-pair")
  private List<FindAndReplaceUnit> findAndReplaceUnits;
  private Boolean replaceFirstOnly;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public FindAndReplaceService() {
    findAndReplaceUnits = new ArrayList<FindAndReplaceUnit>();
  }

  /**
   * @see com.adaptris.core.Service #doService(com.adaptris.core.AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    for (FindAndReplaceUnit unit : getFindAndReplaceUnits()) {
      String find = unit.getFind().obtainValue(msg);
      String replace = unit.getReplace().obtainValue(msg);
      log.trace("replacing [" + find + "] with [" + replace + "]");
      doReplace(msg, find, replace);
    }
  }

  private void doReplace(AdaptrisMessage msg, String toReplace, String replaceWith) {
    // Standard AdaptrisMessages provide Null Production for msgs.getStringPayload()
    String result = null;
    if (replaceFirstOnly()) {
      result = msg.getStringPayload().replaceFirst(toReplace, replaceWith);
    }
    else {
      result = msg.getStringPayload().replaceAll(toReplace, replaceWith);
    }
    msg.setStringPayload(result, msg.getCharEncoding());
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  public void init() throws CoreException {
    // na
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  public void close() {
    // na
  }

  // getters and setters

  /**
   * <p>
   * Returns replaceFirstOnly.
   * </p>
   *
   * @return replaceFirstOnly if true only the first instances of the <code>String</code> to replace will be replaced. If false all
   *         instances will be replaced.
   */
  public Boolean getReplaceFirstOnly() {
    return replaceFirstOnly;
  }

  /**
   * <p>
   * Sets replaceFirstOnly.
   * </p>
   * 
   * @param b if true only the first instances of the <code>String</code> to replace will be replaced. If false all instances will
   *          be replaced; default is null (false)
   */
  public void setReplaceFirstOnly(Boolean b) {
    replaceFirstOnly = b;
  }

  boolean replaceFirstOnly() {
    return getReplaceFirstOnly() != null ? getReplaceFirstOnly().booleanValue() : false;
  }

  public List<FindAndReplaceUnit> getFindAndReplaceUnits() {
    return findAndReplaceUnits;
  }

  public void setFindAndReplaceUnits(List<FindAndReplaceUnit> findAndReplaceUnit) {
    this.findAndReplaceUnits = findAndReplaceUnit;
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }
}
