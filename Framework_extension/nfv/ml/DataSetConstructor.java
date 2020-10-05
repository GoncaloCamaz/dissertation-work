package pt.uminho.algoritmi.netopt.nfv.ml;


import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

import java.util.List;

public class DataSetConstructor
{
    private static String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";// args[0];
    private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.edges";//args[1];
    private static String servicesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/frameworkConfiguration_Abilene.json";

    public static void main(String[] args) throws Exception {

        NetworkTopology topology = new NetworkTopology(nodesFile,edgesFile);
        NFVState state = new NFVState(servicesFile);
        int numberOfEntries = 1000;
        int cplexTimeLimit = 20;
        double alpha = 0.5;

        DataSetBuilder builder = new DataSetBuilder(numberOfEntries, topology.getDimension(), topology.getNumberEdges(),
        state.getServices().getServices().size(), true);

        builder.evaluateEntries(topology,state,cplexTimeLimit,alpha);

        List<DataSetEntry> entries = builder.getEntries();

        CSVFileGenerator.saveToCSV(entries,topology.getDimension(), topology.getNumberEdges(), state.getServices().getServices().size(), true);
    }
}
