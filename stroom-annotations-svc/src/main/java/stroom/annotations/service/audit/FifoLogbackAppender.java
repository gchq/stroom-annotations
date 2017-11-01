package stroom.annotations.service.audit;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterReply;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FifoLogbackAppender<E> extends ContextAwareBase implements Appender<E> {
    private static final ConcurrentLinkedQueue<Object> logs = new ConcurrentLinkedQueue<>();

    private String name;

    public static List<Object> popLogs() {
        final List<Object> extracted = new ArrayList<>(logs);
        logs.clear();
        return extracted;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void doAppend(E e) throws LogbackException {
        logs.add(e);
    }

    @Override
    public void setName(String s) {
        this.name = s;
    }

    @Override
    public void addFilter(Filter<E> filter) {

    }

    @Override
    public void clearAllFilters() {

    }

    @Override
    public List<Filter<E>> getCopyOfAttachedFiltersList() {
        return null;
    }

    @Override
    public FilterReply getFilterChainDecision(E e) {
        return FilterReply.ACCEPT;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }
}
