package com.adaptris.core.http.jetty.retry;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.Map;
import java.util.function.Consumer;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.*;
import com.adaptris.core.*;
import com.adaptris.core.http.jetty.*;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * {@link FailedMessageRetrier} implementation that retries upon demand, supporting two retry stores.
 * <p>
 * This implementation listens on the specified jetty endpoint(s) and allows you to retry failed
 * message by ID and list the contents of either data store. Requests are routed to a specific
 * store via {@code retryStoreRoutingExpression} and the configured store identifiers. There is no
 * fallback: a request must resolve to either the primary or secondary store.
 * </p>
 * <ul>
 * <li>{@code curl -XGET http://localhost:8080/api/failed/list} lists message ids across all stores</li>
 * <li>{@code curl -XPOST http://localhost:8080/api/retry/[msgId]} resubmits the message; returns 202</li>
 * <li>{@code curl -XDELETE http://localhost:8080/api/failed/delete/[msgId]} deletes the message</li>
 * <li>{@code curl -XGET http://localhost:8080/api/failed/stacktrace/{msgId}} retrieves the stacktrace</li>
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
     * Message expression used to determine the retry store identifier, for example
     * {@code %message{pn.routing.region}}.
     */
    @NotBlank
    @Getter
    @Setter
    @InputFieldHint(expression = true)
    private String retryStoreRoutingExpression;

    @Override
    public void prepare() throws CoreException {
        if (!prepared) {
            validateRetryStoreConfiguration();
            Args.notNull(getReportBuilder(), "report-builder");

            reporter         = new ReportListener();
            retrier          = new RetryListener();
            deleter          = new DeleteListener();
            stacktraceGetter = new StackTraceListener();

            prepareSharedComponents();
            for (RetryStore store : getAllConfiguredStores()) {
                LifecycleHelper.prepare(store);
            }
            prepared = true;
        }
    }

    @Override
    public void init() throws CoreException {
        prepare();
        for (RetryStore store : getAllConfiguredStores()) {
            LifecycleHelper.init(store);
        }
        initListeners();
    }

    @Override
    public void start() throws CoreException {
        for (RetryStore store : getAllConfiguredStores()) {
            LifecycleHelper.start(store);
        }
        startListeners();
    }

    @Override
    public void stop() {
        stopListeners();
        for (RetryStore store : getAllConfiguredStores()) {
            LifecycleHelper.stop(store);
        }
    }

    @Override
    public void close() {
        closeListeners();
        for (RetryStore store : getAllConfiguredStores()) {
            LifecycleHelper.close(store);
        }
    }

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

    // ---------------------------------------------------------------------------
    // Dual-store routing helpers
    // ---------------------------------------------------------------------------

    private RetryStore resolveRetryStoreForRequest(AdaptrisMessage msg) {
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

    private void validateRetryStoreConfiguration() throws CoreException {
        if (getFirstRetryStore() == null) {
            throw new CoreException("No first RetryStore configured; configure firstRetryStore.");
        }
        if (getSecondRetryStore() == null) {
            throw new CoreException("No second RetryStore configured; configure secondRetryStore.");
        }
        if (StringUtils.isBlank(getRetryStoreRoutingExpression())) {
            throw new CoreException("retryStoreRoutingExpression is required.");
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

    private Set<RetryStore> getAllConfiguredStores() {
        Set<RetryStore> stores = new LinkedHashSet<>();
        if (getFirstRetryStore() != null) stores.add(getFirstRetryStore());
        if (getSecondRetryStore() != null) stores.add(getSecondRetryStore());
        return stores;
    }

    private String normalisedIdentifier(String identifier) {
        return StringUtils.trimToNull(identifier) == null ? null : StringUtils.trimToNull(identifier).toLowerCase(Locale.ROOT);
    }

    private String resolveRetryStoreRoute(AdaptrisMessage msg) {
        if (msg == null) {
            return null;
        }
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

    // ---------------------------------------------------------------------------
    // Listener implementations — extend the inner base class so they have direct
    // access to all RetryFromJettyDualStore (and RetryFromJettyBase) members.
    // ---------------------------------------------------------------------------

    class ReportListener extends RetryJettyListenerImpl {
        @Override
        public void onAdaptrisMessage(AdaptrisMessage jettyMsg, Consumer<AdaptrisMessage> success,
                Consumer<AdaptrisMessage> failure) {
            String httpCode = HTTP_ERROR;
            try {
                RetryStore selectedStore = resolveRetryStoreForRequest(jettyMsg);
                if (selectedStore != null) {
                    boolean includeErrorMessage = true;
                    if (jettyMsg.getMetadata(includeErrorMessageFlagMetadataKey()) != null
                            && jettyMsg.getMetadataValue(includeErrorMessageFlagMetadataKey()) != null) {
                        includeErrorMessage = Boolean.parseBoolean(
                                jettyMsg.getMetadataValue(includeErrorMessageFlagMetadataKey()));
                    }
                    getReportBuilder().build(selectedStore.report(includeErrorMessage), jettyMsg);
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

        @Override
        public String friendlyName() {
            return "RetryFromJettyDualStore::Report";
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
                    RetryStore target = resolveRetryStoreForRequest(jettyMsg);
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

        @Override
        public String friendlyName() {
            return "RetryFromJettyDualStore::Delete";
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
                    RetryStore store = resolveRetryStoreForRequest(jettyMsg);
                    if (store == null) {
                        httpCode = HTTP_BAD;
                    } else {
                        Map<String, String> metadata = store.getMetadata(msgId);
                        Workflow workflow = getWorkflow(metadata.get(Workflow.WORKFLOW_ID_KEY));
                        AdaptrisMessage msgForRetry = store.buildForRetry(
                                msgId, metadata, workflow.getConsumer().getMessageFactory());
                        httpCode = HTTP_ACCEPTED;
                        sendResponse(httpCode, jettyMsg);
                        updateRetryCountMetadata(msgForRetry);
                        log.trace("Attempting to retry {}; resubmitting to [{}]",
                                msgForRetry.getUniqueId(), workflow.obtainWorkflowId());
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

        @Override
        public String friendlyName() {
            return "RetryFromJettyDualStore::Retry";
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
                if (msgId == null) {
                    sendResponse(HTTP_BAD, jettyMsg);
                    return;
                }
                RetryStore target = resolveRetryStoreForRequest(jettyMsg);
                if (target == null) {
                    sendResponse(HTTP_BAD, jettyMsg);
                    return;
                }
                String stackTrace = target.getStackTrace(msgId);
                handleStackTraceResponse(msgId, jettyMsg, stackTrace);
            } catch (Exception e) {
                handleException(e, jettyMsg);
            }
        }

        @Override
        public String friendlyName() {
            return "RetryFromJettyDualStore::StackTrace";
        }
    }
}
