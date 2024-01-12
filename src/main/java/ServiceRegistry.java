import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRegistry implements Watcher
{
    private static final String WORKERS_ZNODES_PATH = "/workers";
    private static final String PHYSICAL_ZNODES_PATH = "/physical_nodes";

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
        catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void registerToCluster(String metadata) throws KeeperException, InterruptedException {

        if (this.currentZnode != null) {
            System.out.println("Already registered to service registry");
            return;
        }
        this.currentZnode = zooKeeper.create(PHYSICAL_ZNODES_PATH + "/physical_node_", metadata.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Registered to service registry");
    }

    public void registerForUpdates() {
        try
        {
            masterJob();
        }
        catch (KeeperException | InterruptedException | IOException e) {
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
        System.out.println("The cluster addresses are: " + this.allServiceAddresses);
    }

    private void launchWorkersIfNecessary() throws KeeperException, InterruptedException, IOException
    {
        List<String> physicalZnodes = zooKeeper.getChildren(PHYSICAL_ZNODES_PATH, this);

        List<String> workers = zooKeeper.getChildren(WORKERS_ZNODES_PATH, this);

        //System.out.println("Total workers (before editing) = " + workers.size());

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
                //System.out.println("Physical Node is shut down !!");
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

        System.out.println("Needed Instances is : " + neededInstances);

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
           // String jj = new String(ans.get(ans.size()-1));
           // System.out.println("worker : " +worker + " belongs to : " + jj);
        }
        return ans;
    }

    private void startNewWorker(String physicalNode) throws IOException, InterruptedException, KeeperException
    {
        File file = new File(pathToProgram);

        String remoteUser = new String(zooKeeper.getData(PHYSICAL_ZNODES_PATH+"/"+physicalNode , false, null));

        /**String remoteDirectory = "/JavaJars/";

        String remoteJarFilePath = remoteDirectory + file.getName();

        String scpCommand = "scp " + pathToProgram + " " + remoteUser + ":" + remoteDirectory;

        String sshCommand = "ssh " + remoteUser +
                " 'java -Dorg.slf4j.simpleLogger.defaultLogLevel=off -jar " +
                remoteJarFilePath + " ' ";


        Process scpProcess = Runtime.getRuntime().exec(scpCommand);
        scpProcess.waitFor();
        if (scpProcess.exitValue() == 0) {
            Process sshProcess = Runtime.getRuntime().exec(sshCommand);
        }*/
         String command = "java -Dorg.slf4j.simpleLogger.defaultLogLevel=off -jar " + file.getName() + " " + physicalNode;
         System.out.println("Sending job to " + remoteUser + " of node : " + physicalNode);
         Runtime.getRuntime().exec(command, null, file.getParentFile());

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
                catch (InterruptedException | KeeperException | IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }

}
