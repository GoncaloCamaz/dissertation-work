package pt.uminho.algoritmi.netopt.nfv;

import java.util.HashMap;
import java.util.Map;

public class NFRequestsMap
{
    private Map<Integer, NFRequest> requestsMap;
    private int nodes;

    public NFRequestsMap()
    {
        this.requestsMap = new HashMap<>();
        this.nodes = 0;
    }

    public NFRequestsMap(Map<Integer, NFRequest> request, int nodes) {
        this.requestsMap = request;
        this.nodes = nodes;
    }

    public NFRequestsMap(int nodes)
    {
        this.nodes = nodes;
        this.requestsMap = new HashMap<>();
    }

    public NFRequestsMap(NFRequestsMap r)
    {
        this.requestsMap = r.getRequestMap();
        this.nodes = r.getNodes();
    }

    public void setRequestList(Map<Integer, NFRequest> requests) {
        this.requestsMap = requests;
    }

    public int getNodes() {
        return nodes;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    public Map<Integer, NFRequest> getRequestMap() {
        return requestsMap;
    }
}
