package pt.uminho.algoritmi.netopt.nfv.optimization;

import java.util.HashMap;

public class OptimizationResultObject
{
    private double[][] linkLoads;
    private double[] nodeUtilization;
    private int numberOfNodes;
    private double loadValue;
    private double gammaValue;
    private double phiValue;
    private HashMap<Integer, Integer> servicesDeployed;
    private boolean allNodesWServices;

    public OptimizationResultObject(int numberOfNodes)
    {
        this.linkLoads = new double[numberOfNodes][numberOfNodes];
        this.nodeUtilization = new double[numberOfNodes];
        this.numberOfNodes = numberOfNodes;
        this.loadValue = 0;
        this.gammaValue = 0;
        this.phiValue = 0;
        this.servicesDeployed = new HashMap<>();
        this.allNodesWServices = false;
    }

    public OptimizationResultObject(double[][] linkLoads, double[] nodeUtilization, int numberOfNodes, double loadvalue, double phi, double gamma,
                                    HashMap<Integer,Integer> map, boolean allNodesWServices) {
        this.linkLoads = linkLoads;
        this.nodeUtilization = nodeUtilization;
        this.numberOfNodes = numberOfNodes;
        this.loadValue = loadvalue;
        this.phiValue = phi;
        this.gammaValue = gamma;
        this.servicesDeployed = map;
        this.allNodesWServices = allNodesWServices;
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

    public HashMap<Integer, Integer> getServicesDeployed() {
        return servicesDeployed;
    }

    public void setServicesDeployed(HashMap<Integer, Integer> servicesDeployed) {
        this.servicesDeployed = servicesDeployed;
    }

    public int getNumberOfServicesDeployed()
    {
        int val = 0;

        for(Integer i : servicesDeployed.values())
        {
            val += servicesDeployed.get(i);
        }

        return val;
    }

    public boolean isAllNodesWServices() {
        return allNodesWServices;
    }

    public void setAllNodesWServices(boolean allNodesWServices) {
        this.allNodesWServices = allNodesWServices;
    }

    // if no solution was reached, the values of gamma and phi will remain the same as the object constructor
    public boolean hasSolution()
    {
        boolean ret = true;

        if(this.gammaValue == 0 && this.phiValue == 0)
        {
            ret = false;
        }
        return ret;
    }
}
