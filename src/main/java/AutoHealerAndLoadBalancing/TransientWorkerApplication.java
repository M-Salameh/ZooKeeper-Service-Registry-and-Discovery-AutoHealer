package AutoHealerAndLoadBalancing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TransientWorkerApplication
{
    private static final Logger logger = LoggerFactory.getLogger(TransientWorkerApplication.class);
    public static void main(String[] args)
    {
        TransientWorker worker = new TransientWorker();
        String nodeNum = args[0];
        try
        {
            worker.connectToZookeeper();
        }
        catch (IOException e)
        {
            logger.warn("Cannot Connect To ZooKeeper");
            throw new RuntimeException(e);
        }
        try
        {
            worker.work(nodeNum);
        }
        catch (Exception e)
        {
            logger.error("Worker Shut Down");
            System.exit(1);
        }
    }
}
