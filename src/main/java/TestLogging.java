import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyClass {
    private static final Logger logger = LoggerFactory.getLogger(MyClass.class);

    /*public static void changeLogFile(String newLogFile) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        FileAppender fileAppender = (FileAppender) loggerContext.getLogger("ROOT").getAppender("FILE");
        fileAppender.setFile(newLogFile);
        fileAppender.start();

        loggerContext.getLogger("ROOT").detachAppender("FILE");
        loggerContext.getLogger("ROOT").addAppender(fileAppender);
    }*/

    public static void someMethod() {
        logger.debug("Debug log message");
        logger.info("Info log message");
        logger.error("Error log message");
    }

    public static void main(String[] args) {
        someMethod();
        //changeLogFile("logs/MyClassLogs.logs");
       // someMethod();
    }
}