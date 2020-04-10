package rt.tests.accumappender;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.junit.Test;
import rt.tests.logger.ExtLogger;

import static org.apache.logging.log4j.Level.*;
import static org.apache.logging.log4j.core.Filter.Result.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static rt.tests.accumappender.Markers.FAIL;
import static rt.tests.accumappender.Markers.SUCCESS;

public class NoExtLoggerFilterTest {

    @Test
    public void testRegularLoggerEventsArePassedThrough() {
        NoExtLoggerFilter unit = getUnit();

        assertThat(unit.filter(createLogEvent(TRACE, FAIL, "Message {}")), equalTo(NEUTRAL));
        assertThat(unit.filter(createLogEvent(DEBUG, null, "Message {}")), equalTo(NEUTRAL));
        assertThat(unit.filter(createLogEvent(INFO, null, "Message {}")), equalTo(NEUTRAL));
        assertThat(unit.filter(createLogEvent(WARN, SUCCESS, "Message {}", "warning")), equalTo(NEUTRAL));
        assertThat(unit.filter(createLogEvent(ERROR, null, "Message {}", "Failure")), equalTo(NEUTRAL));
        assertThat(unit.filter(createLogEvent(FATAL, FAIL, "Message {}")), equalTo(NEUTRAL));
    }

    @Test
    public void testExtLoggerEventsAreFilteredOut() {
        NoExtLoggerFilter unit = getUnit();

        assertThat(unit.filter(createExtLogEvent(TRACE, FAIL, "Message {}")), equalTo(DENY));
        assertThat(unit.filter(createExtLogEvent(DEBUG, null, "Message {}")), equalTo(DENY));
        assertThat(unit.filter(createExtLogEvent(INFO, null, "Message {}")), equalTo(DENY));
        assertThat(unit.filter(createExtLogEvent(WARN, SUCCESS, "Message {}", "warning")), equalTo(DENY));
        assertThat(unit.filter(createExtLogEvent(ERROR, null, "Message {}", "Failure")), equalTo(DENY));
        assertThat(unit.filter(createExtLogEvent(FATAL, FAIL, "Message {}")), equalTo(DENY));
    }

    @Test
    public void testExtLoggerEventsArePassedThroughIfOnMismatchIsAccept() {
        NoExtLoggerFilter unit = getUnit(DENY, ACCEPT);

        assertThat(unit.filter(createExtLogEvent(TRACE, FAIL, "Message {}")), equalTo(ACCEPT));
        assertThat(unit.filter(createExtLogEvent(DEBUG, null, "Message {}")), equalTo(ACCEPT));
        assertThat(unit.filter(createExtLogEvent(INFO, null, "Message {}")), equalTo(ACCEPT));
        assertThat(unit.filter(createExtLogEvent(WARN, SUCCESS, "Message {}", "warning")), equalTo(ACCEPT));
        assertThat(unit.filter(createExtLogEvent(ERROR, null, "Message {}", "Failure")), equalTo(ACCEPT));
        assertThat(unit.filter(createExtLogEvent(FATAL, FAIL, "Message {}")), equalTo(ACCEPT));
    }

    @Test
    public void testRegularEventsAreFilteredOutIfOnMatchIsDeny() {
        NoExtLoggerFilter unit = getUnit(DENY, ACCEPT);

        assertThat(unit.filter(createLogEvent(TRACE, FAIL, "Message {}")), equalTo(DENY));
        assertThat(unit.filter(createLogEvent(DEBUG, null, "Message {}")), equalTo(DENY));
        assertThat(unit.filter(createLogEvent(INFO, null, "Message {}")), equalTo(DENY));
        assertThat(unit.filter(createLogEvent(WARN, SUCCESS, "Message {}", "warning")), equalTo(DENY));
        assertThat(unit.filter(createLogEvent(ERROR, null, "Message {}", "Failure")), equalTo(DENY));
        assertThat(unit.filter(createLogEvent(FATAL, FAIL, "Message {}")), equalTo(DENY));
    }

    private LogEvent createExtLogEvent(Level level, Markers marker, String message, Object... arguments) {
        return Log4jLogEvent.newBuilder()
                .setLoggerName("Logger")
                .setLoggerFqcn(ExtLogger.FQCN)
                .setMessage(new ParameterizedMessage(message, arguments))
                .setLevel(level)
                .setMarker(marker)
                .build();
    }

    private LogEvent createLogEvent(Level level, Markers marker, String message, Object... arguments) {
        return Log4jLogEvent.newBuilder()
                .setLoggerName("Logger")
                .setMessage(new ParameterizedMessage(message, arguments))
                .setLevel(level)
                .setMarker(marker)
                .build();
    }

    private NoExtLoggerFilter getUnit() {
        return getUnit(NEUTRAL, DENY);
    }

    private NoExtLoggerFilter getUnit(Result onMatch, Result onMismatch) {
        NoExtLoggerFilter filter = NoExtLoggerFilter.newNoExtLoggerFilterBuilder()
                .setOnMatch(onMatch)
                .setOnMismatch(onMismatch)
                .build();
        filter.start();
        return filter;
    }
}
