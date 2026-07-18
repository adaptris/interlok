package com.adaptris.core.http.jetty.retry;

import static com.adaptris.core.CoreConstants.HTTP_METHOD;
import static com.adaptris.core.http.jetty.JettyConstants.JETTY_URI;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
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
import com.adaptris.interlok.util.Args;
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

    protected abstract RetryStore resolveRetryStoreForRequest(AdaptrisMessage msg);

    protected abstract Collection<RetryStore> getConfiguredRetryStores();

    protected abstract void validateRetryStoreConfiguration() throws CoreException;

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

    @Override
    public void prepare() throws CoreException {
        if (!prepared) {
            validateRetryStoreConfiguration();
            Args.notNull(getReportBuilder(), "report-builder");

            reporter       = new ReportListener();
            retrier        = new RetryListener();
            deleter        = new DeleteListener();
            stacktraceGetter = new StackTraceListener();

            prepareSharedComponents();
            prepareRetryStores();
            prepared = true;
        }
    }

    @Override
    public void init() throws CoreException {
        prepare();
        initRetryStores();
        initListeners();
    }

    @Override
    public void start() throws CoreException {
        startRetryStores();
        startListeners();
    }

    @Override
    public void stop() {
        stopListeners();
        stopRetryStores();
    }

    @Override
    public void close() {
        closeListeners();
        closeRetryStores();
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
            log.warn("executeQuietly caught exception: {}", e, e);
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

    protected void prepareRetryStores() throws CoreException {
        for (RetryStore store : getConfiguredRetryStores()) {
            LifecycleHelper.prepare(store);
        }
    }

    protected void initRetryStores() throws CoreException {
        for (RetryStore store : getConfiguredRetryStores()) {
            LifecycleHelper.init(store);
        }
    }

    protected void startRetryStores() throws CoreException {
        for (RetryStore store : getConfiguredRetryStores()) {
            LifecycleHelper.start(store);
        }
    }

    protected void stopRetryStores() {
        for (RetryStore store : getConfiguredRetryStores()) {
            LifecycleHelper.stop(store);
        }
    }

    protected void closeRetryStores() {
        for (RetryStore store : getConfiguredRetryStores()) {
            LifecycleHelper.close(store);
        }
    }

    protected void sendResponse(String httpResponseCode, AdaptrisMessage msg) {
        msg.addMessageHeader(HTTP_STATUS_KEY, httpResponseCode);
        msg.addMessageHeader(CONTENT_TYPE_METADATA_KEY, StringUtils.defaultIfBlank(
                msg.getMetadataValue(CONTENT_TYPE_METADATA_KEY), MimeConstants.CONTENT_TYPE_TEXT_PLAIN));
        executeQuietly(new JettyResponseService()
                .withHttpStatus(HTTP_STATUS_EXPR)
                .withContentType(CONTENT_TYPE_EXPR), msg);
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

    protected String reportListenerFriendlyName() {
        return "RetryFromJetty::Report";
    }

    protected String deleteListenerFriendlyName() {
        return "RetryFromJetty::Delete";
    }

    protected String retryListenerFriendlyName() {
        return "RetryFromJetty::Retry";
    }

    protected String stackTraceListenerFriendlyName() {
        return "RetryFromJetty::StackTrace";
    }

    protected class ReportListener extends RetryJettyListenerImpl {
        @Override
        public void onAdaptrisMessage(AdaptrisMessage jettyMsg, Consumer<AdaptrisMessage> success,
                Consumer<AdaptrisMessage> failure) {
            handleReportRequest(jettyMsg, RetryFromJettyBase.this::resolveRetryStoreForRequest);
        }

        @Override
        public String friendlyName() {
            return reportListenerFriendlyName();
        }
    }

    protected class DeleteListener extends RetryJettyListenerImpl {
        private transient Object locker = new Object();

        @Override
        @lombok.Synchronized(value = "locker")
        public void onAdaptrisMessage(AdaptrisMessage jettyMsg, Consumer<AdaptrisMessage> success,
                Consumer<AdaptrisMessage> failure) {
            handleDeleteRequest(jettyMsg, RetryFromJettyBase.this::resolveRetryStoreForRequest);
        }

        @Override
        public String friendlyName() {
            return deleteListenerFriendlyName();
        }
    }

    protected class RetryListener extends RetryJettyListenerImpl {
        private transient Object locker = new Object();

        @Override
        @lombok.Synchronized(value = "locker")
        public void onAdaptrisMessage(AdaptrisMessage jettyMsg, Consumer<AdaptrisMessage> success,
                Consumer<AdaptrisMessage> failure) {
            handleRetryRequest(jettyMsg, RetryFromJettyBase.this::resolveRetryStoreForRequest, success,
                    failure);
        }

        @Override
        public String friendlyName() {
            return retryListenerFriendlyName();
        }
    }

    protected class StackTraceListener extends RetryJettyListenerImpl {
        private transient Object locker = new Object();

        @Override
        @lombok.Synchronized(value = "locker")
        public void onAdaptrisMessage(AdaptrisMessage jettyMsg, Consumer<AdaptrisMessage> success,
                Consumer<AdaptrisMessage> failure) {
            handleStackTraceRequest(jettyMsg, RetryFromJettyBase.this::resolveRetryStoreForRequest);
        }

        @Override
        public String friendlyName() {
            return stackTraceListenerFriendlyName();
        }
    }

    protected boolean includeErrorMessage(AdaptrisMessage jettyMsg) {
        boolean includeErrorMessage = true;
        if (jettyMsg.getMetadata(includeErrorMessageFlagMetadataKey()) != null
                && jettyMsg.getMetadataValue(includeErrorMessageFlagMetadataKey()) != null) {
            includeErrorMessage = Boolean.parseBoolean(
                    jettyMsg.getMetadataValue(includeErrorMessageFlagMetadataKey()));
        }
        return includeErrorMessage;
    }

    protected void handleReportRequest(AdaptrisMessage jettyMsg,
            Function<AdaptrisMessage, RetryStore> storeResolver) {
        String httpCode = HTTP_ERROR;
        try {
            RetryStore store = storeResolver.apply(jettyMsg);
            if (store != null) {
                getReportBuilder().build(store.report(includeErrorMessage(jettyMsg)), jettyMsg);
                httpCode = HTTP_OK;
            } else {
                httpCode = HTTP_BAD;
            }
        } catch (Exception e) {
            jettyMsg.setContent(ExceptionUtils.getRootCauseMessage(e), StandardCharsets.UTF_8.name());
        } finally {
            sendResponse(httpCode, jettyMsg);
        }
    }

    protected void handleDeleteRequest(AdaptrisMessage jettyMsg,
            Function<AdaptrisMessage, RetryStore> storeResolver) {
        String httpCode = HTTP_ERROR;
        try {
            String msgId = extractMsgId(deleteRouting, jettyMsg);
            if (msgId != null) {
                RetryStore target = storeResolver.apply(jettyMsg);
                if (target == null) {
                    httpCode = HTTP_BAD;
                } else {
                    log.trace("Attempting to delete {}", msgId);
                    httpCode = target.delete(msgId) ? HTTP_OK : HTTP_NOT_FOUND;
                }
            } else {
                httpCode = HTTP_BAD;
            }
        } catch (Exception e) {
            handleException(e, jettyMsg);
        }
        sendResponse(httpCode, jettyMsg);
    }

    protected void handleRetryRequest(AdaptrisMessage jettyMsg,
            Function<AdaptrisMessage, RetryStore> storeResolver, Consumer<AdaptrisMessage> success,
            Consumer<AdaptrisMessage> failure) {
        String httpCode = HTTP_ERROR;
        try {
            String msgId = extractMsgId(retryRouting, jettyMsg);
            if (msgId != null) {
                RetryStore store = storeResolver.apply(jettyMsg);
                if (store == null) {
                    httpCode = HTTP_BAD;
                } else {
                    // Look up the metadata from the store, find the workflow, then use its
                    // consumer's message factory to build the retry message.
                    java.util.Map<String, String> metadata = store.getMetadata(msgId);
                    com.adaptris.core.Workflow workflow = getWorkflow(metadata.get(com.adaptris.core.Workflow.WORKFLOW_ID_KEY));
                    AdaptrisMessage msgForRetry = store.buildForRetry(
                            msgId, metadata, workflow.getConsumer().getMessageFactory());
                    httpCode = HTTP_ACCEPTED;
                    sendResponse(httpCode, jettyMsg);
                    updateRetryCountMetadata(msgForRetry);
                    log.trace("Attempting to retry {}; resubmitting to [{}]",
                            msgForRetry.getUniqueId(), workflow.obtainWorkflowId());
                    // pooling workflow returns immediately, standard workflow does not —
                    // submit to an ExecutorService.
                    workflowSubmitter.execute(() -> {
                        Thread.currentThread().setName("Retry Failed Message");
                        workflow.onAdaptrisMessage(msgForRetry, success, failure);
                    });
                }
            } else {
                httpCode = HTTP_BAD;
            }
        } catch (Exception e) {
            handleException(e, jettyMsg);
        }
        sendResponse(httpCode, jettyMsg);
    }

    protected void handleStackTraceRequest(AdaptrisMessage jettyMsg,
            Function<AdaptrisMessage, RetryStore> storeResolver) {
        try {
            String msgId = extractMsgId(stackTraceRouting, jettyMsg);
            if (msgId == null) {
                sendResponse(HTTP_BAD, jettyMsg);
                return;
            }
            RetryStore store = storeResolver.apply(jettyMsg);
            if (store == null) {
                sendResponse(HTTP_BAD, jettyMsg);
                return;
            }
            String stackTrace = store.getStackTrace(msgId);
            handleStackTraceResponse(msgId, jettyMsg, stackTrace);
        } catch (Exception e) {
            handleException(e, jettyMsg);
        }
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
