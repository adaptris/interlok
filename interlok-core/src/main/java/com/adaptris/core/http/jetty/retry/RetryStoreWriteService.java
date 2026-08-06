package com.adaptris.core.http.jetty.retry;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.ServiceException;
import com.adaptris.core.Workflow;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Write a message for retry with {@link RetryFromJetty}.
 *
 * @since 3.11.1
 * @config retry-store-write-message
 */
@XStreamAlias("retry-store-write-message")
@NoArgsConstructor
@ComponentProfile(summary = "Write a message to the retry store for future retries",
    since = "3.11.1", tag = "retry")
@DisplayOrder(order = {"retryStore"})
public class RetryStoreWriteService extends RetryStoreServiceImpl {

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      enrichWorkflowId(msg);
      getRetryStore().write(msg);
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }

  }

  private void enrichWorkflowId(AdaptrisMessage msg) {
    if (StringUtils.isNotBlank(msg.getMetadataValue(Workflow.WORKFLOW_ID_KEY))) {
      return;
    }

    String workflowId = msg.getMessageLifecycleEvent().getWorkflowId();
    if (StringUtils.isBlank(workflowId)) {
      workflowId = msg.getMetadataValue(CoreConstants.WORKFLOW_ID_KEY);
    }

    if (StringUtils.isNotBlank(workflowId)) {
      msg.addMetadata(Workflow.WORKFLOW_ID_KEY, workflowId);
    }
  }
}
