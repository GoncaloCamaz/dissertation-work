package pt.uminho.algoritmi.netopt.nfv;

import java.util.HashMap;
import java.util.Map;

public class NFRequestsMap
{
    private Map<Integer, NFRequest> requestsMap;
    private int nodes;
    private String filename;

    public NFRequestsMap()
    {
        this.requestsMap = new HashMap<>();
        this.nodes = 0;
        this.filename = "";
    }

    public NFRequestsMap(Map<Integer, NFRequest> request, int nodes, String filename) {
        this.requestsMap = request;
        this.nodes = nodes;
        this.filename = filename;
    }

    public NFRequestsMap(int nodes, String filename)
    {
        this.nodes = nodes;
        this.filename = filename;
        this.requestsMap = new HashMap<>();
    }

    public NFRequestsMap(NFRequestsMap r)
    {
        this.requestsMap = r.getRequestMap();
        this.nodes = r.getNodes();
        this.filename = r.getFilename();
    }

    public NFRequestsMap(String filename)
    {
        this.requestsMap = new HashMap<>();
        this.nodes = 0;
        this.filename = filename;
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

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Map<Integer, NFRequest> getRequestMap() {
        return requestsMap;
    }

    public String getFilename() {
        return filename;
    }
}
