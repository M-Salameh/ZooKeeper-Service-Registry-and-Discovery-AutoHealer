import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

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

    public static void main(String[] args) throws InterruptedException, IOException {

         String remoteDirectory = "/JavaJars/";

        String RELATIVE_PATH_TO_JARS = "/out/artifacts/";
        String pathToFile = args.length == 3 ? args[2] : "TransientWorker_jar/Registration&Discovery-AutoHealer.jar";
        pathToFile = System.getProperty("user.dir") + RELATIVE_PATH_TO_JARS + pathToFile;
        File file = new File(pathToFile);
        String remoteUser = "root@192.168.184.10";
         String pathToProgram  = System.getProperty("user.dir") + RELATIVE_PATH_TO_JARS + pathToFile;
         String remoteJarFilePath = remoteDirectory + file.getName();

         String scpCommand = "scp " + pathToProgram + " " + remoteUser + ":" + remoteDirectory;

         String sshCommand = "ssh " + remoteUser +
         " 'java -Dorg.slf4j.simpleLogger.defaultLogLevel=off -jar " +
         remoteJarFilePath + " ' ";

         Process scpProcess = Runtime.getRuntime().exec(scpCommand);
         scpProcess.waitFor();
         if (scpProcess.exitValue() == 0) {
         Process sshProcess = Runtime.getRuntime().exec(sshCommand);
         }
    }
}