package com.adaptris.core.http.jetty.retry;

import java.util.Collection;
import java.util.Collections;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.*;
import com.adaptris.core.http.jetty.*;
import com.adaptris.interlok.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
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
 * This jetty implementation allows listing of the failed messages, retrying a message, deleting
 * messages and retrieving the stacktrace from the store.
 * <ul>
 * <li>{@code curl -XGET http://localhost:8080/api/failed/list} lists message ids in the store</li>
 * <li>{@code curl -XPOST http://localhost:8080/api/retry/[msgId]} resubmits the message; returns 202 on success</li>
 * <li>{@code curl -XDELETE http://localhost:8080/api/failed/delete/[msgId]} deletes the message</li>
 * <li>{@code curl -XGET http://localhost:8080/api/failed/stacktrace/[msgId]} retrieves the stacktrace</li>
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

    public RetryFromJetty withRetryStore(RetryStore rs) {
        setRetryStore(rs);
        return this;
    }

    public RetryFromJetty withReportBuilder(ReportBuilder b) {
        setReportBuilder(b);
        return this;
    }

    @Override
    protected RetryStore resolveRetryStoreForRequest(AdaptrisMessage msg) {
        return getRetryStore();
    }

    @Override
    protected Collection<RetryStore> getConfiguredRetryStores() {
        return Collections.singleton(getRetryStore());
    }

    @Override
    protected void validateRetryStoreConfiguration() {
        Args.notNull(getRetryStore(), "retry-store");
    }
}
