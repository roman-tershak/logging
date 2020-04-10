package rt.tests.accumappender;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.logging.log4j.Level.*;
import static org.apache.logging.log4j.core.LifeCycle.State.STARTED;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static rt.tests.accumappender.Markers.FAIL;
import static rt.tests.accumappender.Markers.SUCCESS;

public class AccumulatingAppenderTest {

    private Appender mockAppender;
    private Configuration mockConfiguration;

    @Before
    public void before() {
        mockAppender = createMockAppender();
        mockConfiguration = createMockConfiguration(mockAppender);
    }

    @After
    public void after() {
        reset(mockAppender, mockConfiguration);
    }

    @Test
    public void testAppenderNameIsReturned() {
        AccumulatingAppender unit = getUnit("accuApp");

        assertThat(unit.getName(), equalTo("accuApp"));
    }

    @Test
    public void testSuccessMarkerIsPassedThrough() {
        AccumulatingAppender unit = getUnit();

        Log4jLogEvent logEvent = createLogInfoEvent("Info message", SUCCESS);

        unit.append(logEvent);

        verifyLogEvents(INFO, "Info message", SUCCESS);
    }

    @Test
    public void testFailMarkerIsPassedThrough() {
        AccumulatingAppender unit = getUnit();

        Log4jLogEvent logEvent = createLogErrorEvent("Error message", FAIL);

        unit.append(logEvent);

        verifyLogEvents(ERROR, "Error message", FAIL);
    }

    @Test
    public void testLogEventMessagesArePassedThroughBySuccessMarker() {
        AccumulatingAppender unit = getUnit();

        unit.append(createLogInfoEvent("Info message one"));
        unit.append(createLogInfoEvent("Info message two", SUCCESS));

        verifyExactLogEvents(INFO, "Info message one : Info message two", SUCCESS);
    }

    @Test
    public void testVariousLogEventMessagesArePassedThroughBySuccessMarker() {
        AccumulatingAppender unit = getUnit();

        unit.append(createLogTraceEvent("Trace message"));
        unit.append(createLogDebugEvent("Debug message"));
        unit.append(createLogWarnEvent("Warn message"));
        unit.append(createLogInfoEvent("Info message", SUCCESS));

        verifyExactLogEvents(INFO, "Trace message : Debug message : Warn message : Info message", SUCCESS);
    }

    @Test
    public void testVariousLogEventMessagesArePassedThroughWithFailMarker() {
        AccumulatingAppender unit = getUnit();

        unit.append(createLogTraceEvent("Trace message"));
        unit.append(createLogDebugEvent("Debug message"));
        unit.append(createLogDebugEvent("Info message"));
        unit.append(createLogWarnEvent("Warn message"));
        unit.append(createLogInfoEvent("Fail message", FAIL));

        verifyExactLogEvents(INFO, "Trace message : Debug message : Info message : Warn message : Fail message", FAIL);
    }

    @Test
    public void testStartClearsPreviousLogEvents() {
        AccumulatingAppender unit = getUnit();

        unit.append(createLogInfoEvent("Info message"));
        unit.start();

        unit.append(createLogInfoEvent("Info message 1"));
        unit.append(createLogInfoEvent("Info message 2", SUCCESS));

        verifyExactLogEvents(INFO,"Info message 1 : Info message 2", SUCCESS);
    }

    @Test
    public void testStopClearsPreviousLogEvents() {
        AccumulatingAppender unit = getUnit();

        unit.append(createLogInfoEvent("Info message"));

        unit.stop();
        unit.start();

        unit.append(createLogInfoEvent("Info message 1"));
        unit.append(createLogInfoEvent("Info message 2", SUCCESS));

        verifyExactLogEvents(INFO,"Info message 1 : Info message 2", SUCCESS);
    }

    @Test
    public void testStop2ClearsPreviousLogEvents() {
        AccumulatingAppender unit = getUnit();

        unit.append(createLogInfoEvent("Info message"));

        unit.stop(0L, TimeUnit.MILLISECONDS);
        unit.start();

        unit.append(createLogInfoEvent("Info message 1"));
        unit.append(createLogInfoEvent("Info message 2", SUCCESS));

        verifyExactLogEvents(INFO,"Info message 1 : Info message 2", SUCCESS);
    }

