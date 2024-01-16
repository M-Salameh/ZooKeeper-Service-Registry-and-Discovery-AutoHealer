package AutoHealerAndLoadBalancing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class NodeSorting
{
    private static final Logger logger = LoggerFactory.getLogger(NodeSorting.class);
    private static class Pair
    {
        String node;
        int tasksNo;

        // Constructor
        public Pair(String x, int y) {
            this.node = x;
            this.tasksNo = y;
        }
        @Override
        public String toString()
        {
            return "node : " + this.node +" has : " + tasksNo + " workers !!";
        }
    }
    public static List<String> sort(List<byte[]> workers , List<String> Nodes)
    {
        Map<String , Integer> map = new HashMap<>();
        //System.out.println("physical nodes are : " + Nodes);
        logger.info("physical nodes are : " + Nodes);
        for (byte[] worker : workers)
        {
            String s = new String(worker);
            if (!Nodes.contains(s)) continue;
            int x = map.getOrDefault(s , 0);
            map.put(s , x+1);
        }
        for (String node : Nodes)
        {
            int x = map.getOrDefault(node , 0);
            map.put(node , x);
        }
        List<Pair> temp  = new ArrayList<>();
        for (Map.Entry<String , Integer> el : map.entrySet())
        {
            Pair pair = new Pair(el.getKey() , el.getValue());
            temp.add(pair);
        }
        Comparator<Pair> comparator = new Comparator<>()
        {
            @Override
            public int compare(Pair p1, Pair p2)
            {
                return p1.tasksNo - p2.tasksNo;
            }
        };
        List<String> ans = new ArrayList<>();
        Collections.sort(temp, comparator);
        for (Pair pair : temp)
        {
            ans.add(pair.node);
        }
        return ans;
    }
}
