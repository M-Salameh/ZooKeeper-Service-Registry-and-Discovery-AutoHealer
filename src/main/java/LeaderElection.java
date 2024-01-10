import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class LeaderElection implements Watcher {

    private static final String AUTOHEALER_ZNODES_PATH = "/physical_nodes";
    private String currentZnodeName;
    private ZooKeeper zooKeeper;

    private OnElectionCallback onElectionCallback;

    public LeaderElection(ZooKeeper zooKeeper, OnElectionCallback onElectionCallback) {
        this.zooKeeper = zooKeeper;
        this.onElectionCallback = onElectionCallback;

    }

    public void volunteerForLeadership() throws InterruptedException, KeeperException {
        String znodePrefix = AUTOHEALER_ZNODES_PATH + "/node_";
        String znodeFullPath = zooKeeper.create(znodePrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        System.out.println(znodeFullPath);
        this.currentZnodeName = znodeFullPath.replace(AUTOHEALER_ZNODES_PATH + "/", "");
    }

    public void reelectLeader() throws InterruptedException, KeeperException {
        String predecessorName = "";
        Stat predecessorStat = null;

        //this while to guarantee get predecessor even if it deleted just before zookeeper.exist
        while (predecessorStat == null)
        {
            List<String> children = zooKeeper.getChildren(AUTOHEALER_ZNODES_PATH, false);
            Collections.sort(children);

            String smallestChild = children.get(0); //the first element
            if (smallestChild.equals(currentZnodeName)) {
                System.out.println("I'm a leader");
                try
                {
                    String IP = InetAddress.getLocalHost().getHostAddress();
                    String username = System.getProperty("user.name")+"@"+IP;
                    zooKeeper.setData(AUTOHEALER_ZNODES_PATH + "/"+currentZnodeName , username.getBytes() , -1);
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
                onElectionCallback.onElectedToBeLeader();
                return;
            }
            else {
                System.out.println("I'm not a leader");
                int predecessorIndex = children.indexOf(currentZnodeName) - 1;
                predecessorName = children.get(predecessorIndex);
                predecessorStat = zooKeeper.exists(AUTOHEALER_ZNODES_PATH + "/" + predecessorName, this);
            }
        }
        onElectionCallback.onWorker();
        System.out.println("Watching znode " + predecessorName);
        System.out.println();

    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType())
        {
            case NodeDeleted:
                try
                {
                    reelectLeader();
                }
                catch (InterruptedException | KeeperException e) {
                    throw new RuntimeException(e);
                }
                break;

        }
    }
}
