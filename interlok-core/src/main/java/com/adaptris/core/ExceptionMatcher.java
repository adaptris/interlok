package com.adaptris.core;

import javax.annotation.RegEx;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public interface ExceptionMatcher {
    boolean matches(Exception exception);

    enum MatchSource {
        Message(exception -> exception.getMessage() != null ? exception.getMessage() : ""),
        ClassName(exception -> exception.getClass().getCanonicalName())
        ;
        private Function<Exception, String> sourceGetter;

        MatchSource(Function<Exception, String> sourceGetter) {
            this.sourceGetter = sourceGetter;
        }

        public String getSource(Exception exception) {
            return sourceGetter.apply(exception);
        }
    }

    class Regex implements ExceptionMatcher {
        private List<MatchSource> sources;
        private String regex;
        private transient Pattern compiled;

        public String getRegex() {
            return this.regex;
        }

        public void setRegex(@RegEx String regex) {
            this.compiled = Pattern.compile(regex);
            this.regex = regex;
        }

        public List<MatchSource> getSources() {
            return sources;
        }

        public void setSources(List<MatchSource> sources) {
            this.sources = sources;
        }

        @Override
        public boolean matches(Exception exception) {
            return sources.stream().anyMatch(source -> source.getSource(exception) != null && compiled.matcher(source.getSource(exception)).matches());
        }
    }

    class InstanceOf implements ExceptionMatcher {
        Class<? extends Exception> type;

        public Class<? extends Exception> getType() {
            return type;
        }

        public void setType(Class<? extends Exception> type) {
            this.type = type;
        }

        @Override
        public boolean matches(Exception exception) {
            return type.isAssignableFrom(exception.getClass());
        }
    }
}
