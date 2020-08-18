package pt.uminho.algoritmi.netopt.nfv.optimization;

import pt.uminho.algoritmi.netopt.cplex.utils.SourceDestinationPair;

import java.lang.reflect.Array;
import java.util.*;

public class NFVRequestConfiguration
{
    private int requestID;
    private int requestOrigin;
    private int requestDestination;
    private double bandwidth;
    private List<SourceDestinationPair> srpath;
    private Map<Integer, Integer> serviceProcessment; //Key -> Service ID; Value -> Node ID
    private List<Integer> serviceOrder;

    public NFVRequestConfiguration()
    {
        this.requestID = -1;
        this.requestOrigin = -1;
        this.requestDestination = -1;
        this.bandwidth = -1;
        this.srpath = new ArrayList<>();
        this.serviceProcessment = new HashMap<>();
        this.serviceOrder = new ArrayList<>();
    }

    public NFVRequestConfiguration(Integer requestID, List<SourceDestinationPair> srpath, Map<Integer, Integer> serviceProcessment, int requestOrigin, int requestDestination,
                                   List<Integer> order)
    {
        this.requestID = requestID;
        this.requestOrigin = requestOrigin;
        this.requestDestination = requestDestination;
        this.srpath = srpath;
        this.serviceProcessment = serviceProcessment;
        this.serviceOrder = order;
    }

    public List<Integer> getServiceOrder() {
        return serviceOrder;
    }

    public void setServiceOrder(List<Integer> serviceOrder) {
        this.serviceOrder = serviceOrder;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public double getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
    }

    public int getRequestOrigin() {
        return requestOrigin;
    }

    public void setRequestOrigin(int requestOrigin) {
        this.requestOrigin = requestOrigin;
    }

    public int getRequestDestination() {
        return requestDestination;
    }

    public void setRequestDestination(int requestDestination) {
        this.requestDestination = requestDestination;
    }

    public Integer getRequestID() {
        return requestID;
    }

    public void setRequestID(Integer requestID) {
        this.requestID = requestID;
    }

    public List<SourceDestinationPair> getSrpath() {
        return srpath;
    }

    public void setSrpath(List<SourceDestinationPair> srpath) {
        this.srpath = srpath;
    }

    public Map<Integer, Integer> getServiceProcessment() {
        return serviceProcessment;
    }

    public void setServiceProcessment(Map<Integer, Integer> serviceProcessment) {
        this.serviceProcessment = serviceProcessment;
    }



    public List<Integer> genSRPath()
    {
        List<Integer> path = new ArrayList<>();
        getFinalSRPath();
        path = getSegmentPathByServiceOrder();

        return path;
    }

    private List<Integer> getSegmentPathByServiceOrder()
    {
        ArrayList<Integer> ret = new ArrayList<>();
        for(Integer i : this.serviceOrder)
        {
            int node = getNodeLocationProcessment(i);
            ret.add(node);
        }

        return ret;
    }

    private void getFinalSRPath()
    {
        int pathSize = srpath.size();
        int i = 0;
        int node = this.requestOrigin;
        List<SourceDestinationPair> orderedSRPath = new ArrayList<>();
        List<SourceDestinationPair> auxList = new ArrayList<>();
        auxList = this.srpath;

        while(i < pathSize)
        {
            for(SourceDestinationPair pair : auxList)
            {
                if(pair.getSource() == node)
                {
                    node = pair.getDestination();
                    orderedSRPath.add(pair);
                    break;
                }
            }
            i++;
        }

        this.srpath = orderedSRPath;
    }

    private int getNodeLocationProcessment(int actualService)
    {
        return this.serviceProcessment.get(actualService);
    }
}
