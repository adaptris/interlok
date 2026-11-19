package com.adaptris.core;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import org.apache.commons.lang3.BooleanUtils;

/**
 * Abstract class that generalises behaviour for subclasses that generate events
 */
public abstract class AbstractMessageEventGenerator implements MessageEventGenerator {

    @AdvancedConfig(rare = true)
    @InputFieldDefault(value = "false")
    private Boolean successOnFail;


    @Override
    public boolean successOnFailure() {
        return BooleanUtils.toBooleanDefaultIfNull(getSuccessOnFail(), false);
    }

    /**
     * @return whether or not this service is configured to be marked as successful
     * even if failure is encountered.
     * @see #successOnFailure()
     */
    public Boolean getSuccessOnFail() {
        return successOnFail;
    }

    /**
     * whether or not this service is configured to be marked as successful
     * even if failure is encountered.
     *
     * @param b true/false, default if not specified is false.
     */
    public void setSuccessOnFail(Boolean b) {
        successOnFail = b;
    }
}
