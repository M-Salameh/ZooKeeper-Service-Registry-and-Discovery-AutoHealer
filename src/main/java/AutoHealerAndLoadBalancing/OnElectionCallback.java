package AutoHealerAndLoadBalancing;

public interface OnElectionCallback {

    void onElectedToBeLeader();

    void onWorker(String IP);
}
