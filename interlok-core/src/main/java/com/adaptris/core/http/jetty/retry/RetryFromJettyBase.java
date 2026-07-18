package com.adaptris.core.http.jetty.retry;

import static com.adaptris.core.CoreConstants.HTTP_METHOD;
import static com.adaptris.core.http.jetty.JettyConstants.JETTY_URI;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.ComponentLifecycleExtension;
import com.adaptris.core.CoreException;
import com.adaptris.core.FailedMessageRetrierImp;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.Service;
import com.adaptris.core.http.jetty.EmbeddedConnection;
import com.adaptris.core.http.jetty.JettyMessageConsumer;
import com.adaptris.core.http.jetty.JettyResponseService;
import com.adaptris.core.http.jetty.JettyRouteCondition;
import com.adaptris.core.http.jetty.JettyRouteCondition.JettyRoute;
import com.adaptris.core.http.jetty.MetadataParameterHandler;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.text.mime.MimeConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Abstract base class for Jetty-based {@link com.adaptris.core.FailedMessageRetrier} implementations.
 * <p>
 * Provides the shared infrastructure for HTTP endpoint handling, routing conditions, consumer lifecycle
 * management, and the {@link RetryJettyListenerImpl} base class used by all concrete listener
 * implementations.
 * </p>
 *
 * @since 3.11.1
 */
@NoArgsConstructor
@Slf4j
public abstract class RetryFromJettyBase extends FailedMessageRetrierImp {

    public static final String DEFAULT_ENDPOINT_PREFIX = "/api/retry/";
    public static final String DEFAULT_REPORTING_ENDPOINT = "/api/failed/list";
    public static final String DEFAULT_DELETE_PREFIX = "/api/failed/delete/";
    public static final String DEFAULT_STACKTRACE_PREFIX = "/api/failed/stacktrace/";
    public static final String DEFAULT_INCLUDE_ERROR_MESSAGE_FLAG_METADATA_KEY = "includeErrorMessageFlag";

    protected static final String HTTP_RETRY_METHOD = "POST";
    protected static final String HTTP_DELETE_METHOD = "DELETE";
    protected static final String HTTP_STACKTRACE_METHOD = "GET";

    protected static final TimeInterval DEFAULT_SHUTDOWN_WAIT = new TimeInterval(30L, TimeUnit.SECONDS.name());

    public static final String CONTENT_TYPE_METADATA_KEY = "__Content-Type";
    public static final String CONTENT_TYPE_EXPR = "%message{__Content-Type}";

    static final String HTTP_STATUS_KEY = "__httpResponseCode";
    private static final String HTTP_STATUS_EXPR = "%message{__httpResponseCode}";
    static final String MSG_ID_KEY = "__MsgId";

    protected static final String HTTP_OK = "" + HttpURLConnection.HTTP_OK;
    protected static final String HTTP_ACCEPTED = "" + HttpURLConnection.HTTP_ACCEPTED;
    protected static final String HTTP_ERROR = "" + HttpURLConnection.HTTP_INTERNAL_ERROR;
    protected static final String HTTP_BAD = "" + HttpURLConnection.HTTP_BAD_REQUEST;
    protected static final String HTTP_NOT_FOUND = "" + HttpURLConnection.HTTP_NOT_FOUND;

    /**
     * The retry endpoint prefix.
     * <p>
     * The default if not explicitly specified is {@value DEFAULT_ENDPOINT_PREFIX}, note the trailing
     * {@code "/"}. The expectation is that when clients interact with the endpoint it will be in the
     * form {@code /prefix/'msgId'}
     * </p>
     */
    @Getter
    @Setter
    @InputFieldDefault(value = DEFAULT_ENDPOINT_PREFIX)
    @InputFieldHint(external = true)
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
    @InputFieldHint(external = true)
    private String reportingEndpoint;

    /**
     * The delete endpoint prefix.
     * <p>
     * The default if not explicitly specified is {@value DEFAULT_DELETE_PREFIX}, note the trailing
     * {@code "/"}. The expectation is that when clients interact with the endpoint it will be in the
     * form {@code /prefix/'msgId'}
     * </p>
     */
    @Getter
    @Setter
    @InputFieldDefault(value = DEFAULT_DELETE_PREFIX)
    @InputFieldHint(external = true)
    private String deleteEndpointPrefix;

