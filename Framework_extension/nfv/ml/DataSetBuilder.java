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


    public DataSetBuilder()
    {
        this.entries = new ArrayList<>();
    }

    public DataSetBuilder(int entries, int numberOfNodes, int numberOfEdges, int numberOfServices)
    {
        this.entries = genEntries(entries, numberOfNodes, numberOfEdges, numberOfServices);
    }

    private List<DataSetEntry> genEntries(int numberOfEntries, int numberOfNodes, int numberOfEdges, int numberOfServices)
    {
        List<DataSetEntry> entries = new ArrayList<>();

        while(numberOfEntries > 0)
        {
            DataSetEntry entry = new DataSetEntry(numberOfNodes,numberOfEdges,numberOfServices);
            entry.setOrigin(returnRandomInt(numberOfNodes));
            entry.setDestination(returnRandomInt(numberOfNodes));
            entry.setBandwidth(returnRandomDouble(12));
            entry.setLinksState(returnRandomCapacities(numberOfEdges));
            entry.setNodesState(returnRandomCapacities(numberOfNodes));
            entry.setRequests(returnRandomRequests(numberOfServices));
            entries.add(entry);
            numberOfEntries--;
        }

        return entries;
    }

    public void evaluateEntries(NetworkTopology topology, NFVState state, int cplexTimeLimit, double alpha) throws IloException
    {
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
        int number = random.nextInt(limit);

        return number;
    }

    public double returnRandomDouble(int limit)
    {
        Random random = new Random();
        int number = random.nextInt(limit);
        double ret = number + random.nextDouble();
        ret = Math.floor(ret*1000) / 1000;

        return ret;
    }

    public double[] returnRandomCapacities(int arraySize)
    {
        double[] ret = new double[arraySize];
        Random random = new Random();

        for(int i = 0; i < arraySize; i++)
        {
            double retVal = 0 + random.nextDouble();
            retVal = Math.floor(retVal*1000) / 1000;
            ret[i] = 0 + retVal;
        }

        return ret;
    }

    public int[] returnRandomRequests(int arraySize)
    {
        int[] ret = new int[arraySize];

        for(int i = 0; i < arraySize; i++)
        {
            ret[i] = returnRandomInt(2);
        }

        return ret;
    }
}