    @Test
    public void testErrorLogEventsArePassedThrough() {
        AccumulatingAppender unit = getUnit("AccuApp", "ERROR");

        unit.append(createLogTraceEvent("Trace message"));
        unit.append(createLogDebugEvent("Debug message"));
        unit.append(createLogDebugEvent("Info message"));
        unit.append(createLogWarnEvent("Warn message"));
        unit.append(createLogErrorEvent("Error message"));
        unit.append(createLogFatalEvent("Fatal message"));

        verifyExactLogEvents(ERROR, "Error message", FATAL, "Fatal message");
    }

    @Test
    public void testTheDefaultPassThroughLevelIsError() {
        AccumulatingAppender unit = getUnit("AccuApp", null);

        unit.append(createLogTraceEvent("Trace message"));
        unit.append(createLogDebugEvent("Debug message"));
        unit.append(createLogDebugEvent("Info message"));
        unit.append(createLogWarnEvent("Warn message"));
        unit.append(createLogErrorEvent("Error message"));
        unit.append(createLogFatalEvent("Fatal message"));

        verifyExactLogEvents(ERROR, "Error message", FATAL, "Fatal message");
    }

    @Test
    public void testTheDefaultPassThroughLevelIsErrorWithEmptyLevelSpecified() {
        AccumulatingAppender unit = getUnit("AccuApp", "");

        unit.append(createLogTraceEvent("Trace message"));
        unit.append(createLogDebugEvent("Debug message"));
        unit.append(createLogDebugEvent("Info message"));
        unit.append(createLogWarnEvent("Warn message"));
        unit.append(createLogErrorEvent("Error message"));
        unit.append(createLogFatalEvent("Fatal message"));

        verifyExactLogEvents(ERROR, "Error message", FATAL, "Fatal message");
    }

    @Test
    public void testLogEventsArePassedThroughWithWarnPassThroughLevel() {
        AccumulatingAppender unit = getUnit("AccuApp", "WARN");

        unit.append(createLogTraceEvent("Trace message"));
        unit.append(createLogDebugEvent("Debug message"));
        unit.append(createLogDebugEvent("Info message"));
        unit.append(createLogWarnEvent("Warn message"));
        unit.append(createLogErrorEvent("Error message"));
        unit.append(createLogFatalEvent("Fatal message"));

        verifyExactLogEvents(WARN, "Warn message", ERROR, "Error message", FATAL, "Fatal message");
    }

    @Test
    public void testLogEventsArePassedThroughWithInfoPassThroughLevel() {
        AccumulatingAppender unit = getUnit("AccuApp", "INFO");

        unit.append(createLogTraceEvent("Trace message"));
        unit.append(createLogDebugEvent("Debug message"));
        unit.append(createLogInfoEvent("Info message"));
        unit.append(createLogWarnEvent("Warn message"));
        unit.append(createLogErrorEvent("Error message"));
        unit.append(createLogFatalEvent("Fatal message"));

        verifyExactLogEvents(INFO, "Info message", WARN, "Warn message",
                ERROR, "Error message", FATAL, "Fatal message");
    }

    @Test
    public void testLogEventsArePassedThroughWithDebugPassThroughLevel() {
        AccumulatingAppender unit = getUnit("AccuApp", "DEBUG");

        unit.append(createLogTraceEvent("Trace message"));
        unit.append(createLogDebugEvent("Debug message"));
        unit.append(createLogInfoEvent("Info message"));
        unit.append(createLogWarnEvent("Warn message"));
        unit.append(createLogErrorEvent("Error message"));
        unit.append(createLogFatalEvent("Fatal message"));

        verifyExactLogEvents(DEBUG, "Debug message", INFO, "Info message",
                WARN, "Warn message", ERROR, "Error message",
                FATAL, "Fatal message");
    }

    @Test
    public void testLogEventsArePassedThroughWithTracePassThroughLevel() {
        AccumulatingAppender unit = getUnit("AccuApp", "TRACE");

        unit.append(createLogTraceEvent("Trace message"));
        unit.append(createLogDebugEvent("Debug message"));
        unit.append(createLogInfoEvent("Info message"));
        unit.append(createLogWarnEvent("Warn message"));
        unit.append(createLogErrorEvent("Error message"));
        unit.append(createLogFatalEvent("Fatal message"));

        verifyExactLogEvents(TRACE, "Trace message", DEBUG, "Debug message",
                INFO, "Info message", WARN, "Warn message",
                ERROR, "Error message", FATAL, "Fatal message");
    }

