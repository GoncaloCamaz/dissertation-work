package pt.uminho.algoritmi.netopt.nfv;

import java.util.HashMap;
import java.util.Map;

public class NFNodesMap
{
    private Map<Integer, NFNode> nodes;
    private String filename;

    public NFNodesMap()
    {
        this.nodes = new HashMap<>();
        this.filename = "";
    }

    public NFNodesMap(Map<Integer, NFNode> nodes, String filename) {
        this.nodes = nodes;
        this.filename = filename;
    }

    public NFNodesMap(String filename)
    {
        this.filename = filename;
        this.nodes = new HashMap<>();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Map<Integer, NFNode> getNodes() {
        return nodes;
    }

    public void setNodes(Map<Integer, NFNode> nodes) {
        this.nodes = nodes;
    }
}
