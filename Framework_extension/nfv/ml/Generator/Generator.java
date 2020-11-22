package pt.uminho.algoritmi.netopt.nfv.ml.Generator;

import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

import java.util.List;

public class Generator
{

    private static String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";// args[0];
    private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abileneMachineLearning.edges";//args[1];
    private static String servicesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/frameworkConfiguration_Abilene_WorstCase.json";
    private static String requests = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/pedidosAbilene_1200.csv";//args[3];

    public static void main(String[] args) throws Exception {

        //String nodesFile = args[0];
        //String edgesFile = args[1];
        //String requests = args[2];
        //String servicesFile = args[3];
        int numberOfEntries = 1200;//Integer.parseInt(args[4]);
        int maxRequestDuration =15;// Integer.parseInt(args[5]);

        NetworkTopology topology = new NetworkTopology(nodesFile,edgesFile);
        NFVState state = new NFVState(servicesFile, requests, true);
        Network network = new Network(topology, state, numberOfEntries, maxRequestDuration);
        network.startEvaluation();
    }
}
