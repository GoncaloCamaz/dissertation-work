package pt.uminho.algoritmi.netopt.nfv;

import java.util.HashMap;
import java.util.Map;

public class NFRequestsMap
{
    private Map<Integer, NFRequest> requestsMap;

    public NFRequestsMap()
    {
        this.requestsMap = new HashMap<>();
    }

    public NFRequestsMap(Map<Integer, NFRequest> request) {
        this.requestsMap = request;
    }

    public NFRequestsMap(int nodes)
    {
        this.requestsMap = new HashMap<>();
    }

    public NFRequestsMap(NFRequestsMap r)
    {
        this.requestsMap = r.getRequestMap();
    }

    public void setRequestList(Map<Integer, NFRequest> requests) {
        this.requestsMap = requests;
    }

    public Map<Integer, NFRequest> getRequestMap() {
        return requestsMap;
    }
}
