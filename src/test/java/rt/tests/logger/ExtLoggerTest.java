package rt.tests.logger;

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

public class ExtLoggerTest {

    @Test
    public void testCreateReturnsExtLogger() {
        ExtLogger unit = ExtLogger.create("com.acme.test");

        assertThat(unit, isA(Logger.class));
        assertThat(unit.getName(), equalTo("com.acme.test"));

        unit = ExtLogger.create(this);

        assertThat(unit, isA(Logger.class));
        assertThat(unit.getName(), equalTo(this.getClass().getName()));

        unit = ExtLogger.create(ExtLoggerTest.class);

        assertThat(unit, isA(Logger.class));
        assertThat(unit.getName(), equalTo(ExtLoggerTest.class.getName()));

        unit = ExtLogger.create();

        assertThat(unit, isA(Logger.class));
        assertThat(unit.getName(), equalTo(ExtLogger.class.getName()));
    }
}
