package rt.tests.accumappender;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import rt.tests.logger.ExtLogger;

@Plugin(name = "ExtLoggerFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE, printObject = true)
public class ExtLoggerFilter extends AbstractFilter {

    @PluginBuilderFactory
    public static ExtLoggerFilter.Builder newExtLoggerFilterBuilder() {
        return new ExtLoggerFilter.Builder();
    }

    public ExtLoggerFilter(Result onMatch, Result onMismatch) {
        super(onMatch, onMismatch);
    }

    @Override
    public Result filter(LogEvent event) {
        return filterInternal(event);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return filterInternal(logger);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return filterInternal(logger);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return filterInternal(logger);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0) {
        return filterInternal(logger);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1) {
        return filterInternal(logger);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2) {
        return filterInternal(logger);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3) {
        return filterInternal(logger);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return filterInternal(logger);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return filterInternal(logger);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return filterInternal(logger);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return filterInternal(logger);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return filterInternal(logger);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return filterInternal(logger);
    }

    protected Result filterInternal(LogEvent event) {
        return isMatch(event)
                ? getOnMatch() 
                : getOnMismatch();
    }

    protected boolean isMatch(LogEvent event) {
        return ExtLogger.FQCN.equals(event.getLoggerFqcn());
    }

    protected Result filterInternal(Logger logger) {
        return isMatch(logger)
                ? getOnMatch() 
                : getOnMismatch();
    }

    protected boolean isMatch(Logger logger) {
        return ExtLogger.class.isAssignableFrom(logger.getClass());
    }

    protected static class Builder extends AbstractFilterBuilder<Builder> implements org.apache.logging.log4j.core.util.Builder<ExtLoggerFilter> {
        @Override
        public ExtLoggerFilter build() {
            return new ExtLoggerFilter(this.getOnMatch(), this.getOnMismatch());
        }
    }
}
