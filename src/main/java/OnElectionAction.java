import org.apache.zookeeper.KeeperException;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OnElectionAction implements OnElectionCallback
{


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
    public void onWorker()
    {
        try
        {
            String IP = InetAddress.getLocalHost().getHostAddress();
            String username = System.getProperty("user.name");
            serviceRegistry.registerToCluster(username+"@"+IP);
        }
        catch (InterruptedException | KeeperException | UnknownHostException e)
        {
            e.printStackTrace();
        }

    }
}
