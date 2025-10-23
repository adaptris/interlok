package com.adaptris.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.extern.slf4j.Slf4j;

/**
 * An Exception matcher that tests whether an Exception's is an instanceof the configured
 * Exception class.
 */
@Slf4j
@XStreamAlias("instance-of-exception-matcher")
public class InstanceOfExceptionMatcher extends AbstractExceptionMatcher {
    Class<? extends Exception> clazz;

    public Class<? extends Exception> getClazz() {
        return clazz;
    }

    public void setClazz(Class<? extends Exception> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean matches(Exception exception) {
        if (exception != null && clazz != null) return clazz.isAssignableFrom(exception.getClass());
        else return false;
    }
}