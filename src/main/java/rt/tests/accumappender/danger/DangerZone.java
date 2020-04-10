package rt.tests.accumappender.danger;

import rt.tests.logger.CustomLogger;
import rt.tests.logger.ExtLogger;

public class DangerZone {

    private static final ExtLogger EXT_LOGGER = ExtLogger.create(DangerZone.class);
    private static final CustomLogger CUSTOM_LOGGER = CustomLogger.create(DangerZone.class);

    public static void test() {
        EXT_LOGGER.debug("A message from danger zone");
        CUSTOM_LOGGER.debug("Using CustomLogger in danger zone");
    }
}
