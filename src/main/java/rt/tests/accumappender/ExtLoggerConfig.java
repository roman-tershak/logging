package rt.tests.accumappender;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;
import java.util.List;

@Plugin(name = "ExtLogger", category = Node.CATEGORY, printObject = true)
public class ExtLoggerConfig extends LoggerConfig {

    public ExtLoggerConfig(String name, List<AppenderRef> appenderRefs, Filter filter, Level level, boolean additivity,
                           Property[] properties, Configuration config, boolean includeLocation) {
        super(name, appenderRefs, filter, level, additivity, properties, config, includeLocation);
    }

    /**
     * Factory method to create a LoggerConfig.
     *
     * @param additivity True if additive, false otherwise.
     * @param level The Level to be associated with the Logger.
     * @param loggerName The name of the Logger.
     * @param includeLocation "true" if location should be passed downstream
     * @param refs An array of Appender names.
     * @param properties Properties to pass to the Logger.
     * @param config The Configuration.
     * @param filter A Filter.
     * @return A new LoggerConfig.
     * @since 3.0
     */
    @PluginFactory
    public static LoggerConfig createLogger(
            @PluginAttribute(value = "additivity", defaultBoolean = true) final boolean additivity,
            @PluginAttribute("level") final Level level,
            @Required(message = "Loggers cannot be configured without a name") @PluginAttribute("name") final String loggerName,
            @PluginAttribute("includeLocation") final String includeLocation,
            @PluginElement("AppenderRef") final AppenderRef[] refs,
            @PluginElement("Properties") final Property[] properties,
            @PluginConfiguration final Configuration config,
            @PluginElement("Filter") final Filter filter) {
        final String name = loggerName.equals(ROOT) ? Strings.EMPTY : loggerName;
        return new ExtLoggerConfig(name, Arrays.asList(refs), filter, level, additivity, properties, config,
                includeLocation(includeLocation, config));
    }


}
