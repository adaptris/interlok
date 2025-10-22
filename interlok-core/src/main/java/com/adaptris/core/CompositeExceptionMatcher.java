package com.adaptris.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * An exception matcher which allows multiple exception matchers to be configured and matches any
 */
@XStreamAlias("composite-exception-matcher")
public class CompositeExceptionMatcher implements ExceptionMatcher {

    @XStreamImplicit
    List<ExceptionMatcher> matchers;

    public boolean matches(Exception exception) {
        return matchers.stream().anyMatch(m -> m.matches(exception));
    }

    public List<ExceptionMatcher> getMatchers() {
        return matchers;
    }

    public void setMatchers(List<ExceptionMatcher> matchers) {
        this.matchers = matchers;
    }
}
