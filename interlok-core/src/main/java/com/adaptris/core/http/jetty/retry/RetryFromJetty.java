package com.adaptris.core.http.jetty.retry;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.*;
import com.adaptris.core.http.jetty.*;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * {@link FailedMessageRetrier} implementation that retries upon demand.
 * <p>
 * This implementation listens on the specified jetty endpoint(s) and allows you to retry failed
 * message by ID and list the contents of the data store that contains failed messages. Sometimes we
 * can't rely on standard error handlers/retriers to retry failed messages. This is intended to
 * codify some of the concepts discussed
 * <a href="https://interlok.adaptris.net/blog/2017/10/19/interlok-s3-error-store.html">here</a>
 * into a simpler configuration chain.
 * </p>
 * <p>
 * This jetty implementation allows listing of the failed messages, retrying a message, deleting
 * messages and retrieving the stacktrace from the store.
 * <ul>
 * <li>{@code curl -XGET http://localhost:8080/api/failed/list} lists message ids in the store</li>
 * <li>{@code curl -XPOST http://localhost:8080/api/retry/[msgId]} resubmits the message; returns 202 on success</li>
 * <li>{@code curl -XDELETE http://localhost:8080/api/failed/delete/[msgId]} deletes the message</li>
 * <li>{@code curl -XGET http://localhost:8080/api/failed/stacktrace/{msgId}} retrieves the stacktrace</li>
 * </ul>
 * </p>
 * <p>
 * By default, the first line of the stacktrace (error message) is included in the report.
 * Disable by setting the {@code includeErrorMessageFlag} query parameter (or configured metadata key) to {@code false}.
 * </p>
 *
 * @config retry-via-jetty
 * @since 3.11.1
 */
@NoArgsConstructor
@Slf4j
@ComponentProfile(summary = "Listen for HTTP traffic on the specified URI and retry messages",
        recommended = {EmbeddedConnection.class, JettyConnection.class}, since = "3.11.1")
@DisplayOrder(order = {"retryEndpointPrefix", "reportingEndpoint", "deleteEndpointPrefix",
        "retryHttpMethod", "deleteHttpMethod", "connection", "retryStore", "reportBuilder"})
@XStreamAlias("retry-via-jetty")
public class RetryFromJetty extends RetryFromJettyBase {

    /**
     * Where messages are stored for retries.
     */
    @Getter
    @Setter
    @NotNull
    @NonNull
    private RetryStore retryStore;

    @Override
    public void prepare() throws CoreException {
        if (!prepared) {
            Args.notNull(getReportBuilder(), "report-builder");
            Args.notNull(getRetryStore(), "retry-store");

            reporter       = new ReportListener();
            retrier        = new RetryListener();
            deleter        = new DeleteListener();
            stacktraceGetter = new StackTraceListener();

            prepareSharedComponents();
            LifecycleHelper.prepare(getRetryStore());
            prepared = true;
        }
    }

    @Override
    public void init() throws CoreException {
        prepare();
        LifecycleHelper.init(getRetryStore());
        initListeners();
    }

    @Override
    public void start() throws CoreException {
        LifecycleHelper.start(getRetryStore());
        startListeners();
    }

    @Override
    public void stop() {
        stopListeners();
        LifecycleHelper.stop(getRetryStore());
    }

    @Override
    public void close() {
        closeListeners();
        LifecycleHelper.close(getRetryStore());
    }

    public RetryFromJetty withRetryStore(RetryStore rs) {
        setRetryStore(rs);
        return this;
    }

    public RetryFromJetty withReportBuilder(ReportBuilder b) {
        setReportBuilder(b);
        return this;
    }

    // ---------------------------------------------------------------------------
    // Listener implementations — extend the inner base class so they have direct
    // access to all RetryFromJetty (and RetryFromJettyBase) members.
    // ---------------------------------------------------------------------------
    class ReportListener extends RetryJettyListenerImpl {
        @Override
        public void onAdaptrisMessage(AdaptrisMessage jettyMsg, Consumer<AdaptrisMessage> success,
                Consumer<AdaptrisMessage> failure) {
            String httpCode = HTTP_ERROR;
            boolean includeErrorMessage = true;
            try {
                if (jettyMsg.getMetadata(includeErrorMessageFlagMetadataKey()) != null
                        && jettyMsg.getMetadataValue(includeErrorMessageFlagMetadataKey()) != null) {
                    includeErrorMessage = Boolean.parseBoolean(
                            jettyMsg.getMetadataValue(includeErrorMessageFlagMetadataKey()));
                }
                getReportBuilder().build(getRetryStore().report(includeErrorMessage), jettyMsg);
                httpCode = HTTP_OK;
            } catch (Exception e) {
                jettyMsg.setContent(ExceptionUtils.getRootCauseMessage(e), StandardCharsets.UTF_8.name());
            } finally {
                sendResponse(httpCode, jettyMsg);
            }
        }

        @Override
        public String friendlyName() {
            return "RetryFromJetty::Report";
        }
    }

    private class DeleteListener extends RetryJettyListenerImpl {
        private transient Object locker = new Object();

        @Override
        @Synchronized(value = "locker")
        public void onAdaptrisMessage(AdaptrisMessage jettyMsg, Consumer<AdaptrisMessage> success,
                Consumer<AdaptrisMessage> failure) {
            String httpCode = HTTP_ERROR;
            try {
                String msgId = extractMsgId(deleteRouting, jettyMsg);
                if (msgId != null) {
                    log.trace("Attempting to delete {}", msgId);
                    httpCode = getRetryStore().delete(msgId) ? HTTP_OK : HTTP_NOT_FOUND;
                } else {
                    httpCode = HTTP_BAD;
                }
            } catch (Exception e) {
                handleException(e, jettyMsg);
            }
            sendResponse(httpCode, jettyMsg);
        }

        @Override
        public String friendlyName() {
            return "RetryFromJetty::Delete";
        }
    }

    private class RetryListener extends RetryJettyListenerImpl {
        private transient Object locker = new Object();

        @Override
        @Synchronized(value = "locker")
        public void onAdaptrisMessage(AdaptrisMessage jettyMsg, Consumer<AdaptrisMessage> success,
                Consumer<AdaptrisMessage> failure) {
            String httpCode = HTTP_ERROR;
            try {
                String msgId = extractMsgId(retryRouting, jettyMsg);
                if (msgId != null) {
                    // Look up the metadata from the store, find the workflow, then use its
                    // consumer's message factory to build the retry message.
                    Map<String, String> metadata = getRetryStore().getMetadata(msgId);
                    Workflow workflow = getWorkflow(metadata.get(Workflow.WORKFLOW_ID_KEY));
                    AdaptrisMessage msgForRetry = getRetryStore().buildForRetry(
                            msgId, metadata, workflow.getConsumer().getMessageFactory());
                    httpCode = HTTP_ACCEPTED;
                    sendResponse(httpCode, jettyMsg);
                    updateRetryCountMetadata(msgForRetry);
                    log.trace("Attempting to retry {}; resubmitting to [{}]",
                            msgForRetry.getUniqueId(), workflow.obtainWorkflowId());
                    // pooling workflow returns immediately, standard workflow does not —
                    // submit to an ExecutorService.
                    workflowSubmitter.execute(new Thread() {
                        @Override
                        public void run() {
                            Thread.currentThread().setName("Retry Failed Message");
                            workflow.onAdaptrisMessage(msgForRetry, success, failure);
                        }
                    });
                } else {
                    httpCode = HTTP_BAD;
                }
            } catch (Exception e) {
                handleException(e, jettyMsg);
            }
            sendResponse(httpCode, jettyMsg);
        }

        @Override
        public String friendlyName() {
            return "RetryFromJetty::Retry";
        }
    }

    private class StackTraceListener extends RetryJettyListenerImpl {
        private transient Object locker = new Object();

        @Override
        @Synchronized(value = "locker")
        public void onAdaptrisMessage(AdaptrisMessage jettyMsg, Consumer<AdaptrisMessage> success,
                Consumer<AdaptrisMessage> failure) {
            try {
                String msgId = extractMsgId(stackTraceRouting, jettyMsg);
                String stackTrace = getRetryStore().getStackTrace(msgId);
                handleStackTraceResponse(msgId, jettyMsg, stackTrace);
            } catch (Exception e) {
                handleException(e, jettyMsg);
            }
        }

        @Override
        public String friendlyName() {
            return "RetryFromJetty::StackTrace";
        }
    }
}
