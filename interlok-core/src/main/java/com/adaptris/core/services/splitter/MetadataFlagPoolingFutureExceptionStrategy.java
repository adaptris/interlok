package com.adaptris.core.services.splitter;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.BooleanUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Ignores exception so long as some messages were considered successful based on a metadata key.
 * 
 * <p>
 * This strategy is useful if messages within a split-join are transient, and can be ignored provided some of them work; it allows
 * you to ignore exceptions processing individual mesages provided one or more messages have set a specific metadata to the value
 * {@code true | 1}.
 * </p>
 * 
 * @config metadata-flag-pooling-future-exception-strategy
 *
 */
@XStreamAlias("metadata-flag-pooling-future-exception-strategy")
public class MetadataFlagPoolingFutureExceptionStrategy implements PoolingFutureExceptionStrategy {
    @NotNull
    @Valid
    private String metadataFlagKey;

    @NotNull
    @Valid
    private boolean defaultFlagValue = false;


    @Override
    public void handle(ServiceExceptionHandler handler, List<Future<AdaptrisMessage>> results) throws CoreException {
        validateSettings();
        int cancelledResults = 0;
        int raisedFlagCount = 0;
        for (Future<AdaptrisMessage> future : results) {
            if (getFutureMetadataFlag(future)) raisedFlagCount++;
            if (future.isCancelled()) cancelledResults++;
        }
        // If there are any positive flags then suppress any exceptions
        if (raisedFlagCount > 0)
            return;
        // Call default exception handler
        else
            handler.throwFirstException();
        // Handle any cancelled jobs
        if (cancelledResults > 0)
            throw new CoreException("Timeout exceeded waiting for job completion.");
    }

    private boolean getFutureMetadataFlag(Future<AdaptrisMessage> msgResult) {
        try {
            AdaptrisMessage adaptrisMessage = msgResult.get();
            String metadataValue = adaptrisMessage.getMetadataValue(metadataFlagKey);
            if (BooleanUtils.toBoolean(metadataValue) || "1".equals(metadataValue))
                return true;
        } catch (InterruptedException | ExecutionException  ignored) {
            ;
        }
        return defaultFlagValue;
    }

    private boolean determineSuccess() {
        return true;
    }

    private void validateSettings() throws CoreException {
        try {
            Args.notNull(getMetadataFlagKey(), "metadataFlagKey");
        } catch (Exception e) {
            throw ExceptionHelper.wrapCoreException(e);
        }
    }

    public void setMetadataFlagKey(String metadataKey) {
        this.metadataFlagKey = Args.notNull(metadataKey, "metadataFlagKey");
    }

    private String getMetadataFlagKey() {
        return this.metadataFlagKey;
    }

}
