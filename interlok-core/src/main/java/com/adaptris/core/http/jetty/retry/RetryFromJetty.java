package com.adaptris.core.http.jetty.retry;

import static com.adaptris.core.CoreConstants.HTTP_METHOD;
import static com.adaptris.core.http.jetty.JettyConstants.JETTY_URI;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.ComponentLifecycleExtension;
import com.adaptris.core.CoreException;
import com.adaptris.core.FailedMessageRetrier;
import com.adaptris.core.FailedMessageRetrierImp;
import com.adaptris.core.Service;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.Workflow;
import com.adaptris.core.http.jetty.EmbeddedConnection;
import com.adaptris.core.http.jetty.JettyConnection;
import com.adaptris.core.http.jetty.JettyMessageConsumer;
import com.adaptris.core.http.jetty.JettyResponseService;
import com.adaptris.core.http.jetty.JettyRouteCondition;
import com.adaptris.core.http.jetty.JettyRouteCondition.JettyRoute;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.interlok.util.Args;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.text.mime.MimeConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

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
 * This jetty implementation allows two modes of operation. Listing the failed messages, and
 * retrying a message.
 * <ul>
 * <li>{@code curl -XGET http://localhost:8080/api/failed/list} gives you a list of message ids that
 * are listed in the store</li>
 * <li>{@code curl -XPOST http://localhost:8080/api/retry/[msgId]} will attempt to resubmit the
 * message to the appropriate workflow; returning a 202 upon success</li>
 * <ul>
 * Note that this implementation expects that you have a separate tooling that allows you to delete
 * failed messages.
 * </p>
 *
 * @since 3.11.1
 * @config retry-via-jetty
 */
@NoArgsConstructor
@Slf4j
@ComponentProfile(summary = "Listen for HTTP traffic on the specified URI and retry messages",
    recommended = {EmbeddedConnection.class, JettyConnection.class}, since = "3.11.1")
@DisplayOrder(order = {"retryEndpointPrefix", "reportingEndpoint", "retryHttpMethod", "connection",
    "retryStore", "reportBuilder"})
@XStreamAlias("retry-via-jetty")
public class RetryFromJetty extends FailedMessageRetrierImp {

  public static final String DEFAULT_ENDPOINT_PREFIX = "/api/retry/";
  public static final String DEFAULT_REPORTING_ENDPOINT = "/api/failed/list";
  private static final String HTTP_RETRY_METHOD = "POST";
  private static final TimeInterval DEFAULT_SHUTDOWN_WAIT = new TimeInterval(30L, TimeUnit.SECONDS.name());

  public static final String CONTENT_TYPE_METADATA_KEY = "__Content-Type";
  public static final String CONTENT_TYPE_EXPR = "%message{__Content-Type}";

  private static final String HTTP_STATUS_KEY = "__httpResponseCode";
  private static final String HTTP_STATUS_EXPR = "%message{__httpResponseCode}";
  private static final String RETRY_MSG_ID_KEY = "__retryMsgId";

  protected static final String HTTP_OK = "" + HttpURLConnection.HTTP_OK;
  protected static final String HTTP_ACCEPTED = "" + HttpURLConnection.HTTP_ACCEPTED;
  protected static final String HTTP_ERROR = "" + HttpURLConnection.HTTP_INTERNAL_ERROR;
  protected static final String HTTP_BAD = "" + HttpURLConnection.HTTP_BAD_REQUEST;

  /**
   * The retry endpoint.
   * <p>
   * The default if not explicitly specified is {@value DEFAULT_ENDPOINT_PREFIX}, note the trailing
   * {@code "/"}. The expectation is that when clients interact with the endpoint it will be in the
   * form {@code /prefix/'msgId'}
   * </p>
   */
  @Getter
  @Setter
  @InputFieldDefault(value = DEFAULT_ENDPOINT_PREFIX)
  private String retryEndpointPrefix;
  /**
   * The endpoint that allows reporting on what has failed.
   * <p>
   * The default if not explicitly specified is {@value DEFAULT_REPORTING_ENDPOINT}.
   * </p>
   */
  @Getter
  @Setter
  @InputFieldDefault(value = DEFAULT_REPORTING_ENDPOINT)
  private String reportingEndpoint;

  @Getter
  @Setter
  @NotNull
  private AdaptrisConnection connection = new EmbeddedConnection();


  /**
   * How to build reports.
   *
   */
  @Getter
  @Setter
  @NotNull
  @NonNull
  private ReportBuilder reportBuilder = new ReportBuilder();

  /**
   * Where messages are stored for retries.
   *
   */
  @Getter
  @Setter
  @NotNull
  @NonNull
  private RetryStore retryStore;

  /**
   * The HTTP method which is required for retries; the default is POST.
   */
  @AdvancedConfig(rare=true)
  @Getter
  @Setter
  @InputFieldDefault(value = HTTP_RETRY_METHOD)
  private String retryHttpMethod;

  private transient String retryServletPath;
  private transient String retryServletRegexp;
  private transient StandaloneConsumer reporting;
  private transient StandaloneConsumer retrying;
  private transient ReportListener reporter;
  private transient RetryListener retrier;
  private transient ExecutorService workflowSubmitter;
  private transient JettyRouteCondition routing;

  @Override
  public void prepare() throws CoreException {
    Args.notNull(getReportBuilder(), "report-builder");
    Args.notNull(getRetryStore(), "retry-store");
    retryServletPath = retryEndpointPrefix() + "*";
    retryServletRegexp = "^" + retryEndpointPrefix() + "(.*)";
    routing = new JettyRouteCondition().withUrlPattern(retryServletRegexp)
        .withMetadataKeys(RETRY_MSG_ID_KEY)
        .withMethod(retryHttpMethod());
    reporter = new ReportListener();
    retrier = new RetryListener();
    // By not dictating the method in the consumer; we accept all methods in jetty, but we use the
    // jetty route filter to filter it out.
    retrying = new StandaloneConsumer(getConnection(),
        new JettyMessageConsumer().withPath(retryServletPath));
    reporting = new StandaloneConsumer(getConnection(), new JettyMessageConsumer().withPath(reportingEndpoint()));
    retrying.registerAdaptrisMessageListener(retrier);
    reporting.registerAdaptrisMessageListener(reporter);
    LifecycleHelper.prepare(routing, getRetryStore(), getReportBuilder(), reporter, retrier,
        retrying, reporting);
  }

  @Override
  public void init() throws CoreException {
    LifecycleHelper.init(routing, getRetryStore(), getReportBuilder(), reporter, retrier, retrying,
        reporting);
    workflowSubmitter = Executors.newSingleThreadExecutor();
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(routing, getRetryStore(), getReportBuilder(), reporter, retrier, retrying,
        reporting);
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(routing, retrying, reporting, reporter, retrier, getRetryStore(),
        getReportBuilder());
  }

  @Override
  public void close() {
    LifecycleHelper.close(routing, retrying, reporting, reporter, retrier, getRetryStore(),
        getReportBuilder());
    ManagedThreadFactory.shutdownQuietly(workflowSubmitter, DEFAULT_SHUTDOWN_WAIT);
  }

  public RetryFromJetty withRetryStore(RetryStore rs) {
    setRetryStore(rs);
    return this;
  }

  public RetryFromJetty withReportBuilder(ReportBuilder b) {
    setReportBuilder(b);
    return this;
  }

  private String retryEndpointPrefix() {
    return StringUtils.defaultIfBlank(getRetryEndpointPrefix(), DEFAULT_ENDPOINT_PREFIX);
  }

  private String reportingEndpoint() {
    return StringUtils.defaultIfBlank(getRetryEndpointPrefix(), DEFAULT_REPORTING_ENDPOINT);
  }

  private String retryHttpMethod() {
    return StringUtils.defaultIfBlank(getRetryHttpMethod(), HTTP_RETRY_METHOD);
  }

  protected static void executeQuietly(Service service, AdaptrisMessage msg) {
    try {
      service.doService(msg);
    } catch (Exception e) {

    }
  }

  private abstract class ListenerImpl
      implements AdaptrisMessageListener, ComponentLifecycle, ComponentLifecycleExtension {

    private JettyResponseService service;

    public ListenerImpl() {
      service = new JettyResponseService().withHttpStatus(HTTP_STATUS_EXPR)
          .withContentType(CONTENT_TYPE_EXPR);
    }

    protected void sendResponse(String httpResponseCode, AdaptrisMessage msg) {
      msg.addMessageHeader(HTTP_STATUS_KEY, httpResponseCode);
      // Default a Content-Type if not available
      msg.addMessageHeader(CONTENT_TYPE_METADATA_KEY, StringUtils.defaultIfBlank(
          msg.getMetadataValue(CONTENT_TYPE_METADATA_KEY), MimeConstants.CONTENT_TYPE_TEXT_PLAIN));
      executeQuietly(service, msg);
    }

    @Override
    public void prepare() throws CoreException {
      LifecycleHelper.prepare(service);
    }

    @Override
    public void init() throws CoreException {
      LifecycleHelper.init(service);
    }

    @Override
    public void start() throws CoreException {
      LifecycleHelper.start(service);
    }

    @Override
    public void stop() {
      LifecycleHelper.stop(service);

    }

    @Override
    public void close() {
      LifecycleHelper.close(service);
    }
  }

  @NoArgsConstructor
  private class ReportListener extends ListenerImpl {
    @Override
    public void onAdaptrisMessage(AdaptrisMessage jettyMsg, Consumer<AdaptrisMessage> success,
        Consumer<AdaptrisMessage> failure) {
      String httpCode = HTTP_ERROR;
      try {
        getReportBuilder().build(getRetryStore().report(), jettyMsg);
        httpCode = HTTP_OK;
      } catch (Exception e) {
        jettyMsg.setContent(ExceptionUtils.getRootCauseMessage(e), StandardCharsets.UTF_8.name());
      } finally {
        sendResponse(httpCode, jettyMsg);
      }
    }

    @Override
    public String friendlyName() {
      return "RetryFromJetty::Reporting";
    }
  }

  private class RetryListener extends ListenerImpl {
    private transient Object locker = new Object();

    @Override
    @Synchronized(value = "locker")
    public void onAdaptrisMessage(AdaptrisMessage jettyMsg, Consumer<AdaptrisMessage> success,
        Consumer<AdaptrisMessage> failure) {
      try {
        JettyRoute route = routing.build(jettyMsg.getMetadataValue(HTTP_METHOD),
            jettyMsg.getMetadataValue(JETTY_URI));
        if (route.matches()) {
          String msgId =
              route.metadata().stream().filter((e) -> e.getKey().equalsIgnoreCase(RETRY_MSG_ID_KEY))
                  .findFirst().get().getValue();
          // There's a decision point here because we need to decide between
          // large or small message factory.
          // Do we want people to configure it?
          // Therefore we look up the metadata from the store;
          // Figure out the workflow, and then get the consumer.getMessageFactory()

          Map<String, String> metadata = retryStore.getMetadata(msgId);
          Workflow workflow = getWorkflow(metadata.get(Workflow.WORKFLOW_ID_KEY));
          AdaptrisMessage msgForRetry =
              retryStore.buildForRetry(msgId, metadata, workflow.getConsumer().getMessageFactory());
          // We know at this point we have something to retry.
          // So, we can fire a 202 before submission.
          sendResponse(HTTP_ACCEPTED, jettyMsg);
          updateRetryCountMetadata(msgForRetry);
          // pooling workflow returns immediately, standard workflow does not.
          // so submit to an Executor Service.
          workflowSubmitter.execute(new Thread() {
            @Override
            public void run() {
              Thread.currentThread().setName("Retry Failed Message");
              workflow.onAdaptrisMessage(msgForRetry, success, failure);
            }
          });
        } else {
          sendResponse(HTTP_BAD, jettyMsg);
        }
      } catch (Exception e) {
        jettyMsg.setContent(ExceptionUtils.getRootCauseMessage(e), StandardCharsets.UTF_8.name());
        sendResponse(HTTP_ERROR, jettyMsg);
      }
    }


    @Override
    public String friendlyName() {
      return "RetryFromJetty::Retry";
    }
  }
}
