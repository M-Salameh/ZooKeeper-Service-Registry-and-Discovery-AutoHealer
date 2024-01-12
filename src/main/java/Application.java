import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
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
    private static final String RELATIVE_PATH_TO_JARS = "/out/artifacts/";

    /**
     * input for jars to execute must be their relative paths
     * inside the out/artifact/ directory
     * */
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {

        int currentServerPort = args.length == 3 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        numberOfInstances = args.length == 3 ? Integer.parseInt(args[1]) : 4;
        pathToFile = args.length == 3 ? args[2] : "TransientWorker_jar/Registration&Discovery-AutoHealer.jar";
        pathToFile = System.getProperty("user.dir") + RELATIVE_PATH_TO_JARS + pathToFile;

        Application application = new Application();
        ZooKeeper zooKeeper = application.connectToZookeeper();

        ServiceRegistry serviceRegistry = new ServiceRegistry(zooKeeper , numberOfInstances , pathToFile);

        OnElectionAction onElectionAction = new OnElectionAction(serviceRegistry, currentServerPort);

        LeaderElection leaderElection = new LeaderElection(zooKeeper, onElectionAction);
        leaderElection.volunteerForLeadership();
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
                    System.out.println("Successfully connected to Zookeeper");
                } else if (watchedEvent.getState() == Event.KeeperState.Disconnected) {
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from Zookeeper");
                        zooKeeper.notifyAll();
                    }
                } else if (watchedEvent.getState() == Event.KeeperState.Closed) {
                    System.out.println("Closed Successfully");
                }
                break;
        }
    }
}
