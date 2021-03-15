package pt.uminho.algoritmi.netopt.nfv.ml.Generator;

import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

public class Generator
{

    private static String nodesFile ="C:/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";// args[0];
    private static String edgesFile = "C:/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abileneMachineLearning.edges";//args[1];
    private static String servicesFile = "C:/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/frameworkConfiguration_Abilene_WorstCase.json";
    private static String requests = "C:/Users/gcama/Desktop/Dissertacao/Work/Generator/pedidos.csv";//args[3];

    public static void main(String[] args) throws Exception {

       // String nodesFile = args[0];
        //String edgesFile = args[1];
       // String requests = args[2];
       // String servicesFile = args[3];
        int maxRequestDuration = 24;

        NetworkTopology topology = new NetworkTopology(nodesFile,edgesFile);
        NFVState state = new NFVState(servicesFile, requests, true);
        Network network = new Network(topology, state, maxRequestDuration);
        network.startEvaluation();
    }
}