    @Test
    public void testMultipleMarkersLogMessagesBetweenMarkers() {
        AccumulatingAppender unit = getUnit();

        unit.append(createLogInfoEvent("Info 1"));
        unit.append(createLogInfoEvent("Info 2", SUCCESS));

        unit.append(createLogInfoEvent("Info 3"));
        unit.append(createLogInfoEvent("Info 4", SUCCESS));

        verifyExactLogEvents(A(
                A(INFO, "Info 1 : Info 2", SUCCESS),
                A(INFO, "Info 3 : Info 4", SUCCESS)));
    }

    @Test
    public void testThrowablesArePassedThroughIfPassExceptionThroughIsSetToTrue() {
        AccumulatingAppender unit = getUnit("AccuApp", "ERROR", "true");

        unit.append(createLogInfoEvent("Info message 1"));
        try {
            throw new Exception("An Exception");
        } catch (Exception e) {
            Log4jLogEvent logWarnEvent = createLogEvent("Logger","Warning!", WARN, e);

            unit.append(logWarnEvent);

            verifyExactLogEvents(WARN, "Warning!", e);
        }
    }

    @Test
    public void testThrowablesAreNotPassedThroughIfPassExceptionThroughIsSetToFalse() {
        AccumulatingAppender unit = getUnit("AccuApp", "ERROR", "false");

        unit.append(createLogInfoEvent("Info message 1"));
        try {
            throw new Exception("An Exception");
        } catch (Exception e) {
            unit.append(createLogEvent("Logger","Warning!", WARN, e));
        }
        verifyNoLogEvents();
    }

    @Test
    public void testTheDefaultPassExceptionThroughIsTrue() {
        AccumulatingAppender unit = getUnit("AccuApp", "ERROR");

        unit.append(createLogInfoEvent("Info message 1"));
        try {
            throw new Exception("Some Exception");
        } catch (Exception e) {
            unit.append(createLogEvent("Logger","Warning!", WARN, e));

            verifyExactLogEvents(WARN, "Warning!", e);
        }
    }

    @Test
    public void testThrowablesArePassedThroughWithFailMarkerAndExceptionInTheLastLogEvent() {
        AccumulatingAppender unit = getUnit("AccuApp", "ERROR");

        unit.append(createLogInfoEvent("Info message 1"));
        try {
            throw new Exception("An Exception");
        } catch (Exception e) {
            unit.append(createLogEvent("Logger","Error!", ERROR, FAIL, e));

            verifyExactLogEvents(ERROR, "Info message 1 : Error!", FAIL, e);
        }
    }

    @Test
    public void testThrowablesArePassedThroughWithFailMarkerAndExceptionNotInTheLastLogEvent() {
        AccumulatingAppender unit = getUnit("AccuApp", "ERROR");

        unit.append(createLogInfoEvent("Info message 1"));
        try {
            throw new Exception("An Exception");
        } catch (Exception e) {
            unit.append(createLogEvent("Logger","Warning!", WARN, e));

            unit.append(createLogEvent("Logger","Error!", ERROR, FAIL));

            verifyExactLogEvents(A(
                    A(WARN, "Warning!", null, e),
                    A(ERROR, "Info message 1 : Warning! : Error!", FAIL)));
        }
    }

    @Test
    public void testFormattedLogEventMessagesWithSuccessMarker() {
        AccumulatingAppender unit = getUnit();

        unit.append(createLogEvent("Logger", TRACE, "Trace message {} {}", 1, "two"));
        unit.append(createLogEvent("Logger", DEBUG, "Debug message {}", DEBUG));
        unit.append(createLogEvent("Logger", WARN, "Warn message {}"));
        unit.append(createLogEvent("Logger", INFO, SUCCESS, "Info message {}", SUCCESS + "!"));

        verifyExactLogEvents(INFO, "Trace message 1 two : Debug message DEBUG : Warn message {} : Info message SUCCESS!", SUCCESS);
    }

    @Test
    public void testFormattedLogEventMessagesWithFailMarker() {
        AccumulatingAppender unit = getUnit();

        unit.append(createLogEvent("Logger", TRACE, "Trace message {} {}", 1, "two"));
        unit.append(createLogEvent("Logger", DEBUG, "Debug message {}", DEBUG));
        unit.append(createLogEvent("Logger", WARN, "Warn message {}"));
        unit.append(createLogEvent("Logger", INFO, "Info message {}", SUCCESS + "!"));
        unit.append(createLogEvent("Logger", INFO, FAIL, "Fail message {}", FAIL + "!"));

        verifyExactLogEvents(INFO, "Trace message 1 two : Debug message DEBUG : Warn message {} : Info message SUCCESS! : Fail message FAIL!", FAIL);
    }

