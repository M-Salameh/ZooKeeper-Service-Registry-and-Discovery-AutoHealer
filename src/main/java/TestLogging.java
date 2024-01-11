import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLogging {
    private static final Logger logger = LoggerFactory.getLogger(TestLogging.class);

   /* public static void changeLogFile(String newLogFile) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        FileAppender fileAppender = (FileAppender) loggerContext.getLogger("ROOT").getAppender("FILE");
        fileAppender.setFile(newLogFile);
        fileAppender.start();

        loggerContext.getLogger("ROOT").detachAppender("FILE");
        loggerContext.getLogger("ROOT").addAppender(fileAppender);
    }*/

    public static void tryLogging() {
        logger.debug("Debug log message");
        logger.info("Info log message");
        logger.error("Error log message");
    }

    public static void main(String[] args) {
        //tryLogging();
        //changeLogFile("logs/MyClassLogs.logs");
       // someMethod();
        String workingDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + workingDir);
        /// out put :  D:\HIAST\FIY\FS\Distributed Systems\Lab\6\DS-06\Registration&Discovery-AutoHealer
        /// out/artifacts/TransientWorker_jar/Registration&Discovery-AutoHealer.jar
    }
}