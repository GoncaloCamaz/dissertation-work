package pt.uminho.algoritmi.netopt.nfv.optimization.Utils;

public class ResultAnalystParserObject
{
    private double linkUtil;
    private double nodeUtil;
    private int numberOfServices;
    private double objectiveFunction;

    public ResultAnalystParserObject(double linkUtil, double nodeUtil, int numberOfServices, double objectiveFunction) {
        this.linkUtil = linkUtil;
        this.nodeUtil = nodeUtil;
        this.numberOfServices = numberOfServices;
        this.objectiveFunction = objectiveFunction;
    }

    public double getLinkUtil() {
        return linkUtil;
    }

    public void setLinkUtil(double linkUtil) {
        this.linkUtil = linkUtil;
    }

    public double getNodeUtil() {
        return nodeUtil;
    }

    public void setNodeUtil(double nodeUtil) {
        this.nodeUtil = nodeUtil;
    }

    public int getNumberOfServices() {
        return numberOfServices;
    }

    public void setNumberOfServices(int numberOfServices) {
        this.numberOfServices = numberOfServices;
    }

    public double getObjectiveFunction() {
        return objectiveFunction;
    }

    public void setObjectiveFunction(double objectiveFunction) {
        this.objectiveFunction = objectiveFunction;
    }
}
