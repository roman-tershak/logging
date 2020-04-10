package rt.tests.accumappender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rt.tests.accumappender.danger.DangerZone;
import rt.tests.logger.ExtLogger;

import static rt.tests.accumappender.Markers.SUCCESS;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static final Logger EXT_LOGGER = ExtLogger.create(Main.class);

    public static void main(String[] args) {

        LOGGER.debug("A debug message");
        EXT_LOGGER.debug("And extended debug");

        DangerZone.test();

        LOGGER.info("A standard info message");
        EXT_LOGGER.info("An extended logger info message");

        EXT_LOGGER.info(SUCCESS, "Success");
    }

}
