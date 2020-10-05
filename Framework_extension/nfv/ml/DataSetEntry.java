package pt.uminho.algoritmi.netopt.nfv.ml;

public class DataSetEntry
{
    private int origin;
    private int destination;
    private double bandwidth;
    private int[] requests;
    private double[] linksState;
    private double[] nodesState;
    private int[] processmentLocation;

    public DataSetEntry(int numberOfNodes, int numberOfEdges, int numberOfServices, boolean binaryOutput)
    {
        this.origin = -1;
        this.destination = -1;
        this.bandwidth = -1;
        this.requests = new int[numberOfServices];
        this.linksState = new double[numberOfEdges];
        this.nodesState = new double[numberOfNodes];
        if(binaryOutput)
            this.processmentLocation = new int[numberOfServices*numberOfNodes];
        else
            this.processmentLocation = new int[numberOfServices];

    }

    public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public double getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
    }

    public int[] getRequests() {
        return requests;
    }

    public void setRequests(int[] requests) {
        this.requests = requests;
    }

    public double[] getLinksState() {
        return linksState;
    }

    public void setLinksState(double[] linksState) {
        this.linksState = linksState;
    }

    public double[] getNodesState() {
        return nodesState;
    }

    public void setNodesState(double[] nodesState) {
        this.nodesState = nodesState;
    }

    public int[] getProcessmentLocation() {
        return processmentLocation;
    }

    public void setProcessmentLocation(int[] processmentLocation) {
        this.processmentLocation = processmentLocation;
    }
}
