package rt.tests.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.junit.LoggerContextRule;
import org.apache.logging.log4j.test.appender.ListAppender;
import org.junit.*;
import rt.tests.accumappender.Markers;

import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static rt.tests.accumappender.Markers.FAIL;
import static rt.tests.accumappender.Markers.SUCCESS;

public class ExtLoggerIntegrationTest {

    @ClassRule
    public static LoggerContextRule init = new LoggerContextRule("log4j2-test1.xml");
    private static ListAppender listAppender;

    @BeforeClass
    public static void setupLogging() {
        listAppender = init.getAppender("List");
    }

    @Before
    public void before() {
        listAppender.clear();
    }

    @Test
    public void testExtLoggerLogsExtendedLogMessages() {
        ExtLogger logger = ExtLogger.create("com.acme.tests");

        logger.debug("A debug message");
        logger.info(SUCCESS, "An info message");

        String log = getLog();
        assertThat(log, containsString("A debug message : An info message"));
        assertThat(log, containsString("com.acme.tests"));
    }

    @Test
    public void testRegularLogMessagesAreLogged() {
        Logger logger = LogManager.getLogger("com.acme.tests");

        logger.info("Info mesage 1");
        logger.error(FAIL, "A failure");

        String log = getLog();
        assertThat(log, containsString("Info mesage 1 : A failure"));
        assertThat(log, containsString("com.acme.tests"));

    }

    @Test
    public void testExceptionIsLoggedIntoTheLog() {
        ExtLogger logger = ExtLogger.create("com.acme.tests");
        try {
            logger.info("Everything seemed to be fine");
            throw new Exception("An exception has been thrown");
        } catch (Exception e) {
            logger.error(FAIL, "...until ", e);
        }

        String log = getLog();
        assertThat(log, containsString("Everything seemed to be fine : ...until "));
        assertThat(log, containsString("java.lang.Exception: An exception has been thrown"));
        assertThat(log, containsString("at " + this.getClass().getName() + ".testExceptionIsLoggedIntoTheLog("));
    }

    private String getLog() {
        return listAppender.getMessages().stream().collect(Collectors.joining());
    }
}
