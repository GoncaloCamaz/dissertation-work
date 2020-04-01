package pt.uminho.algoritmi.netopt.nfv.optimization;

public class OptimizationResultObject
{
    private double[][] linkLoads;
    private double[] nodeUtilization;
    private int numberOfNodes;
    private double loadValue;
    private double gammaValue;
    private double phiValue;

    public OptimizationResultObject(int numberOfNodes)
    {
        this.linkLoads = new double[numberOfNodes][numberOfNodes];
        this.nodeUtilization = new double[numberOfNodes];
        this.numberOfNodes = numberOfNodes;
        this.loadValue = 0;
        this.gammaValue = 0;
        this.phiValue = 0;
    }

    public OptimizationResultObject(double[][] linkLoads, double[] nodeUtilization, int numberOfNodes, double loadvalue, double phi, double gamma) {
        this.linkLoads = linkLoads;
        this.nodeUtilization = nodeUtilization;
        this.numberOfNodes = numberOfNodes;
        this.loadValue = loadvalue;
        this.phiValue = phi;
        this.gammaValue = gamma;
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

    public double getGammaValue() {
        return gammaValue;
    }

    public void setGammaValue(double gammaValue) {
        this.gammaValue = gammaValue;
    }

    public double getPhiValue() {
        return phiValue;
    }

    public void setPhiValue(double phiValue) {
        this.phiValue = phiValue;
    }
}
