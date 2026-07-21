package com.adaptris.core.http.jetty.retry;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.*;
import com.adaptris.core.*;
import com.adaptris.core.http.jetty.*;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * {@link FailedMessageRetrier} implementation that retries upon demand, supporting two retry stores.
 * <p>
 * This implementation listens on the specified jetty endpoint(s) and allows you to retry failed
 * message by ID and list the contents of either data store. Requests are routed to a specific
 * store by extracting the region from the URL path first; if a region cannot be extracted, the
 * optional {@code retryStoreRoutingExpression} is used as a fallback. The resolved route must
 * match one of the configured store identifiers; requests are rejected if no match is found.
 * </p>
 * <ul>
 * <li>{@code curl -XGET http://localhost:8080/api/failed/[region]/list} lists message ids for the region</li>
 * <li>{@code curl -XPOST http://localhost:8080/api/[region]/retry/[msgId]} resubmits the message; returns 202</li>
 * <li>{@code curl -XDELETE http://localhost:8080/api/failed/[region]/delete/[msgId]} deletes the message</li>
 * <li>{@code curl -XGET http://localhost:8080/api/failed/[region]/stacktrace/[msgId]} retrieves the stacktrace</li>
 * </ul>
 *
 * @config retry-via-jetty-dual-store
 * @since 5.0.6
 */
@NoArgsConstructor
@Slf4j
@ComponentProfile(summary = "Listen for HTTP traffic on the specified URI and retry messages",
        recommended = {EmbeddedConnection.class, JettyConnection.class}, since = "5.0.6")
@DisplayOrder(order = {"retryEndpointPrefix", "reportingEndpoint", "deleteEndpointPrefix",
        "retryHttpMethod", "deleteHttpMethod", "connection", "firstRetryStore", "secondRetryStore",
        "firstRetryStoreIdentifier", "secondRetryStoreIdentifier", "retryStoreRoutingExpression",
        "reportBuilder"})
@XStreamAlias("retry-via-jetty-dual-store")
public class RetryFromJettyDualStore extends RetryFromJettyBase {

    private static final String REGION_KEY = "__RetryStoreRegion";
    private static final String API_ENDPOINT_PATH = "/api/*";
    private static final String REPORTING_ENDPOINT_REGEXP = "^/api/failed/([^/]+)/list$";
    private static final String RETRY_ENDPOINT_REGEXP = "^/api/([^/]+)/retry/(.*)";
    private static final String DELETE_ENDPOINT_REGEXP = "^/api/failed/([^/]+)/delete/(.*)";
    private static final String STACKTRACE_ENDPOINT_REGEXP = "^/api/failed/([^/]+)/stacktrace/(.*)";

    private static final Pattern REPORTING_ENDPOINT_PATTERN = Pattern.compile(REPORTING_ENDPOINT_REGEXP);
    private transient RetryJettyListenerImpl dispatchListener;

    @NotNull
    @Valid
    @Getter
    @Setter
    private RetryStore firstRetryStore;

    @NotBlank
    @Getter
    @Setter
    @InputFieldHint(expression = true)
    private String firstRetryStoreIdentifier;

    @NotNull
    @Valid
    @Getter
    @Setter
    private RetryStore secondRetryStore;

    @NotBlank
    @Getter
    @Setter
    @InputFieldHint(expression = true)
    private String secondRetryStoreIdentifier;

    /**
     * Optional message expression used as a fallback to determine the retry store identifier when
     * the URL path does not provide a region, for example
     * {@code %message{pn.routing.region}}.
     */
    @Getter
    @Setter
    @InputFieldHint(expression = true)
    private String retryStoreRoutingExpression;


    public RetryFromJettyDualStore withFirstRetryStore(RetryStore rs) {
        setFirstRetryStore(rs);
        return this;
    }

    public RetryFromJettyDualStore withSecondRetryStore(RetryStore rs) {
        setSecondRetryStore(rs);
        return this;
    }

    public RetryFromJettyDualStore withFirstRetryStoreIdentifier(String identifier) {
        setFirstRetryStoreIdentifier(identifier);
        return this;
    }

    public RetryFromJettyDualStore withSecondRetryStoreIdentifier(String identifier) {
        setSecondRetryStoreIdentifier(identifier);
        return this;
    }

    public RetryFromJettyDualStore withReportBuilder(ReportBuilder b) {
        setReportBuilder(b);
        return this;
    }

    public RetryFromJettyDualStore withRetryStoreRoutingExpression(String expr) {
        setRetryStoreRoutingExpression(expr);
        return this;
    }

    @Override
    protected String reportListenerFriendlyName() {
        return "RetryFromJettyDualStore::Report";
    }

    @Override
    protected String deleteListenerFriendlyName() {
        return "RetryFromJettyDualStore::Delete";
    }

    @Override
    protected String retryListenerFriendlyName() {
        return "RetryFromJettyDualStore::Retry";
    }

    @Override
    protected String stackTraceListenerFriendlyName() {
        return "RetryFromJettyDualStore::StackTrace";
    }

    @Override
    protected void prepareSharedComponents() throws CoreException {
        retryRouting = new JettyRouteCondition().withUrlPattern(RETRY_ENDPOINT_REGEXP)
                .withMetadataKeys(REGION_KEY, MSG_ID_KEY).withMethod(retryHttpMethod());
        deleteRouting = new JettyRouteCondition().withUrlPattern(DELETE_ENDPOINT_REGEXP)
                .withMetadataKeys(REGION_KEY, MSG_ID_KEY).withMethod(deleteHttpMethod());
        stackTraceRouting = new JettyRouteCondition().withUrlPattern(STACKTRACE_ENDPOINT_REGEXP)
                .withMetadataKeys(REGION_KEY, MSG_ID_KEY).withMethod(stackTraceHttpMethod());
        dispatchListener = new DispatchListener();
        JettyMessageConsumer apiConsumer = new JettyMessageConsumer().withPath(API_ENDPOINT_PATH);
        apiConsumer.setParameterHandler(new MetadataParameterHandler());
        reporting = new StandaloneConsumer(getConnection(), apiConsumer);
        reporting.registerAdaptrisMessageListener(dispatchListener);

        LifecycleHelper.prepare(getReportBuilder());
        LifecycleHelper.prepare(deleteRouting, retryRouting, stackTraceRouting, dispatchListener, reporting);
    }

    @Override
    protected void initListeners() throws CoreException {
        LifecycleHelper.init(getReportBuilder());
        LifecycleHelper.init(deleteRouting, retryRouting, stackTraceRouting, dispatchListener, reporting);
        workflowSubmitter = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void startListeners() throws CoreException {
        LifecycleHelper.start(getReportBuilder());
        LifecycleHelper.start(deleteRouting, retryRouting, stackTraceRouting, dispatchListener, reporting);
    }

    @Override
    protected void stopListeners() {
        LifecycleHelper.stop(deleteRouting, retryRouting, stackTraceRouting, dispatchListener, reporting);
        LifecycleHelper.stop(getReportBuilder());
    }

    @Override
    protected void closeListeners() {
        LifecycleHelper.close(deleteRouting, retryRouting, stackTraceRouting, dispatchListener, reporting);
        LifecycleHelper.close(getReportBuilder());
        ManagedThreadFactory.shutdownQuietly(workflowSubmitter, DEFAULT_SHUTDOWN_WAIT);
    }

    @Override
    protected RetryStore resolveRetryStoreForRequest(AdaptrisMessage msg) {
        String route = resolveRetryStoreRoute(msg);
        String firstIdentifier = resolveRetryStoreIdentifier(getFirstRetryStoreIdentifier(), msg);
        String secondIdentifier = resolveRetryStoreIdentifier(getSecondRetryStoreIdentifier(), msg);
        if (StringUtils.isAnyBlank(route, firstIdentifier, secondIdentifier)) {
            return null;
        }
        if (Objects.equals(firstIdentifier, secondIdentifier)) {
            log.debug("Resolved retry store identifiers are not distinct; request cannot be routed safely.");
            return null;
        }
        if (Objects.equals(route, firstIdentifier)) {
            return getFirstRetryStore();
        }
        if (Objects.equals(route, secondIdentifier)) {
            return getSecondRetryStore();
        }
        return null;
    }

    @Override
    protected Collection<RetryStore> getConfiguredRetryStores() {
        Set<RetryStore> stores = new LinkedHashSet<>();
        if (getFirstRetryStore() != null) stores.add(getFirstRetryStore());
        if (getSecondRetryStore() != null) stores.add(getSecondRetryStore());
        return stores;
    }

    @Override
    protected void validateRetryStoreConfiguration() throws CoreException {
        if (getFirstRetryStore() == null) {
            throw new CoreException("No first RetryStore configured; configure firstRetryStore.");
        }
        if (getSecondRetryStore() == null) {
            throw new CoreException("No second RetryStore configured; configure secondRetryStore.");
        }
        String firstId = normalisedIdentifier(getFirstRetryStoreIdentifier());
        String secondId = normalisedIdentifier(getSecondRetryStoreIdentifier());
        if (StringUtils.isAnyBlank(firstId, secondId)) {
            throw new CoreException("firstRetryStoreIdentifier and secondRetryStoreIdentifier are required.");
        }
        if (Objects.equals(firstId, secondId)) {
            throw new CoreException("firstRetryStoreIdentifier and secondRetryStoreIdentifier must be distinct.");
        }
    }


    private String normalisedIdentifier(String identifier) {
        return StringUtils.trimToNull(identifier) == null ? null : StringUtils.trimToNull(identifier).toLowerCase(Locale.ROOT);
    }

    private String resolveRetryStoreRoute(AdaptrisMessage msg) {
        if (msg == null) {
            return null;
        }
        String routeFromPath = resolveRetryStoreRouteFromPath(msg);
        if (routeFromPath != null) {
            return routeFromPath;
        }
        return resolveRetryStoreRouteFromExpression(msg);
    }

    private String resolveRetryStoreRouteFromPath(AdaptrisMessage msg) {
        try {
            String method = msg.getMetadataValue(CoreConstants.HTTP_METHOD);
            String uri = msg.getMetadataValue(JettyConstants.JETTY_URI);
            if (StringUtils.isAnyBlank(method, uri)) {
                return null;
            }
            JettyRouteCondition.JettyRoute retryRoute = retryRouting.build(method, uri);
            if (retryRoute.matches()) {
                return extractRouteFromMetadata(retryRoute);
            }
            JettyRouteCondition.JettyRoute deleteRoute = deleteRouting.build(method, uri);
            if (deleteRoute.matches()) {
                return extractRouteFromMetadata(deleteRoute);
            }
            JettyRouteCondition.JettyRoute stackTraceRoute = stackTraceRouting.build(method, uri);
            if (stackTraceRoute.matches()) {
                return extractRouteFromMetadata(stackTraceRoute);
            }
            Matcher reportMatcher = REPORTING_ENDPOINT_PATTERN.matcher(uri);
            if (reportMatcher.matches()) {
                return normalisedIdentifier(reportMatcher.group(1));
            }
        } catch (Exception e) {
            log.debug("Could not resolve retry store route from URL path: {}", e.getMessage());
        }
        return null;
    }

    private String resolveRetryStoreRouteFromExpression(AdaptrisMessage msg) {
        String expression = StringUtils.trimToNull(getRetryStoreRoutingExpression());
        if (expression == null) {
            return null;
        }
        try {
            String resolved = StringUtils.trimToNull(msg.resolve(expression));
            return resolved == null ? null : resolved.toLowerCase(Locale.ROOT);
        } catch (Exception e) {
            log.debug("Could not resolve retry store routing expression [{}]: {}", expression, e.getMessage());
            return null;
        }
    }

    private String extractRouteFromMetadata(JettyRouteCondition.JettyRoute route) {
        return route.metadata().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(REGION_KEY))
                .findFirst()
                .map(e -> normalisedIdentifier(e.getValue()))
                .orElse(null);
    }

    private String resolveRetryStoreIdentifier(String configuredIdentifier, AdaptrisMessage msg) {
        String identifier = StringUtils.trimToNull(configuredIdentifier);
        if (identifier == null || msg == null) {
            return normalisedIdentifier(identifier);
        }
        try {
            return normalisedIdentifier(msg.resolve(identifier));
        } catch (Exception e) {
            log.debug("Could not resolve retry store identifier [{}]: {}", identifier, e.getMessage());
            return null;
        }
    }

    protected class DispatchListener extends RetryJettyListenerImpl {

        @Override
        public void onAdaptrisMessage(AdaptrisMessage jettyMsg, Consumer<AdaptrisMessage> success,
                                      Consumer<AdaptrisMessage> failure) {
            try {
                String method = jettyMsg.getMetadataValue(CoreConstants.HTTP_METHOD);
                String uri = jettyMsg.getMetadataValue(JettyConstants.JETTY_URI);
                if (retryRouting.build(method, uri).matches()) {
                    handleRetryRequest(jettyMsg, RetryFromJettyDualStore.this::resolveRetryStoreForRequest, success, failure);
                    return;
                }
                if (deleteRouting.build(method, uri).matches()) {
                    handleDeleteRequest(jettyMsg, RetryFromJettyDualStore.this::resolveRetryStoreForRequest);
                    return;
                }
                if (stackTraceRouting.build(method, uri).matches()) {
                    handleStackTraceRequest(jettyMsg, RetryFromJettyDualStore.this::resolveRetryStoreForRequest);
                    return;
                }
                Matcher reportMatcher = REPORTING_ENDPOINT_PATTERN.matcher(uri);
                if (reportMatcher.matches()) {
                    handleReportRequest(jettyMsg, RetryFromJettyDualStore.this::resolveRetryStoreForRequest);
                    return;
                }
                sendResponse(HTTP_NOT_FOUND, jettyMsg);
            } catch (Exception e) {
                handleException(e, jettyMsg);
            }
        }

        @Override
        public String friendlyName() {
            return "RetryFromJettyDualStore::Dispatcher";
        }
    }

}