    private void verifyLogEvents(Level expLevel, String expMessage, Markers expMarker) {
        List<LogEvent> logEvents = captureLogEvents(mockAppender);
        assertThat(logEvents.size(), greaterThan(0));

        verifyLogEvent(logEvents.get(0), expLevel, expMessage, expMarker);
    }

    private void verifyExactLogEvents(Level expLevel, String expMessage, Markers expMarker) {
        List<LogEvent> logEvents = captureLogEvents(mockAppender, 1);
        assertThat(logEvents.size(), equalTo(1));

        verifyLogEvent(logEvents.get(0), expLevel, expMessage, expMarker);
    }

    private void verifyExactLogEvents(Level expLevel, String expMessage, Throwable t) {
        List<LogEvent> logEvents = captureLogEvents(mockAppender, 1);
        assertThat(logEvents.size(), equalTo(1));

        verifyLogEvent(logEvents.get(0), expLevel, expMessage, null, t);
    }

    private void verifyExactLogEvents(Level expLevel, String expMessage, Markers expMarker, Throwable t) {
        List<LogEvent> logEvents = captureLogEvents(mockAppender, 1);
        assertThat(logEvents.size(), equalTo(1));

        verifyLogEvent(logEvents.get(0), expLevel, expMessage, expMarker, t);
    }

    private void verifyExactLogEvents(Object[] expEvents) {
        List<LogEvent> logEvents = captureLogEvents(mockAppender, expEvents.length);
        assertThat(logEvents.size(), equalTo(expEvents.length));

        if (expEvents.length == 1) {
            verifyLogEvent(logEvents.get(0), expEvents);
        }
        for (int i = 0; i < expEvents.length; i++) {
            verifyLogEvent(logEvents.get(i), (Object[]) expEvents[i]);
        }
    }

    private void verifyNoLogEvents() {
        List<LogEvent> logEvents = captureLogEvents(mockAppender, 0);
        assertThat(logEvents.size(), equalTo(0));
    }

    private void verifyExactLogEvents(Level expLevel, String expMessage) {
        List<LogEvent> logEvents = captureLogEvents(mockAppender, 1);
        assertThat(logEvents.size(), equalTo(1));

        verifyLogEvent(logEvents.get(0), expLevel, expMessage);
    }

    private void verifyExactLogEvents(Level expLevel1, String expMessage1,
                                      Level expLevel2, String expMessage2) {
        List<LogEvent> logEvents = captureLogEvents(mockAppender, 2);
        assertThat(logEvents.size(), equalTo(2));

        verifyLogEvent(logEvents.get(0), expLevel1, expMessage1);
        verifyLogEvent(logEvents.get(1), expLevel2, expMessage2);
    }

    private void verifyExactLogEvents(Level expLevel1, String expMessage1,
                                      Level expLevel2, String expMessage2,
                                      Level expLevel3, String expMessage3) {
        List<LogEvent> logEvents = captureLogEvents(mockAppender, 3);
        assertThat(logEvents.size(), equalTo(3));

        verifyLogEvent(logEvents.get(0), expLevel1, expMessage1);
        verifyLogEvent(logEvents.get(1), expLevel2, expMessage2);
        verifyLogEvent(logEvents.get(2), expLevel3, expMessage3);
    }

    private void verifyExactLogEvents(Level expLevel1, String expMessage1,
                                      Level expLevel2, String expMessage2,
                                      Level expLevel3, String expMessage3,
                                      Level expLevel4, String expMessage4) {
        List<LogEvent> logEvents = captureLogEvents(mockAppender, 4);
        assertThat(logEvents.size(), equalTo(4));

        verifyLogEvent(logEvents.get(0), expLevel1, expMessage1);
        verifyLogEvent(logEvents.get(1), expLevel2, expMessage2);
        verifyLogEvent(logEvents.get(2), expLevel3, expMessage3);
        verifyLogEvent(logEvents.get(3), expLevel4, expMessage4);
    }

