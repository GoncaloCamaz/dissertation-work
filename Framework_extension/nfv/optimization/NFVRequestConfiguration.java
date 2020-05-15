package pt.uminho.algoritmi.netopt.nfv.optimization;

import pt.uminho.algoritmi.netopt.cplex.utils.SourceDestinationPair;

import java.util.*;

public class NFVRequestConfiguration
{
    private int requestID;
    private int requestOrigin;
    private int requestDestination;
    private int bandwidth;
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

    public int getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(int bandwidth) {
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
        if(!returnArcs())
        {
            path = getFinalSRPath();
        }
        else
        {
            path = this.serviceOrder;
            if(!path.contains(requestDestination))
            {
                path.add(requestDestination);
                if(path.get(0) != requestOrigin) {
                    List<Integer> pathAux = new ArrayList<>();
                    pathAux.add(requestOrigin);
                    for(Integer i : path)
                    {
                        pathAux.add(i);
                    }
                    path = pathAux;
                }
            }
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

        if(path.get(0) != requestOrigin)

        this.srpath = orderedSRPath;
        return path;
    }


    private int getNodeLocationProcessment(int actualService)
    {
        return this.serviceProcessment.get(actualService);
    }

    private boolean returnArcs()
    {
        List<SourceDestinationPair> aux = new ArrayList<>();
        for(SourceDestinationPair pair : this.srpath)
        {
            SourceDestinationPair pairInverted = new SourceDestinationPair(pair.getDestination(), pair.getSource());
            if(this.srpath.contains(pairInverted))
                aux.add(pairInverted);
        }

        return (aux.size() != 0 ? true : false);
    }
/*
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

      private List<Integer> genServiceProcessmentLocationPathBased()
    {
        List<Integer> path = new ArrayList<>();
        path.add(requestOrigin);
        for(Integer i : this.serviceProcessment.values())
        {
            if(!path.contains(i))
                path.add(i);
        }
        if(!path.contains(requestDestination))
            path.add(requestDestination);

        return path;
    }

 */
}
