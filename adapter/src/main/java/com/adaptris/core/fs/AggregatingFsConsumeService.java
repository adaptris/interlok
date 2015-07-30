package com.adaptris.core.fs;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.aggregator.AggregatingConsumeService;
import com.adaptris.core.services.aggregator.AggregatingConsumeServiceImpl;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implentation of {@link AggregatingConsumeService} that allows you to consume a related message from a directory based on some
 * criteria.
 * 
 * @config aggregating-fs-consume-service
 * @license STANDARD
 */
@XStreamAlias("aggregating-fs-consume-service")
public class AggregatingFsConsumeService extends AggregatingConsumeServiceImpl<NullConnection> {

  @NotNull
  @Valid
  private AggregatingFsConsumer fsConsumer;

  public AggregatingFsConsumeService() {

  }

  @Override
  public void init() throws CoreException {
    super.init();
    if (fsConsumer == null) throw new CoreException("FS Consumer is null");
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      start(fsConsumer);
      fsConsumer.aggregateMessages(msg, this);
    }
    finally {
      stop(fsConsumer);
    }
  }

  /**
   * @return the fsConsumer
   */
  public AggregatingFsConsumer getFsConsumer() {
    return fsConsumer;
  }

  /**
   * @param fsConsumer the fsConsumer to set
   */
  public void setFsConsumer(AggregatingFsConsumer fsConsumer) {
    this.fsConsumer = fsConsumer;
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Standard) && (getFsConsumer() != null ? getFsConsumer().isEnabled(license) : true);
  }
}
