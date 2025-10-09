package com.adaptris.core;

import java.util.List;

public class CompositeExceptionMatcher implements ExceptionMatcher {
    List<ExceptionMatcher> matchers;

    public boolean matches(Exception exception) {
        return matchers.stream().anyMatch(m -> m.matches(exception));
    }
}
