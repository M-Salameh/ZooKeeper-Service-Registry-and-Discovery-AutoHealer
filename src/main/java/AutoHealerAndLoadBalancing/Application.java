package AutoHealerAndLoadBalancing;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Application implements Watcher {

    private static final String address = "192.168.184.10:2181";
    private static final int SESSION_TIMEOUT = 3000; //dead client
    private static final int DEFAULT_PORT = 8080;
    private ZooKeeper zooKeeper;

    public static int numberOfInstances;
    public static String pathToFile = "";
    private final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {

        int currentServerPort = args.length == 3 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        numberOfInstances = args.length == 3 ? Integer.parseInt(args[1]) : 4;
        String IP = args.length == 3 ? args[2] : "M_Salameh@127.0.0.1";

        pathToFile = System.getProperty("user.dir") + "Worker.jar";

        Application application = new Application();
        ZooKeeper zooKeeper = application.connectToZookeeper();

        ServiceRegistry serviceRegistry = new ServiceRegistry(zooKeeper , numberOfInstances , pathToFile);

        OnElectionAction onElectionAction = new OnElectionAction(serviceRegistry, currentServerPort);

        LeaderElection leaderElection = new LeaderElection(zooKeeper, onElectionAction);
        leaderElection.volunteerForLeadership(IP);
        leaderElection.reelectLeader();

        application.run();
        application.close();

    }

    public ZooKeeper connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(address, SESSION_TIMEOUT, this);
        return zooKeeper;
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    private void close() throws InterruptedException {
        this.zooKeeper.close();
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    logger.info("Successfully connected to Zookeeper");
                } else if (watchedEvent.getState() == Event.KeeperState.Disconnected) {
                    synchronized (zooKeeper) {
                       logger.warn("Disconnected from Zookeeper");
                        zooKeeper.notifyAll();
                    }
                } else if (watchedEvent.getState() == Event.KeeperState.Closed) {
                    logger.info("Closed Successfully");
                }
                break;
        }
    }
}
