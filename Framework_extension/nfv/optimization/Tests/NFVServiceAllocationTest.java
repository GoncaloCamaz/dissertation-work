package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;

import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.optimization.ParamsNFV;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.ConfigurationSolutionSaver;
import pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.JecoliNFV;

import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.NondominatedPopulation;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class NFVServiceAllocationTest
{
    /** Debug mode
    private static String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.nodes";// args[0];
    private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.edges";//args[1];
    private static String servicesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/frameworkConfiguration.json";
    private static String requests = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/pedidos.csv";//args[3];
    private static String evaluation = "phi";
    private static String serviceMapingFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/serviceMap.json";
    private static int populationSize = 10;
    private static int numberOfGenerations = 1;
    private static int lowerBound = 0;
    private static int upperBound = 7;
    private static double maxServices = 0.5;
    private static int cplexTimeLimit =20;
    */
    public static void main(String[] args) throws Exception {

        if(args.length!=12)
           System.exit(1);

        String nodesFile = args[0];
        String edgesFile = args[1];
        String requests = args[2];
        String servicesFile =args[3];
        String serviceMapingFile = args[4];
        int populationSize = Integer.parseInt(args[5]);
        int numberOfGenerations = Integer.parseInt(args[6]);
        int lowerBound = Integer.parseInt(args[7]);
        int upperBound = Integer.parseInt(args[8]);
        double maxServices = Integer.parseInt(args[9]); //Can either be a number > 1 where limits te maximum number of services
        //applying a penalization to the fitness's or it can be a percentage (number < 1) defining the importance of the fitness
        int cplexTimeLimit = Integer.parseInt(args[10]);
        String evaluation = args[11].toLowerCase();

        NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
        NFVState state = new NFVState(servicesFile, requests);

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

        JecoliNFV ea = new JecoliNFV(topology,state,lowerBound,upperBound, serviceMapingFile, maxServices,cplexTimeLimit);
        ea.configureNSGAII(params);
        ea.run();
        Population p = new NondominatedPopulation(ea.getSolutionSet());

        int[] solution = p.getLowestValuedSolutions(0, 1).get(0).getVariablesArray();
        String filename = ConfigurationSolutionSaver.saveServicesLocationConfiguration(solution,serviceMapingFile,topology,state, params.getAlgorithm());
        if(maxServices < 1)
        {
            double[][] res = p.getParetoMatrix();
            ConfigurationSolutionSaver.saveParetoToCSV(res, res[0].length,filename);
        }
    }
}