    /**
     * The stacktrace endpoint prefix.
     * <p>
     * The default if not explicitly specified is {@value DEFAULT_STACKTRACE_PREFIX}, note the trailing
     * {@code "/"}. The expectation is that when clients interact with the endpoint it will be in the
     * form {@code /prefix/'msgId'}
     * </p>
     */
    @Getter
    @Setter
    @InputFieldDefault(value = DEFAULT_STACKTRACE_PREFIX)
    @InputFieldHint(external = true)
    private String stackTraceEndpointPrefix;

    /**
     * The underlying Jetty connection.
     */
    @Getter
    @Setter
    @NotNull
    private AdaptrisConnection connection = new EmbeddedConnection();

    /**
     * How to build reports.
     */
    @Getter
    @Setter
    @NotNull
    @NonNull
    private ReportBuilder reportBuilder = new ReportBuilder();

    /**
     * Metadata key used to control whether the error message is included in the report.
     */
    @AdvancedConfig(rare = true)
    @Getter
    @Setter
    @InputFieldDefault(value = DEFAULT_INCLUDE_ERROR_MESSAGE_FLAG_METADATA_KEY)
    private String includeErrorMessageFlagMetadataKey;

    /**
     * The HTTP method required for retries; the default is POST.
     */
    @AdvancedConfig(rare = true)
    @Getter
    @Setter
    @InputFieldDefault(value = HTTP_RETRY_METHOD)
    private String retryHttpMethod;

    /**
     * The HTTP method required for deleting messages from the retry store; the default is DELETE.
     */
    @AdvancedConfig(rare = true)
    @Getter
    @Setter
    @InputFieldDefault(value = HTTP_DELETE_METHOD)
    private String deleteHttpMethod;

    /**
     * The HTTP method required for retrieving a stacktrace; the default is GET.
     */
    @AdvancedConfig(rare = true)
    @Getter
    @Setter
    @InputFieldDefault(value = HTTP_STACKTRACE_METHOD)
    private String stackTraceHttpMethod;

    // Transient infrastructure shared across all implementations
    protected transient StandaloneConsumer reporting;
    protected transient StandaloneConsumer retrying;
    protected transient StandaloneConsumer deleting;
    protected transient StandaloneConsumer gettingStacktrace;

    protected transient RetryJettyListenerImpl reporter;
    protected transient RetryJettyListenerImpl retrier;
    protected transient RetryJettyListenerImpl deleter;
    protected transient RetryJettyListenerImpl stacktraceGetter;

    protected transient ExecutorService workflowSubmitter;
    protected transient JettyRouteCondition retryRouting;
    protected transient JettyRouteCondition deleteRouting;
    protected transient JettyRouteCondition stackTraceRouting;
    protected transient boolean prepared = false;

    // ---------------------------------------------------------------------------
    // Endpoint / method resolution helpers
    // ---------------------------------------------------------------------------

    String retryEndpointPrefix() {
        return resolveConfiguredEndpoint(getRetryEndpointPrefix(), DEFAULT_ENDPOINT_PREFIX);
    }

    String reportingEndpoint() {
        return resolveConfiguredEndpoint(getReportingEndpoint(), DEFAULT_REPORTING_ENDPOINT);
    }

    String stackTraceEndpointPrefix() {
        return resolveConfiguredEndpoint(getStackTraceEndpointPrefix(), DEFAULT_STACKTRACE_PREFIX);
    }

    String deleteEndpointPrefix() {
        return resolveConfiguredEndpoint(getDeleteEndpointPrefix(), DEFAULT_DELETE_PREFIX);
    }

    String retryHttpMethod() {
        return StringUtils.defaultIfBlank(getRetryHttpMethod(), HTTP_RETRY_METHOD);
    }

    String deleteHttpMethod() {
        return StringUtils.defaultIfBlank(getDeleteHttpMethod(), HTTP_DELETE_METHOD);
    }

    String stackTraceHttpMethod() {
        return StringUtils.defaultIfBlank(getStackTraceHttpMethod(), HTTP_STACKTRACE_METHOD);
    }

