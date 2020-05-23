package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;

import pt.uminho.algoritmi.netopt.nfv.NFVState;

import pt.uminho.algoritmi.netopt.nfv.optimization.ParamsNFV;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.Request;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.SRSolutionLoader;
import pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.JecoliWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.NondominatedPopulation;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class WeightsAllocationTest
{

    private static String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.nodes";// args[0];
    private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.edges";//args[1];
    private static String requestsFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/Configuration_30.json";
    private static int populationSize = 5;
    private static int numberOfGenerations = 1;

    public static void main(String[] args) throws Exception {
/*
        if(args.length!=5)
            System.exit(1);

        String nodesFile = args[0];
        String edgesFile = args[1];
        String requestsFile = args[2];
        int populationSize = Integer.parseInt(args[4]);
        int numberOfGenerations = Integer.parseInt(args[5]);
*/
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
        save(p);
    }

    public static void save(Population p) {
        IntegerSolution sol = p.getLowestValuedSolutions(0, 1).get(0);

        FileWriter f;
        try {
            f = new FileWriter("IGP_"+ System.currentTimeMillis() + ".csv", true);
            BufferedWriter W = new BufferedWriter(f);
            W.write(sol.toString());
            W.write("\n");
            W.flush();
            W.close();
            f.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
