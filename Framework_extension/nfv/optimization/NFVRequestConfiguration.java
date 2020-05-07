package pt.uminho.algoritmi.netopt.nfv.optimization;

import pt.uminho.algoritmi.netopt.cplex.utils.SourceDestinationPair;

import java.util.*;

public class NFVRequestConfiguration
{
    private Integer requestID;
    private List<SourceDestinationPair> srpath;
    private Map<Integer, Integer> serviceProcessment; //Key -> Service ID; Value -> Node ID
    private List<Integer> serviceOrder;
    private List<Integer> finalSRPath;

    public NFVRequestConfiguration()
    {
        this.requestID = -1;
        this.srpath = new ArrayList<>();
        this.serviceProcessment = new HashMap<>();
        this.serviceOrder = new ArrayList<>();
        this.finalSRPath = new ArrayList<>();
        this.finalSRPath = genSRPath();
    }

    public NFVRequestConfiguration(Integer requestID, List<SourceDestinationPair> srpath, Map<Integer, Integer> serviceProcessment, List<Integer> order)
    {
        this.requestID = requestID;
        this.srpath = srpath;
        this.serviceProcessment = serviceProcessment;
        this.serviceOrder = order;
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

    public List<Integer> getServiceOrder() {
        return serviceOrder;
    }

    public void setServiceOrder(List<Integer> serviceOrder) {
        this.serviceOrder = serviceOrder;
    }

    public List<Integer> genSRPath()
    {
        ArrayList<Integer> path = new ArrayList<>();
        for(Integer i : serviceOrder)
        {
            int nodeID = serviceProcessment.get(i);
            path.add(nodeID);
        }

        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NFVRequestConfiguration)) return false;
        NFVRequestConfiguration that = (NFVRequestConfiguration) o;
        return Objects.equals(getRequestID(), that.getRequestID()) &&
                Objects.equals(getSrpath(), that.getSrpath()) &&
                Objects.equals(getServiceProcessment(), that.getServiceProcessment());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRequestID(), getSrpath(), getServiceProcessment());
    }
}
