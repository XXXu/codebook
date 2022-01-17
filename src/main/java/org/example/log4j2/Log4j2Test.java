package org.example.log4j2;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4j2Test {
    private static final Logger logger = LogManager.getLogger(Log4j2Test.class);

    public static void main(String[] args) {
        logger.trace("Entering application.");
        logger.info("asdasdasd");
        Bar bar = new Bar();
        if (!bar.doIt()) {
            logger.error("Didn't do it.");
        }
        logger.trace("Exiting application.");
    }
}
