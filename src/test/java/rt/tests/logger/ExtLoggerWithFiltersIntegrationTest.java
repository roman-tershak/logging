package rt.tests.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.junit.LoggerContextRule;
import org.apache.logging.log4j.test.appender.ListAppender;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static rt.tests.accumappender.Markers.FAIL;
import static rt.tests.accumappender.Markers.SUCCESS;

public class ExtLoggerWithFiltersIntegrationTest {

    @ClassRule
    public static LoggerContextRule init = new LoggerContextRule("log4j2-test2.xml");
    private static ListAppender listAppenderForAccuApp;
    private static ListAppender listAppenderForRegular;

    @BeforeClass
    public static void setupLogging() {
        listAppenderForAccuApp = init.getAppender("ListAccuApp");
        listAppenderForRegular = init.getAppender("List");
    }

    @Before
    public void before() {
        listAppenderForAccuApp.clear();
        listAppenderForRegular.clear();
    }

    @Test
    public void testExtLoggerFiltersWithExtendedLogger() {
        ExtLogger logger = ExtLogger.create("com.acme.tests");

        logger.debug("A debug message");
        logger.info(SUCCESS, "An info message");

        assertThat(getAccuAppLog(), containsString("A debug message : An info message"));
        assertThat(getRegularLog(), isEmptyString());
    }

    @Test
    public void testExtLoggerFiltersWithRegularLogger() {
        Logger logger = LogManager.getLogger("com.acme.tests");

        logger.info("Info mesage 1");
        logger.error(FAIL, "A failure");

        String regularLog = getRegularLog();
        assertThat(regularLog, containsString("Info mesage 1"));
        assertThat(regularLog, containsString("A failure"));

        assertThat(getAccuAppLog(), isEmptyString());
    }

    @Test
    public void testExceptionWithExtendedLogger() {
        ExtLogger logger = ExtLogger.create("com.acme.tests");
        try {
            logger.info("Everything seemed to be fine");
            throw new Exception("An exception has been thrown");
        } catch (Exception e) {
            logger.error(FAIL, "...until ", e);
        }

        String accuAppLog = getAccuAppLog();
        assertThat(accuAppLog, containsString("Everything seemed to be fine : ...until "));
        assertThat(accuAppLog, containsString("java.lang.Exception: An exception has been thrown"));
        // If you were to rename the test name, pay your attention to the line below!
        assertThat(accuAppLog, containsString("at " + this.getClass().getName() + ".testExceptionWithExtendedLogger("));

        assertThat(getRegularLog(), isEmptyString());
    }

    @Test
    public void testExceptionWithRegularLogger() {
        Logger logger = LogManager.getLogger("com.acme.tests");
        try {
            logger.info("Everything seemed to be fine");
            throw new Exception("An exception has been thrown");
        } catch (Exception e) {
            logger.error(FAIL, "...until ", e);
        }

        String regularLog = getRegularLog();
        assertThat(regularLog, containsString("Everything seemed to be fine"));
        assertThat(regularLog, containsString("...until "));
        assertThat(regularLog, containsString("java.lang.Exception: An exception has been thrown"));
        // If you were to rename the test name, pay your attention to the line below!
        assertThat(regularLog, containsString("at " + this.getClass().getName() + ".testExceptionWithRegularLogger("));

        assertThat(getAccuAppLog(), isEmptyString());
    }

    private String getAccuAppLog() {
        return listAppenderForAccuApp.getMessages().stream().collect(Collectors.joining());
    }

    private String getRegularLog() {
        return listAppenderForRegular.getMessages().stream().collect(Collectors.joining());
    }
}
