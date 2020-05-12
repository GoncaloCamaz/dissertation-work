package pt.uminho.algoritmi.netopt.nfv.optimization;

import pt.uminho.algoritmi.netopt.cplex.utils.SourceDestinationPair;

import java.util.*;

public class NFVRequestConfiguration
{
    private Integer requestID;
    private int requestOrigin;
    private int requestDestination;
    private List<SourceDestinationPair> srpath;
    private Map<Integer, Integer> serviceProcessment; //Key -> Service ID; Value -> Node ID

    public static void main(String[] args)
    {
        List<SourceDestinationPair> pairs = new ArrayList<>();
        pairs.add(new SourceDestinationPair(0,1));
        pairs.add(new SourceDestinationPair(1,5));
        pairs.add(new SourceDestinationPair(7,2));
        pairs.add(new SourceDestinationPair(5,7));

        NFVRequestConfiguration conf = new NFVRequestConfiguration();
        conf.setSrpath(pairs);
        conf.setRequestOrigin(0);
        conf.setRequestDestination(2);
        boolean ret = conf.completePathExists();
        List<Integer> finalList = conf.getFinalSRPath();
        finalList.size();
        System.out.println(ret);
    }

    public NFVRequestConfiguration()
    {
        this.requestID = -1;
        this.requestOrigin = -1;
        this.requestDestination = -1;
        this.srpath = new ArrayList<>();
        this.serviceProcessment = new HashMap<>();
    }

    public NFVRequestConfiguration(Integer requestID, List<SourceDestinationPair> srpath, Map<Integer, Integer> serviceProcessment, int requestOrigin, int requestDestination)
    {
        this.requestID = requestID;
        this.requestOrigin = requestOrigin;
        this.requestDestination = requestDestination;
        this.srpath = srpath;
        this.serviceProcessment = serviceProcessment;
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
        if(completePathExists())
        {
            path = getFinalSRPath();
        }

        return path;
    }

    private List<Integer> getFinalSRPath()
    {
        int pathSize = srpath.size();
        int i = 0;
        int node = this.requestOrigin;
        List<Integer> path = new ArrayList<>();
        List<SourceDestinationPair> orderedSRPath = new ArrayList<>();
        path.add(node);

        while(i < pathSize)
        {
            for(SourceDestinationPair pair : this.srpath)
            {
                if(pair.getSource() == node)
                {
                    node = pair.getDestination();
                    orderedSRPath.add(pair);
                    path.add(node);
                    break;
                }
            }
            i++;
        }
        this.srpath = orderedSRPath;
        return path;
    }

    private boolean completePathExists()
    {
        int pathSize = srpath.size();
        List<SourceDestinationPair> pairList = new ArrayList<>();
        pairList= this.srpath;
        int i = 0;
        int node = this.requestOrigin;
        boolean ret = false;

        while(i < pathSize)
        {
            for(SourceDestinationPair pair : pairList)
            {
                if(pair.getSource() == node)
                {
                    node = pair.getDestination();
                    break;
                }
            }
            i++;
        }
        if(node == this.requestDestination && i == pathSize)
            ret = true;

        return ret;
    }
}
