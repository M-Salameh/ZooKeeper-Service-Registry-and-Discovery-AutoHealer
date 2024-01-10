import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;

public class TransientWorker
{
    private static final String ZOOKEEPER_ADDRESS = "192.168.184.10:2181";
    private static final int SESSION_TIMEOUT = 3000;

    // Parent Znode where each worker stores an ephemeral child to indicate it is alive
    private static final String WORKERS_ZNODES_PATH = "/workers";

    private static final float CHANCE_TO_FAIL = 0.001F;

    private final Random random = new Random();
    private ZooKeeper zooKeeper;

    private String myName = "";

    public void connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, event -> {
        });
    }

    public void work(String nodeNum) throws KeeperException, InterruptedException {
        addChildZnode(nodeNum);
        while (true)
        {
            System.out.println(myName + " is Working...");
            LockSupport.parkNanos(1000L);
            if (random.nextFloat() < CHANCE_TO_FAIL) {
                System.out.println(myName + " encountered Critical error happened");
                throw new RuntimeException(myName + " : Oops");
            }
        }
    }

    private void addChildZnode(String nodeNumber) throws KeeperException, InterruptedException {
        myName= zooKeeper.create(WORKERS_ZNODES_PATH + "/worker_",
                nodeNumber.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
    }
}

