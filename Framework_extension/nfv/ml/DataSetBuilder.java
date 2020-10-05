package pt.uminho.algoritmi.netopt.nfv.ml;

import ilog.concert.IloException;
import pt.uminho.algoritmi.netopt.cplex.NFV_MCFlowMachineLearning;
import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataSetBuilder
{
    private List<DataSetEntry> entries;

    public DataSetBuilder(int entries, int numberOfNodes, int numberOfEdges, int numberOfServices, boolean binaryOutput)
    {
        this.entries = genEntries(entries, numberOfNodes, numberOfEdges, numberOfServices, binaryOutput);
    }

    private List<DataSetEntry> genEntries(int numberOfEntries, int numberOfNodes, int numberOfEdges, int numberOfServices, boolean binaryOutput)
    {
        List<DataSetEntry> entries = new ArrayList<>();

        while(numberOfEntries > 0)
        {
            DataSetEntry entry = new DataSetEntry(numberOfNodes,numberOfEdges,numberOfServices, binaryOutput);
            entry.setOrigin(returnRandomInt(numberOfNodes));
            entry.setDestination(returnRandomInt(numberOfNodes));

            double bandwidth = 8+returnRandomDouble(7);
            bandwidth = Math.floor(bandwidth*100) / 100;
            entry.setBandwidth(bandwidth);

            entry.setLinksState(returnRandomCapacities(numberOfEdges));
            entry.setNodesState(returnRandomCapacities(numberOfNodes));
            entry.setRequests(returnRandomRequests(numberOfServices));
            entries.add(entry);
            numberOfEntries--;
        }

        return entries;
    }

    public void evaluateEntries(NetworkTopology topology, NFVState state, int cplexTimeLimit, double alpha) throws IloException {

        for(DataSetEntry entry : this.entries)
        {
            NFV_MCFlowMachineLearning solver = new NFV_MCFlowMachineLearning(topology,state,entry,cplexTimeLimit,alpha);
            int[] result = solver.solve();
            entry.setProcessmentLocation(result);
        }
    }

    public List<DataSetEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<DataSetEntry> entries) {
        this.entries = entries;
    }

    public int returnRandomInt(int limit)
    {
        Random random = new Random();
        return random.nextInt(limit);
    }

    public double returnRandomDouble(int limit)
    {
        Random random = new Random();
        int number = random.nextInt(limit);
        double ret = number + random.nextDouble();
        ret = Math.floor(ret*100) / 100;

        return ret;
    }

    public double[] returnRandomCapacities(int arraySize)
    {
        double[] ret = new double[arraySize];
        Random random = new Random();

        for(int i = 0; i < arraySize; i++)
        {
            double retVal = 0 + random.nextDouble();
            retVal = Math.floor(retVal*100) / 100;
            ret[i] = 0 + retVal;
        }

        return ret;
    }

    public int[] returnRandomRequests(int arraySize)
    {
        int[] ret = new int[arraySize];
        int hasService = 0;

        for(int i = 0; i < arraySize; i++)
        {
            int val = returnRandomInt(2);
            ret[i] = val;
            if(val == 1)
            {
                hasService = 1;
            }
        }

        if(hasService == 0)
        {
            ret[0] = 1;
            ret[1] = 1;
            ret[2] = 1;
        }

        return ret;
    }
}
