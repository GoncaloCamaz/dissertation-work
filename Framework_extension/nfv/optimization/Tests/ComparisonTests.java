package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;


import pt.uminho.algoritmi.netopt.nfv.optimization.ParamsNFV;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.Request;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.SRSolutionLoader;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.WeightsSolutionSaver;
import pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.JecoliWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.NondominatedPopulation;

import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;

import java.util.List;

public class ComparisonTests
{

    ///** Debug Mode **
    private static String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.nodes";// args[0];
    private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.edges";//args[1];
    private static String servicesFile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Resultados\\Random\\30\\1200\\frameworkConfiguration.json";
    private static String requestsFile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Resultados\\Random\\30\\";// args[3]
    private static int populationSize = 100;
    private static int numberOfGenerations = 100;

    //  */
    public static void main(String[] args) throws Exception {
/*
        if(args.length!=5)
            System.exit(1);

        String nodesFile = args[0];
        String edgesFile = args[1];
        String requestsFile = args[2];
        int populationSize = Integer.parseInt(args[3]);
        int numberOfGenerations = Integer.parseInt(args[4]);
*/
        int size3 = 300;
        int size12 = 1200;
        String req300 = "300";
        String req1200 = "1200";
        String file1 = "PHI_30_1200.json";
        String file2 = "MLU_30_1200.json";
        String file3 = "PHI_300_30.json";
        String file4 = "MLU_300_30.json";

        NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);

        List<Request> req = SRSolutionLoader.loadResultsFromJson(requestsFile+req1200+"\\"+file1);
        ParamsNFV params = new ParamsNFV();
        params.setArchiveSize(100);
        params.setPopulationSize(populationSize);
        params.setNumberGenerations(numberOfGenerations);
        params.setCriteria(ParamsNFV.TerminationCriteria.ITERATION);

        JecoliWeights ea = new JecoliWeights(topology,req);
        ea.configureEvolutionaryAlgorithm(params);
        ea.run();

        Population p = new NondominatedPopulation(ea.getSolutionSet());
        save(p, topology, size12);
        System.out.println("Finished " + file1);

        List<Request> req1 = SRSolutionLoader.loadResultsFromJson(requestsFile+req1200+"\\"+file2);
        ParamsNFV params1 = new ParamsNFV();
        params1.setArchiveSize(100);
        params1.setPopulationSize(populationSize);
        params1.setNumberGenerations(numberOfGenerations);
        params1.setCriteria(ParamsNFV.TerminationCriteria.ITERATION);

        JecoliWeights ea1 = new JecoliWeights(topology,req1);
        ea1.configureEvolutionaryAlgorithm(params1);
        ea1.run();

        Population p1 = new NondominatedPopulation(ea1.getSolutionSet());
        save(p1, topology, size12+1);
        System.out.println("Finished " + file2);

        List<Request> req2 = SRSolutionLoader.loadResultsFromJson(requestsFile+req300+"\\"+file3);
        ParamsNFV params2 = new ParamsNFV();
        params2.setArchiveSize(100);
        params2.setPopulationSize(populationSize);
        params2.setNumberGenerations(numberOfGenerations);
        params2.setCriteria(ParamsNFV.TerminationCriteria.ITERATION);

        JecoliWeights ea2 = new JecoliWeights(topology,req2);
        ea2.configureEvolutionaryAlgorithm(params2);
        ea2.run();

        Population p2 = new NondominatedPopulation(ea2.getSolutionSet());
        save(p2, topology, size3);
        System.out.println("Finished " + file3);

        List<Request> req3 = SRSolutionLoader.loadResultsFromJson(requestsFile+req300+"\\"+file4);
        ParamsNFV params3 = new ParamsNFV();
        params3.setArchiveSize(100);
        params3.setPopulationSize(populationSize);
        params3.setNumberGenerations(numberOfGenerations);
        params3.setCriteria(ParamsNFV.TerminationCriteria.ITERATION);

        JecoliWeights ea3 = new JecoliWeights(topology,req3);
        ea3.configureEvolutionaryAlgorithm(params3);
        ea3.run();

        Population p3 = new NondominatedPopulation(ea3.getSolutionSet());
        save(p3, topology, size3+1);
        System.out.println("Finished " + file4);
    }

    public static void save(Population p, NetworkTopology topology, int s){
        try {
            WeightsSolutionSaver.save(p, topology,s);
        } catch (DimensionErrorException e) {
            e.printStackTrace();
        }
    }
}