    private void verifyExactLogEvents(Level expLevel1, String expMessage1,
                                      Level expLevel2, String expMessage2,
                                      Level expLevel3, String expMessage3,
                                      Level expLevel4, String expMessage4,
                                      Level expLevel5, String expMessage5) {
        List<LogEvent> logEvents = captureLogEvents(mockAppender, 5);
        assertThat(logEvents.size(), equalTo(5));

        verifyLogEvent(logEvents.get(0), expLevel1, expMessage1);
        verifyLogEvent(logEvents.get(1), expLevel2, expMessage2);
        verifyLogEvent(logEvents.get(2), expLevel3, expMessage3);
        verifyLogEvent(logEvents.get(3), expLevel4, expMessage4);
        verifyLogEvent(logEvents.get(4), expLevel5, expMessage5);
    }

    private void verifyExactLogEvents(Level expLevel1, String expMessage1,
                                      Level expLevel2, String expMessage2,
                                      Level expLevel3, String expMessage3,
                                      Level expLevel4, String expMessage4,
                                      Level expLevel5, String expMessage5,
                                      Level expLevel6, String expMessage6) {
        List<LogEvent> logEvents = captureLogEvents(mockAppender, 6);
        assertThat(logEvents.size(), equalTo(6));

        verifyLogEvent(logEvents.get(0), expLevel1, expMessage1);
        verifyLogEvent(logEvents.get(1), expLevel2, expMessage2);
        verifyLogEvent(logEvents.get(2), expLevel3, expMessage3);
        verifyLogEvent(logEvents.get(3), expLevel4, expMessage4);
        verifyLogEvent(logEvents.get(4), expLevel5, expMessage5);
        verifyLogEvent(logEvents.get(5), expLevel6, expMessage6);
    }

    private void verifyLogEvent(LogEvent logEvent, Level expLevel, String expMessage) {
        assertThat(logEvent, is(notNullValue()));
        assertThat(logEvent.getLevel(), equalTo(expLevel));
        assertThat(logEvent.getMessage().getFormattedMessage(), equalTo(expMessage));
        assertThat(logEvent.getMarker(), is(nullValue()));
    }

    private void verifyLogEvent(LogEvent logEvent, Level expLevel, String expMessage, Markers expMarker) {
        assertThat(logEvent, is(notNullValue()));
        assertThat(logEvent.getLevel(), equalTo(expLevel));
        assertThat(logEvent.getMessage().getFormattedMessage(), equalTo(expMessage));
        assertThat(logEvent.getMarker(), equalTo(expMarker));
    }

    private void verifyLogEvent(LogEvent logEvent, Level expLevel, String expMessage, Markers expMarker, Throwable t) {
        assertThat(logEvent, is(notNullValue()));
        assertThat(logEvent.getLevel(), equalTo(expLevel));
        assertThat(logEvent.getMessage().getFormattedMessage(), equalTo(expMessage));
        assertThat(logEvent.getMarker(), equalTo(expMarker));
        assertThat(logEvent.getThrown(), equalTo(t));
    }

    private void verifyLogEvent(LogEvent logEvent, Object[] expEvent) {
        if (expEvent.length == 2) {
            verifyLogEvent(logEvent, (Level) expEvent[0], (String) expEvent[1]);
        } else if (expEvent.length == 3) {
            verifyLogEvent(logEvent, (Level) expEvent[0], (String) expEvent[1], (Markers) expEvent[2]);
        } else if (expEvent.length == 4) {
            verifyLogEvent(logEvent, (Level) expEvent[0], (String) expEvent[1], (Markers) expEvent[2], (Throwable) expEvent[3]);
        }
    }

    private List<LogEvent> captureLogEvents(Appender mockAppender) {
        ArgumentCaptor<LogEvent> logEventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(mockAppender, atLeast(0)).append(logEventCaptor.capture());
        return logEventCaptor.getAllValues();
    }

    private List<LogEvent> captureLogEvents(Appender mockAppender, int times) {
        ArgumentCaptor<LogEvent> logEventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(mockAppender, times(times)).append(logEventCaptor.capture());
        return logEventCaptor.getAllValues();
    }

    private Log4jLogEvent createLogTraceEvent(String message) {
        return createLogEvent("Virtual Logger", message, TRACE);
    }

    private Log4jLogEvent createLogDebugEvent(String message) {
        return createLogEvent("Virtual Logger", message, DEBUG);
    }

