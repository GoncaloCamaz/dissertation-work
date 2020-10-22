package pt.uminho.algoritmi.netopt.nfv.ml.Generator;

import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

import java.util.List;

public class Generator
{
    private static String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";// args[0];
    private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abileneHalfCapacity.edges";//args[1];
    private static String requests =  "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/pedidosAbilene_300.csv";
    private static String servicesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/frameworkConfiguration_Abilene.json";

    public static void main(String[] args) throws Exception {
        int maxRequestDuration = 14;
        int numberOfEntries = 300;

       // String nodesFile = args[0];
       // String edgesFile = args[1];
       // String requests = args[2];
      //  String servicesFile = args[3];
      //  int numberOfEntries = Integer.parseInt(args[4]);
      //  int maxRequestDuration = Integer.parseInt(args[5]);


        NetworkTopology topology = new NetworkTopology(nodesFile,edgesFile);
        NFVState state = new NFVState(servicesFile, requests, true);
        Network network = new Network(topology, state, numberOfEntries, maxRequestDuration);
        List<DataSetEntry> entries = network.startEvaluation();
        CSVFileGenerator csvFileGenerator = new CSVFileGenerator();
        csvFileGenerator.saveToCSV(entries,topology.getDimension(), topology.getNumberEdges(), state.getServices().getServices().size(), true);
    }
}
