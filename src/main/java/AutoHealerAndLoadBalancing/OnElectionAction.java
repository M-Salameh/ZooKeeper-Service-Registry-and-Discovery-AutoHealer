package AutoHealerAndLoadBalancing;

import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnElectionAction implements OnElectionCallback
{

    private final Logger logger = LoggerFactory.getLogger(OnElectionAction.class);
    private final ServiceRegistry serviceRegistry;
    private final int port;

    public OnElectionAction(ServiceRegistry serviceRegistry, int port)
    {
        this.serviceRegistry = serviceRegistry;
        this.port = port;
    }

    @Override
    public void onElectedToBeLeader()
    {
        serviceRegistry.unregisterFromCluster();
        serviceRegistry.registerForUpdates();
    }

    @Override
    public void onWorker(String IP)
    {
        try
        {
            serviceRegistry.registerToCluster(IP);
        }
        catch (InterruptedException | KeeperException e)
        {
            logger.error("Could Not Register To Cluster");
            e.printStackTrace();
        }

    }
}
