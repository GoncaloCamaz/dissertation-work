package pt.uminho.algoritmi.netopt.nfv.ml.Generator;

import ilog.concert.IloException;
import pt.uminho.algoritmi.netopt.nfv.NFRequest;
import pt.uminho.algoritmi.netopt.nfv.NFRequestsMap;
import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Network
{
    private CurrentNetworkState currentState;
    private NetworkTopology topology;
    private NFVState state;
    private int numberOfEntries;
    private int maxRequestDuration;
    private List<OnlineNFRequest> requests;

    public Network(NetworkTopology topology, NFVState state, int numberOfEntries, int maxRequestDuration) {
        this.topology = topology;
        this.state = state;
        this.numberOfEntries = numberOfEntries;
        this.maxRequestDuration = maxRequestDuration;
        this.requests = getOnlineRequests();
        this.currentState = new CurrentNetworkState(topology.getDimension(), topology.getNumberEdges());
    }

    public void startEvaluation() throws IloException, IOException {

        OptimizationResultObject obj = new OptimizationResultObject(topology.getDimension());
        NFRequest r = new NFRequest();
        CSVFileGenerator.genFileHeaders(this.topology.getDimension(), this.topology.getNumberEdges(),this.state.getServices().getServices().size(), true);

        for(OnlineNFRequest request : requests)
        {
            r = request.getRequest();
            DataSetEntry entry = new DataSetEntry(r.getSource(), r.getDestination(), request.getDuration(), r.getBandwidth());
            entry.setLinksState(this.currentState.getLinks());
            entry.setNodesState(this.currentState.getNodes());
            entry.setRequests(r.getBooleanRequestList(this.state.getServices().getServices().size()));

            currentState.addRequestToQueue(request);
            currentState.setCurrentRequest(request.getRequest().getId());

            obj = this.currentState.evaluateState(topology, state);
            entry.setProcessmentLocation(obj.getServiceProcessmentLocation());
            CSVFileGenerator.addEntrytoFile(entry,this.topology.getDimension(), this.topology.getNumberEdges(),this.state.getServices().getServices().size(), true);

            updateCurrentState(obj);
        }
    }

    private void updateCurrentState(OptimizationResultObject obj)
    {
        this.currentState.setNodes(obj.getNodeUtilization());
        this.currentState.setLinks(obj.getLinksLoad1D());
    }

    private List<OnlineNFRequest> getOnlineRequests()
    {
        NFRequestsMap requestsMap = this.state.getRequests();
        List<OnlineNFRequest> requests = new ArrayList<>();
        Random rand = new Random();

        for(NFRequest request : requestsMap.getRequestMap().values())
        {
            int duration = 10 + rand.nextInt(this.maxRequestDuration);
            OnlineNFRequest onlineNFRequest = new OnlineNFRequest(request,duration, this.state.getServices().getServices().size());
            requests.add(onlineNFRequest);
        }

        return requests;
    }

    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    public void setNumberOfEntries(int numberOfEntries) {
        this.numberOfEntries = numberOfEntries;
    }

    public int getMaxRequestDuration() {
        return maxRequestDuration;
    }

    public void setMaxRequestDuration(int maxRequestDuration) {
        this.maxRequestDuration = maxRequestDuration;
    }

    public NetworkTopology getTopology() {
        return topology;
    }

    public void setTopology(NetworkTopology topology) {
        this.topology = topology;
    }

    public NFVState getState() {
        return state;
    }

    public void setState(NFVState state) {
        this.state = state;
    }

    public List<OnlineNFRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<OnlineNFRequest> requests) {
        this.requests = requests;
    }
}
