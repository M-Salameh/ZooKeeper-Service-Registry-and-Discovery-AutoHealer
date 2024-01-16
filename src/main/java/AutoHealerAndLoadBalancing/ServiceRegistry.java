package AutoHealerAndLoadBalancing;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRegistry implements Watcher
{
    private static final String WORKERS_ZNODES_PATH = "/workers";
    private static final String PHYSICAL_ZNODES_PATH = "/physical_nodes";

    private final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);
    private String pathToProgram = "";
    private final ZooKeeper zooKeeper;

    private String currentZnode = null;
    private List<String> allServiceAddresses = null;

    private int numberOfInstances;
    public ServiceRegistry(ZooKeeper zooKeeper , int numberOfInstances , String pathToProgram)
    {
        this.zooKeeper = zooKeeper;
        this.numberOfInstances = numberOfInstances;
        this.pathToProgram = pathToProgram;
        createServiceRegistryZnode();
    }

    private void createServiceRegistryZnode()
    {
        try
        {
            if (zooKeeper.exists(WORKERS_ZNODES_PATH, false) == null) {
                zooKeeper.create(WORKERS_ZNODES_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            if (zooKeeper.exists(PHYSICAL_ZNODES_PATH, false) == null) {
                zooKeeper.create(PHYSICAL_ZNODES_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        }
        catch (KeeperException | InterruptedException e)
        {
            logger.error("Could NOT Create Service Registry Znode");
            e.printStackTrace();
        }
    }

    public void registerToCluster(String metadata) throws KeeperException, InterruptedException {

        if (this.currentZnode != null)
        {
            logger.info("Already registered to service registry");
            return;
        }
        this.currentZnode = zooKeeper.create(PHYSICAL_ZNODES_PATH + "/physical_node_", metadata.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        logger.info("Registered to service registry");
    }

    public void registerForUpdates() {
        try
        {
            masterJob();
        }
        catch (KeeperException | InterruptedException | IOException e)
        {
            logger.error("Could Not Do LEADER JOB !");
            e.printStackTrace();
        }
    }

    public void unregisterFromCluster()
    {
        try
        {
            if (currentZnode != null && zooKeeper.exists(currentZnode, false) != null) {
                zooKeeper.delete(currentZnode, -1);
            }
        }
        catch (KeeperException | InterruptedException e)
        {
            logger.error("Could Not UN-Register From Cluster !!");
            e.printStackTrace();
        }
    }

    private synchronized void masterJob() throws InterruptedException, KeeperException, IOException {
        updateAddresses();
        launchWorkersIfNecessary();
    }

    private void updateAddresses() throws KeeperException, InterruptedException
    {
        List<String> workerZnodes = zooKeeper.getChildren(PHYSICAL_ZNODES_PATH, false);

        List<String> addresses = new ArrayList<>(workerZnodes.size());

        for (String workerZnode : workerZnodes) {
            String workerFullPath = PHYSICAL_ZNODES_PATH + "/" + workerZnode;
            Stat stat = zooKeeper.exists(workerFullPath, false);
            if (stat == null) {
                continue;
            }

            byte[] addressBytes = zooKeeper.getData(workerFullPath, false, stat);
            String address = new String(addressBytes);
            addresses.add(address);
        }

        this.allServiceAddresses = Collections.unmodifiableList(addresses);
        logger.info("The cluster addresses are: " + this.allServiceAddresses);
    }

    private void launchWorkersIfNecessary() throws KeeperException, InterruptedException, IOException
    {
        List<String> physicalZnodes = zooKeeper.getChildren(PHYSICAL_ZNODES_PATH, this);

        List<String> workers = zooKeeper.getChildren(WORKERS_ZNODES_PATH, this);

        for (String worker : workers)
        {
            Stat stat = zooKeeper.exists(WORKERS_ZNODES_PATH + "/" + worker, false);
            if (stat == null) {
                workers.remove(worker);
                continue;
            }

            String node = new String(zooKeeper.getData(WORKERS_ZNODES_PATH + "/" + worker, false, stat));
            if (!physicalZnodes.contains(node))
            {
                workers.remove(worker);
            }
        }

        List<String> sortedWorkers = NodeSorting.sort(getOriginalNodes(workers), physicalZnodes);

        while (workers.size() > numberOfInstances)
        {
            Stat stat = zooKeeper.exists(WORKERS_ZNODES_PATH + "/" + workers.get(0), false);
            if (stat == null) {
                workers.remove(0);
                continue;
            }
            zooKeeper.delete(WORKERS_ZNODES_PATH + "/" + workers.get(0) , -1);
        }

        int neededInstances = numberOfInstances - workers.size();

        if (neededInstances <= 0) return;


        int index = 0;

        int size = sortedWorkers.size();
        while (neededInstances>0
                && size>0)
        {
            Stat stat = zooKeeper.exists(PHYSICAL_ZNODES_PATH + "/" + sortedWorkers.get(index), false);
            if (stat == null)
            {
                sortedWorkers.remove(index);
                size--;
                continue;
            }
            startNewWorker(sortedWorkers.get(index));
            neededInstances--;
            index = (index + 1) % size;
        }

    }

    private List<byte[]> getOriginalNodes(List<String> workers) throws InterruptedException, KeeperException {
        List<byte[]> ans = new ArrayList<>();
        for (String worker : workers)
        {
            Stat stat = zooKeeper.exists(WORKERS_ZNODES_PATH+"/"+worker ,false);
            if (stat == null) continue;
            ans.add(zooKeeper.getData(WORKERS_ZNODES_PATH+"/"+worker,false,stat ));
        }
        return ans;
    }

    private void startNewWorker(String physicalNode) throws IOException, InterruptedException, KeeperException
    {

        String remoteUser = new String(zooKeeper.getData(PHYSICAL_ZNODES_PATH+"/"+physicalNode , false, null));

        String remoteJarFilePath = "/root/AutoHealer/Worker.jar"; //+ file.getName();

        logger.info("Sending To : " + remoteUser);
        String sshCommand = "ssh " + remoteUser + " \"java -jar " + remoteJarFilePath + " " +physicalNode+"\"";
        Runtime.getRuntime().exec(sshCommand);
    }

    @Override
    public void process(WatchedEvent watchedEvent)
    {
        switch (watchedEvent.getType())
        {
            case NodeChildrenChanged:
            {
                try
                {
                    masterJob();
                }
                catch (InterruptedException | KeeperException | IOException e)
                {
                    logger.error("Could NOT Handle Node Children Changed Event!");
                    throw new RuntimeException(e);
                }
            }

        }
    }

}
