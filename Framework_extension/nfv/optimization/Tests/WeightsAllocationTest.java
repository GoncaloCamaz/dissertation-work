package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;

import pt.uminho.algoritmi.netopt.cplex.utils.SourceDestinationPair;
import pt.uminho.algoritmi.netopt.nfv.optimization.ParamsNFV;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.Request;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.SRSolutionLoader;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.WeightsSolutionSaver;
import pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.JecoliWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.NondominatedPopulation;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class WeightsAllocationTest
{

    /** Debug Mode **
    private static String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.nodes";
    private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.edges";
    private static String requestsFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/Configuration_30.json";
    private static int populationSize = 5;
    private static int numberOfGenerations = 1;

     */
    public static void main(String[] args) throws Exception {

        if(args.length!=5)
            System.exit(1);

        String nodesFile = args[0];
        String edgesFile = args[1];
        String requestsFile = args[2];
        int populationSize = Integer.parseInt(args[3]);
        int numberOfGenerations = Integer.parseInt(args[4]);

        NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
        List<Request> req = SRSolutionLoader.loadResultsFromJson(requestsFile);
        ParamsNFV params = new ParamsNFV();
        params.setArchiveSize(100);
        params.setPopulationSize(populationSize);
        params.setNumberGenerations(numberOfGenerations);
        params.setCriteria(ParamsNFV.TerminationCriteria.ITERATION);

        JecoliWeights ea = new JecoliWeights(topology,req);
        ea.configureEvolutionaryAlgorithm(params);
        ea.run();

        Population p = new NondominatedPopulation(ea.getSolutionSet());
        save(p, topology);
    }

    public static void save(Population p, NetworkTopology topology){
        try {
            WeightsSolutionSaver.save(p, topology);
        } catch (DimensionErrorException e) {
            e.printStackTrace();
        }
    }
}
