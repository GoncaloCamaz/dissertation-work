package pt.uminho.algoritmi.netopt.nfv;

import java.util.HashMap;
import java.util.Map;

public class NFNodesMap
{
    private Map<Integer, NFNode> nodes;

    public NFNodesMap()
    {
        this.nodes = new HashMap<>();
    }

    public NFNodesMap(Map<Integer, NFNode> nodes) {
        this.nodes = nodes;
    }

    public NFNodesMap(NFNodesMap map)
    {
        this.nodes = map.getNodes();
    }

    public Map<Integer, NFNode> getNodes() {
        return nodes;
    }

    public void setNodes(Map<Integer, NFNode> nodes) {
        this.nodes = nodes;
    }
}
