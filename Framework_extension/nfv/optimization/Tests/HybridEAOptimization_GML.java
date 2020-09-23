package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;

import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.optimization.ParamsNFV;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.ConfigurationSolutionSaver;
import pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.JecoliHybrid;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.NondominatedPopulation;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import static pt.uminho.algoritmi.netopt.ospf.utils.io.GraphReader.readGML;

public class HybridEAOptimization_GML
{
    /** Debug mode
     private static String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";// args[0];
     private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.edges";//args[1];
     private static String configurationFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/frameworkConfiguration_Abilene.json";
     private static String requests = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/pedidosAbilene_300.csv";//args[3];
     private static String evaluation = "phi";
     private static String serviceMapingFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/serviceMap.json";
     private static int populationSize = 5;
     private static int numberOfGenerations = 5;
     private static int lowerBoundConf = 0;
     private static int upperBoundConf = 7;
     private static int lowerBoundIGP = 1;
     private static int upperBoundIGP = 20;
     private static int cplexTimeLimit = 25;
     private static double alpha = 0.5;
     */

    public static void main(String[] args) throws Exception {

        if(args.length!=13)
            System.exit(1);

        String topoFile = args[0];
        String requests = args[1];
        String configurationFile =args[2];
        String serviceMapingFile = args[3];
        int populationSize = Integer.parseInt(args[4]);
        int numberOfGenerations = Integer.parseInt(args[5]);
        int lowerBoundConf = Integer.parseInt(args[6]);
        int upperBoundConf = Integer.parseInt(args[7]);
        int lowerBoundIGP = Integer.parseInt(args[8]);
        int upperBoundIGP = Integer.parseInt(args[9]);
        double alpha = Double.parseDouble(args[10]);
        int cplexTimeLimit = Integer.parseInt(args[11]);
        String evaluation = args[12].toLowerCase();

        InputStream inputStream = new FileInputStream(topoFile);
        NetGraph netgraph = readGML(inputStream);

        NetworkTopology topology = new NetworkTopology(netgraph);
        NFVState state = new NFVState(configurationFile, requests);

        ParamsNFV params = new ParamsNFV();
        params.setArchiveSize(100);
        params.setPopulationSize(populationSize);
        params.setNumberGenerations(numberOfGenerations);
        params.setCriteria(ParamsNFV.TerminationCriteria.ITERATION);

        if (evaluation.equals("phi"))
        {
            params.setAlgorithm(ParamsNFV.EvaluationAlgorithm.PHI);
        }
        else
        {
            params.setAlgorithm(ParamsNFV.EvaluationAlgorithm.MLU);
        }

        JecoliHybrid ea = new JecoliHybrid(topology,state,lowerBoundConf,upperBoundConf, lowerBoundIGP, upperBoundIGP, serviceMapingFile,cplexTimeLimit, alpha);
        ea.configureNSGAII(params);
        ea.run();


        Population p = new NondominatedPopulation(ea.getSolutionSet());
        List<IntegerSolution> solAux = p.getLowestTradeOffSolutions(0.5);

        for(IntegerSolution sol : solAux)
        {
            int[] solution = sol.getVariablesArray();
            ConfigurationSolutionSaver.saveServicesLocationConfiguration(solution,serviceMapingFile,topology,state,params.getAlgorithm());
        }
    }
}