    private Log4jLogEvent createLogInfoEvent(String message) {
        return createLogEvent("Virtual Logger", message, INFO);
    }

    private Log4jLogEvent createLogWarnEvent(String message) {
        return createLogEvent("Virtual Logger", message, WARN);
    }

    private Log4jLogEvent createLogErrorEvent(String message) {
        return createLogEvent("Virtual Logger", message, ERROR);
    }

    private Log4jLogEvent createLogFatalEvent(String message) {
        return createLogEvent("Virtual Logger", message, FATAL, (Markers) null);
    }

    private Log4jLogEvent createLogInfoEvent(String message, Markers marker) {
        return createLogEvent("Virtual Logger", message, INFO, marker);
    }

    private Log4jLogEvent createLogErrorEvent(String message, Markers marker) {
        return createLogEvent("Virtual Logger", message, ERROR, marker);
    }

    private Log4jLogEvent createLogEvent(String loggerName, String message, Level level) {
        return Log4jLogEvent.newBuilder()
                .setLoggerName(loggerName)
                .setMessage(new SimpleMessage(message))
                .setLevel(level)
                .build();
    }

    private Log4jLogEvent createLogEvent(String loggerName, String message, Level level, Markers marker) {
        return Log4jLogEvent.newBuilder()
                .setLoggerName(loggerName)
                .setMessage(new SimpleMessage(message))
                .setLevel(level)
                .setMarker(marker)
                .build();
    }

    private Log4jLogEvent createLogEvent(String loggerName, Level level, String message, Object... arguments) {
        return Log4jLogEvent.newBuilder()
                .setLoggerName(loggerName)
                .setMessage(new ParameterizedMessage(message, arguments))
                .setLevel(level)
                .build();
    }

    private Log4jLogEvent createLogEvent(String loggerName, Level level, Markers marker, String message, Object... arguments) {
        return Log4jLogEvent.newBuilder()
                .setLoggerName(loggerName)
                .setMessage(new ParameterizedMessage(message, arguments))
                .setLevel(level)
                .setMarker(marker)
                .build();
    }

    private Log4jLogEvent createLogEvent(String loggerName, String message, Level level, Throwable t) {
        return Log4jLogEvent.newBuilder()
                .setLoggerName(loggerName)
                .setMessage(new SimpleMessage(message))
                .setLevel(level)
                .setThrown(t)
                .build();
    }

    private Log4jLogEvent createLogEvent(String loggerName, String message, Level level, Markers marker, Throwable t) {
        return Log4jLogEvent.newBuilder()
                .setLoggerName(loggerName)
                .setMessage(new SimpleMessage(message))
                .setLevel(level)
                .setMarker(marker)
                .setThrown(t)
                .build();
    }

    private AccumulatingAppender getUnit() {
        return getUnit("accuApp");
    }

    private AccumulatingAppender getUnit(String name) {
        return getUnit(name, null, null, null);
    }

    private AccumulatingAppender getUnit(String name, String passThroughLevel) {
        return getUnit(name, passThroughLevel, null, null);
    }

    private AccumulatingAppender getUnit(String name, String passThroughLevel, String passExceptionThrough) {
        return getUnit(name, passThroughLevel, passExceptionThrough, null);
    }

    private AccumulatingAppender getUnit(
            String name, String passThroughLevel, String passExceptionThrough, String ignoreExceptions) {
        AccumulatingAppender unit = AccumulatingAppender.createAppender(
                name, passThroughLevel, ignoreExceptions, passExceptionThrough,
                new AppenderRef[] {AppenderRef.createAppenderRef(mockAppender.getName(), null, null)},
                mockConfiguration, null, null);

        unit.start();
        return unit;
    }

    private Configuration createMockConfiguration(Appender mockAppender) {
        Configuration mockConfiguration = mock(Configuration.class);
        when(mockConfiguration.getAppender("mockAppender")).thenReturn(mockAppender);
        return mockConfiguration;
    }

    private Appender createMockAppender() {
        Appender mockAppender = mock(Appender.class);
        when(mockAppender.getName()).thenReturn("mockAppender");
        when(mockAppender.ignoreExceptions()).thenReturn(true);
        when(mockAppender.isStarted()).thenReturn(true);
        when(mockAppender.isStopped()).thenReturn(false);
        when(mockAppender.getState()).thenReturn(STARTED);
        return mockAppender;
    }

    private static Object[] A(Object... elems) {
        return elems;
    }
}