    String includeErrorMessageFlagMetadataKey() {
        return StringUtils.defaultIfBlank(getIncludeErrorMessageFlagMetadataKey(),
                DEFAULT_INCLUDE_ERROR_MESSAGE_FLAG_METADATA_KEY);
    }

    String resolveConfiguredEndpoint(String configuredValue, String defaultValue) {
        return StringUtils.defaultIfBlank(
                ExternalResolver.resolve(StringUtils.defaultIfBlank(configuredValue, defaultValue)),
                defaultValue);
    }

    // ---------------------------------------------------------------------------
    // Shared infrastructure helpers
    // ---------------------------------------------------------------------------

    protected static void executeQuietly(Service service, AdaptrisMessage msg) {
        try {
            service.doService(msg);
        } catch (Exception e) {
            log.warn("executeQuietly caught exception: {}", e.toString(), e);
        }
    }

    protected void prepareSharedComponents() throws CoreException {
        String retryServletPath = retryEndpointPrefix() + "*";
        String retryServletRegexp = "^" + retryEndpointPrefix() + "(.*)";
        String deleteServletPath = deleteEndpointPrefix() + "*";
        String deleteServletRegexp = "^" + deleteEndpointPrefix() + "(.*)";
        String stackTraceServletPath = stackTraceEndpointPrefix() + "*";
        String stackTraceServletRegexp = "^" + stackTraceEndpointPrefix() + "(.*)";

        retryRouting = new JettyRouteCondition().withUrlPattern(retryServletRegexp).withMetadataKeys(MSG_ID_KEY).withMethod(retryHttpMethod());
        deleteRouting = new JettyRouteCondition().withUrlPattern(deleteServletRegexp).withMetadataKeys(MSG_ID_KEY).withMethod(deleteHttpMethod());
        stackTraceRouting = new JettyRouteCondition().withUrlPattern(stackTraceServletRegexp).withMetadataKeys(MSG_ID_KEY).withMethod(stackTraceHttpMethod());

        JettyMessageConsumer reportingConsumer = new JettyMessageConsumer().withPath(reportingEndpoint());
        reportingConsumer.setParameterHandler(new MetadataParameterHandler());

        JettyMessageConsumer retryingConsumer = new JettyMessageConsumer().withPath(retryServletPath);
        retryingConsumer.setParameterHandler(new MetadataParameterHandler());
        JettyMessageConsumer deletingConsumer = new JettyMessageConsumer().withPath(deleteServletPath);
        deletingConsumer.setParameterHandler(new MetadataParameterHandler());
        JettyMessageConsumer stackTraceConsumer = new JettyMessageConsumer().withPath(stackTraceServletPath);
        stackTraceConsumer.setParameterHandler(new MetadataParameterHandler());

        retrying = new StandaloneConsumer(getConnection(), retryingConsumer);
        deleting = new StandaloneConsumer(getConnection(), deletingConsumer);
        reporting = new StandaloneConsumer(getConnection(), reportingConsumer);
        gettingStacktrace = new StandaloneConsumer(getConnection(), stackTraceConsumer);

        retrying.registerAdaptrisMessageListener(retrier);
        reporting.registerAdaptrisMessageListener(reporter);
        deleting.registerAdaptrisMessageListener(deleter);
        gettingStacktrace.registerAdaptrisMessageListener(stacktraceGetter);

        LifecycleHelper.prepare(getReportBuilder());
        LifecycleHelper.prepare(deleteRouting, deleter, deleting);
        LifecycleHelper.prepare(retryRouting, retrier, retrying);
        LifecycleHelper.prepare(reporter, reporting);
        LifecycleHelper.prepare(stackTraceRouting, stacktraceGetter, gettingStacktrace);
    }

    protected void initListeners() throws CoreException {
        LifecycleHelper.init(getReportBuilder());
        LifecycleHelper.init(deleteRouting, deleter, deleting);
        LifecycleHelper.init(retryRouting, retrier, retrying);
        LifecycleHelper.init(reporter, reporting);
        LifecycleHelper.init(stackTraceRouting, stacktraceGetter, gettingStacktrace);
        workflowSubmitter = Executors.newSingleThreadExecutor();
    }

