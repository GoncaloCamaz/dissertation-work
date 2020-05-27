package pt.uminho.algoritmi.netopt.nfv.optimization;

import java.util.Arrays;
import java.util.HashMap;

public class OptimizationResultObject
{
    private double[][] linkLoads;
    private double[] nodeUtilization;
    private int numberOfNodes;
    private double loadValue;
    private double gammaValue;
    private double phiValue;
    private double mnu;
    private double mlu;
    private HashMap<Integer, Integer> servicesDeployed;
    private boolean allservicesDeployed;
    private NFVRequestsConfigurationMap nfvRequestsConfigurationMap;

    public OptimizationResultObject(int numberOfNodes)
    {
        this.linkLoads = new double[numberOfNodes][numberOfNodes];
        this.nodeUtilization = new double[numberOfNodes];
        this.numberOfNodes = numberOfNodes;
        this.loadValue = 0;
        this.mlu = 0;
        this.mnu = 0;
        this.gammaValue = 0;
        this.phiValue = 0;
        this.servicesDeployed = new HashMap<>();
        this.allservicesDeployed = false;
        this.nfvRequestsConfigurationMap = new NFVRequestsConfigurationMap();
    }

    public OptimizationResultObject(double[][] linkLoads, double[] nodeUtilization, int numberOfNodes, double loadvalue, double phi, double gamma,
                                    HashMap<Integer,Integer> map, boolean allNodesWServices, NFVRequestsConfigurationMap configurationMapmap, double mlu, double mnu) {
        this.linkLoads = linkLoads;
        this.nodeUtilization = nodeUtilization;
        this.numberOfNodes = numberOfNodes;
        this.loadValue = loadvalue;
        this.phiValue = phi;
        this.mlu = mlu;
        this.mnu = mnu;
        this.gammaValue = gamma;
        this.servicesDeployed = map;
        this.allservicesDeployed = allNodesWServices;
        this.nfvRequestsConfigurationMap = configurationMapmap;
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

    public double getMnu() {
        return mnu;
    }

    public void setMnu(double mnu) {
        this.mnu = mnu;
    }

    public double getMlu() {
        return mlu;
    }

    public void setMlu(double mlu) {
        this.mlu = mlu;
    }

    public NFVRequestsConfigurationMap getNfvRequestsConfigurationMap() {
        return nfvRequestsConfigurationMap;
    }

    public void setNfvRequestsConfigurationMap(NFVRequestsConfigurationMap nfvRequestsConfigurationMap) {
        this.nfvRequestsConfigurationMap = nfvRequestsConfigurationMap;
    }

    public int getNumberOfServicesDeployed()
    {
        int val = 0;

        for(int i = 0; i < numberOfNodes; i++)
        {
            val += servicesDeployed.get(i);
        }

        return val;
    }

    public boolean isAllservicesDeployed() {
        return allservicesDeployed;
    }

    public void setAllservicesDeployed(boolean allservicesDeployed) {
        this.allservicesDeployed = allservicesDeployed;
    }

    // if no solution was reached, the values of gamma and phi will remain the same as the object constructor
    public boolean hasSolution()
    {
        boolean ret = true;

        if(this.gammaValue == 0 && this.phiValue == 0 && this.mnu == 0 && this.mlu == 0)
        {
            ret = false;
        }
        return ret;
    }

    public double getLoad(int i, int j)
    {
        return this.linkLoads[i][j];
    }

    @Override
    public String toString() {
        return "OptimizationResultObject{" +
                "linkLoads=" + Arrays.toString(linkLoads) +
                ", nodeUtilization=" + Arrays.toString(nodeUtilization) +
                ", numberOfNodes=" + numberOfNodes +
                ", loadValue=" + loadValue +
                ", gammaValue=" + gammaValue +
                ", phiValue=" + phiValue +
                ", servicesDeployed=" + servicesDeployed +
                ", allServicesDeployed=" + allservicesDeployed +
                '}';
    }
}
