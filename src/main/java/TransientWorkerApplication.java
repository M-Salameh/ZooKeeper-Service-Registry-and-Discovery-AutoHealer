import java.io.IOException;

public class TransientWorkerApplication
{
    public static void main(String[] args)
    {
        TransientWorker worker = new TransientWorker();
        String nodeNum = args[0];
        try
        {
            worker.connectToZookeeper();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try
        {
            worker.work(nodeNum);
        }
        catch (Exception e) {
            System.exit(1);
        }
    }
}