    protected void startListeners() throws CoreException {
        LifecycleHelper.start(getReportBuilder());
        LifecycleHelper.start(deleteRouting, deleter, deleting);
        LifecycleHelper.start(retryRouting, retrier, retrying);
        LifecycleHelper.start(reporter, reporting);
        LifecycleHelper.start(stackTraceRouting, stacktraceGetter, gettingStacktrace);
    }

    protected void stopListeners() {
        LifecycleHelper.stop(deleteRouting, deleter, deleting);
        LifecycleHelper.stop(retryRouting, retrier, retrying);
        LifecycleHelper.stop(reporter, reporting);
        LifecycleHelper.stop(stackTraceRouting, stacktraceGetter, gettingStacktrace);
        LifecycleHelper.stop(getReportBuilder());
    }

    protected void closeListeners() {
        LifecycleHelper.close(deleteRouting, deleter, deleting);
        LifecycleHelper.close(retryRouting, retrier, retrying);
        LifecycleHelper.close(reporter, reporting);
        LifecycleHelper.close(stackTraceRouting, stacktraceGetter, gettingStacktrace);
        LifecycleHelper.close(getReportBuilder());
        ManagedThreadFactory.shutdownQuietly(workflowSubmitter, DEFAULT_SHUTDOWN_WAIT);
    }

    // ---------------------------------------------------------------------------
    // Shared listener base — inner class so subclass listeners get direct access
    // to the enclosing RetryFromJettyBase instance and all its members.
    // ---------------------------------------------------------------------------

    protected abstract class RetryJettyListenerImpl
            implements AdaptrisMessageListener, ComponentLifecycle, ComponentLifecycleExtension {

        private final JettyResponseService service;

        protected RetryJettyListenerImpl() {
            service = new JettyResponseService()
                    .withHttpStatus(HTTP_STATUS_EXPR)
                    .withContentType(CONTENT_TYPE_EXPR);
        }

        protected void sendResponse(String httpResponseCode, AdaptrisMessage msg) {
            msg.addMessageHeader(HTTP_STATUS_KEY, httpResponseCode);
            msg.addMessageHeader(CONTENT_TYPE_METADATA_KEY, StringUtils.defaultIfBlank(
                    msg.getMetadataValue(CONTENT_TYPE_METADATA_KEY), MimeConstants.CONTENT_TYPE_TEXT_PLAIN));
            executeQuietly(service, msg);
        }

        protected String extractMsgId(JettyRouteCondition routing, AdaptrisMessage jettyMsg)
                throws CoreException {
            JettyRoute route = routing.build(
                    jettyMsg.getMetadataValue(HTTP_METHOD),
                    jettyMsg.getMetadataValue(JETTY_URI));
            if (route.matches()) {
                Optional<MetadataElement> msgIdEntry = route.metadata().stream()
                        .filter(e -> e.getKey().equalsIgnoreCase(MSG_ID_KEY))
                        .findFirst();
                if (msgIdEntry.isPresent()) {
                    return msgIdEntry.get().getValue();
                }
            }
            return null;
        }

        protected void handleException(Exception e, AdaptrisMessage msg) {
            msg.setContent(ExceptionUtils.getRootCauseMessage(e), StandardCharsets.UTF_8.name());
            sendResponse(HTTP_ERROR, msg);
        }

        protected void handleStackTraceResponse(String msgId, AdaptrisMessage jettyMsg, String stackTrace) {
            String httpCode = (msgId != null) ? HTTP_OK : HTTP_BAD;
            if (msgId != null) {
                jettyMsg.setContent(stackTrace, StandardCharsets.UTF_8.name());
            }
            sendResponse(httpCode, jettyMsg);
        }

        @Override
        public void onAdaptrisMessage(AdaptrisMessage msg) {
            // Jetty consumers may invoke the single-argument listener method.
            // Delegate so existing listener logic in the Consumer overload still executes.
            onAdaptrisMessage(msg, m -> {
            }, m -> {
            });
        }

        @Override
        public abstract void onAdaptrisMessage(AdaptrisMessage msg, Consumer<AdaptrisMessage> success,
                Consumer<AdaptrisMessage> failure);

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
}
