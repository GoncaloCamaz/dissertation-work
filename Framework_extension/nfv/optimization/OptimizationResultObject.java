package pt.uminho.algoritmi.netopt.nfv.optimization;

public class OptimizationResultObject
{
    private double[][] linkLoads;
    private double[] nodeUtilization;
    private int numberOfNodes;
    private double loadValue;

    public OptimizationResultObject(int numberOfNodes)
    {
        this.linkLoads = new double[numberOfNodes][numberOfNodes];
        this.nodeUtilization = new double[numberOfNodes];
        this.numberOfNodes = numberOfNodes;
        this.loadValue = 0;
    }

    public OptimizationResultObject(double[][] linkLoads, double[] nodeUtilization, int numberOfNodes, double loadvalue) {
        this.linkLoads = linkLoads;
        this.nodeUtilization = nodeUtilization;
        this.numberOfNodes = numberOfNodes;
        this.loadValue = loadvalue;
    }

    public double[][] getLinkLoads() {
        return linkLoads;
    }

    public void setLinkLoads(double[][] linkLoads) {
        this.linkLoads = linkLoads;
    }

    public double[] getNodeUtilization() {
        return nodeUtilization;
    }

    public void setNodeUtilization(double[] nodeUtilization) {
        this.nodeUtilization = nodeUtilization;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public double getLoadValue() {
        return loadValue;
    }

    public void setLoadValue(double loadValue) {
        this.loadValue = loadValue;
    }
}
