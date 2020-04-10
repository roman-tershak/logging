package rt.tests.accumappender;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;

@Plugin(name = "NoExtLoggerFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE, printObject = true)
public class NoExtLoggerFilter extends ExtLoggerFilter {

    @PluginBuilderFactory
    public static NoExtLoggerFilter.Builder newNoExtLoggerFilterBuilder() {
        return new NoExtLoggerFilter.Builder();
    }

    public NoExtLoggerFilter(Result onMatch, Result onMismatch) {
        super(onMatch, onMismatch);
    }

    @Override
    protected boolean isMatch(LogEvent event) {
        return !super.isMatch(event);
    }

    @Override
    protected boolean isMatch(Logger logger) {
        return !super.isMatch(logger);
    }

    protected static class Builder extends AbstractFilterBuilder<Builder> implements org.apache.logging.log4j.core.util.Builder<NoExtLoggerFilter> {
        @Override
        public NoExtLoggerFilter build() {
            return new NoExtLoggerFilter(this.getOnMatch(), this.getOnMismatch());
        }
    }
}
