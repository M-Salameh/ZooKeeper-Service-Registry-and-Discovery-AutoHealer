package AutoHealerAndLoadBalancing;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;

public class TransientWorker
{
    private static final String ZOOKEEPER_ADDRESS = "192.168.184.10:2181";
    private static final int SESSION_TIMEOUT = 3000;

    private final Logger logger = LoggerFactory.getLogger(TransientWorker.class);

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
        logger.info(myName + " is Working...");
        Thread.sleep(20000);
        logger.warn(myName + " encountered Critical error happened");
        throw new RuntimeException(myName + " : Oops");
    }

    private void addChildZnode(String nodeNumber) throws KeeperException, InterruptedException {
        myName= zooKeeper.create(WORKERS_ZNODES_PATH + "/worker_",
                nodeNumber.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
    }
}

