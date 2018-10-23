package com.adaptris.core.services.splitter;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class MetadataFlagPoolingFutureExceptionStrategy implements PoolingFutureExceptionStrategy{
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
            if (Boolean.valueOf(metadataValue) || "1".equals(metadataValue))
                return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
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
