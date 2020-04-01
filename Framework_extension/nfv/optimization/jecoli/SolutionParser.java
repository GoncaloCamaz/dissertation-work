package pt.uminho.algoritmi.netopt.nfv.optimization.jecoli;

import pt.uminho.algoritmi.netopt.nfv.NFNode;
import pt.uminho.algoritmi.netopt.nfv.NFNodesMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Number of services = 3
0 -> no service
1 -> Service ID 1
2 -> Service ID 2
3 -> Service ID 3
4 -> Service ID 1,2
5 -> Service ID 1,3
6 -> Service ID 2,3
7 -> Service ID 1,2,3
 **/

public class SolutionParser
{
    private Map<Integer, List<Integer>> serv;

    public SolutionParser()
    {
        this.serv = new HashMap<>();
        List<Integer> noServ = new ArrayList<>();
        serv.put(0, noServ);
        List<Integer> serv1 = new ArrayList<>(); serv1.add(1);
        serv.put(1, serv1);
        List<Integer> serv2 = new ArrayList<>(); serv2.add(2);
        serv.put(2, serv2);
        List<Integer> serv3 = new ArrayList<>(); serv3.add(3);
        serv.put(3, serv3);
        List<Integer> serv4 = new ArrayList<>(); serv4.add(0,1); serv4.add(1,2);
        serv.put(4, serv4);
        List<Integer> serv5 = new ArrayList<>(); serv5.add(0,1); serv5.add(1,3);
        serv.put(5, serv5);
        List<Integer> serv6 = new ArrayList<>(); serv6.add(0,2); serv6.add(1,3);
        serv.put(6, serv6);
        List<Integer> serv7 = new ArrayList<>(); serv7.add(0,1); serv7.add(1,2); serv7.add(2,3);
        serv.put(7, serv7);
    }

    public NFNodesMap solutionParser(int[] results)
    {
        NFNodesMap nodesMap = new NFNodesMap();
        Map<Integer, NFNode> nodes = new HashMap<>();

        for(int i = 0; i < results.length; i++)
        {
            List<Integer> availableSerices = serv.get(i);
            NFNode node = new NFNode(i,1000, availableSerices);
            nodes.put(i, node);
        }
        nodesMap.setNodes(nodes);

        return nodesMap;
    }

    public int[] solutionFromConfiguration(NFNodesMap map)
    {
        Map<Integer, NFNode> nodes = map.getNodes();
        int[] res = new int[nodes.size()];
        for(int i = 0; i < nodes.size(); i++)
        {
            List<Integer> avail = nodes.get(i).getAvailableServices();
            int index = getListServID(avail);
            res[i] = index;
        }

        return res;
    }

    private int getListServID(List<Integer> avail)
    {
        int ret = 0; int i = 0;

        for(List<Integer> list : serv.values())
        {
            if(list.equals(avail))
            {
                ret = i;
            }
            i++;
        }
        return ret;
    }
}
