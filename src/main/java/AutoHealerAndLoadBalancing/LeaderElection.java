package AutoHealerAndLoadBalancing;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class LeaderElection implements Watcher {

    private static final String ELECTION_NAMESPACE = "/election";
    private String currentZnodeName;
    private ZooKeeper zooKeeper;
    private final Logger logger = LoggerFactory.getLogger(LeaderElection.class);
    private String IP = "";
    private OnElectionCallback onElectionCallback;

    public LeaderElection(ZooKeeper zooKeeper, OnElectionCallback onElectionCallback)
    {
        this.zooKeeper = zooKeeper;
        this.onElectionCallback = onElectionCallback;

    }

    public void volunteerForLeadership(String IP) throws InterruptedException, KeeperException, UnknownHostException {

        String znodePrefix = ELECTION_NAMESPACE + "/c_";
        this.IP = IP;
        String znodeFullPath = zooKeeper.create(znodePrefix, IP.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        //System.out.println(znodeFullPath);
        logger.info("My Node under Election is" + znodeFullPath);
        this.currentZnodeName = znodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
    }

    public void reelectLeader() throws InterruptedException, KeeperException {
        String predecessorName = "";
        Stat predecessorStat = null;

        //this while to guarantee get predecessor even if it deleted just before zookeeper.exist
        while (predecessorStat == null)
        {
            List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
            Collections.sort(children);

            String smallestChild = children.get(0); //the first element
            if (smallestChild.equals(currentZnodeName))
            {
                logger.info("I am LEADER");
                onElectionCallback.onElectedToBeLeader();
                return;
            }
            else
            {
                logger.info("I am NOT LEADER");
                int predecessorIndex = children.indexOf(currentZnodeName) - 1;
                predecessorName = children.get(predecessorIndex);
                predecessorStat = zooKeeper.exists(ELECTION_NAMESPACE + "/" + predecessorName, this);
            }
        }
        onElectionCallback.onWorker(IP);
        logger.info("Watching znode " + predecessorName + " in ELECTION");
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
                catch (InterruptedException | KeeperException e)
                {
                    logger.error("Can Not Handle Node Deletion in Election");
                    throw new RuntimeException(e);
                }
                break;

        }
    }
}
