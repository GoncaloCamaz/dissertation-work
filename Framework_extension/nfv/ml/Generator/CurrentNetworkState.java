package pt.uminho.algoritmi.netopt.nfv.ml.Generator;

import ilog.concert.IloException;
import pt.uminho.algoritmi.netopt.cplex.NFV_DataSetMILPSolver;
import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CurrentNetworkState
{
    private List<OnlineNFRequest> requestsQueue;
    private double[] links;
    private double[] nodes;
    private int currentRequest;

    public CurrentNetworkState(int currentRequestID, int numberOfNodes, int numberOfEdges)
    {
        this.requestsQueue = new ArrayList<>();
        this.links = new double[numberOfEdges];
        this.nodes = new double[numberOfNodes];
        this.currentRequest = currentRequestID;

        for(int i = 0; i < numberOfEdges; i++)
        {
            links[i] = 0;
        }

        for(int i = 0; i < numberOfNodes; i++)
        {
            nodes[i] = 0;
        }
    }

    public CurrentNetworkState(int numberOfNodes, int numberOfEdges)
    {
        this.requestsQueue = new ArrayList<>();
        this.links = new double[numberOfEdges];
        this.nodes = new double[numberOfNodes];
        this.currentRequest = -1;

        for(int i = 0; i < numberOfEdges; i++)
        {
            links[i] = 0;
        }

        for(int i = 0; i < numberOfNodes; i++)
        {
            nodes[i] = 0;
        }
    }



    public OptimizationResultObject evaluateState(NetworkTopology topology, NFVState state) throws IloException {
        OptimizationResultObject ob = new OptimizationResultObject(topology.getDimension());

        NFV_DataSetMILPSolver solver = new NFV_DataSetMILPSolver(topology, state, this.requestsQueue,this.currentRequest,35, 0.5); // remove DataSetEntry

        ob = solver.solve();
        for(OnlineNFRequest r : this.requestsQueue)
        {
            if(r.getRequest().getId() == this.currentRequest)
            {
                r.setProcessmentLocation(ob.getServiceProcessmentLocation());
            }
        }
        decrementDuration();

        return ob;
    }

    private void decrementDuration()
    {
        for(OnlineNFRequest request : this.requestsQueue)
        {
            request.decrementDuration();
        }

        this.setRequestsQueue(this.requestsQueue.stream().filter(r -> r.getDuration() > 0).collect(Collectors.toList()));
    }

    public List<OnlineNFRequest> getRequestinQueue() {
        return requestsQueue;
    }

    public void setRequestinQueue(List<OnlineNFRequest> requestinQueue) {
        this.requestsQueue = requestinQueue;
    }

    public int addRequestToQueue(OnlineNFRequest request)
    {
        this.requestsQueue.add(request);

        return this.requestsQueue.size();
    }

    public List<OnlineNFRequest> getRequestsQueue() {
        return requestsQueue;
    }

    public void setRequestsQueue(List<OnlineNFRequest> requestsQueue) {
        this.requestsQueue = requestsQueue;
    }

    public int getCurrentRequest() {
        return currentRequest;
    }

    public void setCurrentRequest(int currentRequest) {
        this.currentRequest = currentRequest;
    }

    public double[] getLinks() {
        return links;
    }

    public void setLinks(double[] links) {
        this.links = links;
    }

    public double[] getNodes() {
        return nodes;
    }

    public void setNodes(double[] nodes) {
        this.nodes = nodes;
    }
}
