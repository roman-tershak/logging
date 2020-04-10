package rt.tests.accumappender;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.message.SimpleMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static org.apache.logging.log4j.Level.ERROR;
import static rt.tests.accumappender.Markers.*;


@Plugin(name = "Accumulating", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class AccumulatingAppender extends AbstractAppender {

    private static volatile AccumulatingAppender instance;
    private static final ThreadLocal<List<LogEvent>> logs = ThreadLocal.withInitial(() -> new ArrayList<>());

    private final Configuration config;
    private final ConcurrentMap<String, AppenderControl> appenders = new ConcurrentHashMap<>();
    private final RewritePolicy rewritePolicy;
    private final AppenderRef[] appenderRefs;
    private final Level passThroughLevel;
    private final boolean passExceptions;


    /**
     * Creates a AccumulatingAppender.
     *
     * @param name          The name of the Appender.
     * @param ignore        If {@code "true"} (default) exceptions encountered when appending events are logged; otherwise
     *                      they are propagated to the caller.
     * @param appenderRefs  An array of Appender names to call.
     * @param config        The Configuration.
     * @param rewritePolicy The policy to use to modify the event.
     * @param filter        A Filter to filter events.
     * @return The created AccumulatingAppender.
     */
    @PluginFactory
    public static AccumulatingAppender createAppender(
            @PluginAttribute("name") final String name,
            @PluginAttribute("passThroughLevel") final String passThroughLevel,
            @PluginAttribute("ignoreExceptions") final String ignore,   // TODO implement later
            @PluginAttribute("passExceptionThrough") final String passExceptionThrough,
            @PluginElement("AppenderRef") final AppenderRef[] appenderRefs,
            @PluginConfiguration final Configuration config,
            @PluginElement("RewritePolicy") final RewritePolicy rewritePolicy, // TODO implement later
            @PluginElement("Filter") final Filter filter) { // TODO implement later

        final boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
        final boolean passExceptions = Booleans.parseBoolean(passExceptionThrough, true);
        if (name == null) {
            LOGGER.error("No name provided for AccumulatingAppender");
            return null;
        }
        if (appenderRefs == null) {
            LOGGER.error("No appender references defined for AccumulatingAppender");
            return null;
        }

        Level passThroughlevel = Level.toLevel(passThroughLevel, ERROR);

        instance = new AccumulatingAppender(name, passThroughlevel, ignoreExceptions, passExceptions,
                appenderRefs, filter, rewritePolicy, config, null);
        return instance;
    }

    private AccumulatingAppender(final String name, final Level passThroughLevel,
                                 final boolean ignoreExceptions, final boolean passExceptions,
                                 final AppenderRef[] appenderRefs,
                                 final Filter filter,
                                 final RewritePolicy rewritePolicy,
                                 final Configuration config, final Property[] properties) {
        super(name, filter, null, ignoreExceptions, properties);

        this.config = config;
        this.passThroughLevel = passThroughLevel;
        this.passExceptions = passExceptions;
        this.rewritePolicy = rewritePolicy;
        this.appenderRefs = appenderRefs;
    }

    @Override
    public void start() {
        clearLogEvents();

        for (final AppenderRef ref : appenderRefs) {
            final String name = ref.getRef();
            final Appender appender = config.getAppender(name);
            if (appender != null) {
                final Filter filter = appender instanceof AbstractAppender ?
                        ((AbstractAppender) appender).getFilter() : null;
                appenders.put(name, new AppenderControl(appender, ref.getLevel(), filter));
            } else {
                LOGGER.error("Appender " + ref + " cannot be located. Reference ignored");
            }
        }

        super.start();
    }

    @Override
    public boolean stop(long timeout, TimeUnit timeUnit) {
        clearLogEvents();
        return super.stop(timeout, timeUnit);
    }

    @Override
    public void stop() {
        clearLogEvents();
        super.stop();
    }

    private void clearLogEvents() {
        logs.remove();
    }

    @Override
    public void append(LogEvent event) {

//        if (rewritePolicy != null) {
//            event = rewritePolicy.rewrite(event);
//        }
        logs.get().add(event.toImmutable());    // TODO wrap it

        Marker marker = event.getMarker();

        if (marker == SUCCESS) {
            logAccumulatedEvents(event);
        } else if (marker == FAIL) {
            logAccumulatedEvents(event);
        } else if (event.getLevel().isMoreSpecificThan(passThroughLevel)) {
            propagateEventFurther(event);
        } else if (event.getThrown() != null && this.passExceptions) {
            propagateEventFurther(event);
        }
    }

    private void propagateEventFurther(LogEvent event) {
        for (final AppenderControl control : appenders.values()) {
            control.callAppender(event);
        }
    }

    private void logAccumulatedEvents(LogEvent logEvent) {
        List<LogEvent> logEvents = logs.get();
        logs.remove();

        StringBuilder sb = new StringBuilder();
        LogEvent lastWithThrown = null;
        for (LogEvent event : logEvents) {
            if (sb.length() > 0)
                sb.append(" : ");

            sb.append(event.getMessage().getFormattedMessage());

            if (!this.passExceptions) {
                if (event.getThrown() != null) {
                    lastWithThrown = event;
                }
            }
        }
        String formattedMessage = sb.toString();

        if (logEvent.getThrown() != null) {
            lastWithThrown = logEvent;
        }

        LogEvent eventToLog = makeLogEventCopy(logEvent, formattedMessage, lastWithThrown);
        propagateEventFurther(eventToLog);
    }

    private LogEvent makeLogEventCopy(LogEvent event, String formattedMessage, LogEvent lastWithThrown) {
        Log4jLogEvent.Builder builder = Log4jLogEvent.newBuilder()
                .setMarker(event.getMarker())
                .setLevel(event.getLevel())
                .setMessage(new SimpleMessage(formattedMessage))
                .setLoggerName(event.getLoggerName())
                .setInstant(event.getInstant())
                .setTimeMillis(event.getTimeMillis())
                .setNanoTime(event.getNanoTime())
                .setContextData(null)   // TODO
                .setContextStack(null)  // TODO
                .setEndOfBatch(event.isEndOfBatch())
                .setIncludeLocation(event.isIncludeLocation())
                .setLoggerFqcn(event.getLoggerFqcn())
                .setSource(event.getSource())
                .setThreadId(event.getThreadId())
                .setThreadName(event.getThreadName())
                .setThreadPriority(event.getThreadPriority());

        if (lastWithThrown != null) {
            builder
                    .setThrown(lastWithThrown.getThrown())
                    .setThrownProxy(lastWithThrown.getThrownProxy());
        }
        return builder.build();
    }












}
