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
import java.util.List;

public class NFVServiceAllocationTest
{
  //  /** Debug mode
    private static String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";// args[0];
    private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abileneHalfCapacity.edges";//args[1];
    private static String servicesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/frameworkConfiguration_AbileneML.json";
    private static String requests = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/pedidosAbilene_300.csv";//args[3];
    private static String evaluation = "phi";
    private static String serviceMapingFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/serviceMap.json";
    private static int populationSize = 5;
    private static int numberOfGenerations = 2;
    private static int lowerBound = 0;
    private static int upperBound = 7;
    private static double maxServices = 0;
    private static int cplexTimeLimit = 25;
    private static double alpha = 0.5;
  // */
    public static void main(String[] args) throws Exception {
/*
        if(args.length!=13)
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
        double maxServices = Double.parseDouble(args[9]);
        double alpha = Double.parseDouble(args[10]);
        int cplexTimeLimit = Integer.parseInt(args[11]);
        String evaluation = args[12].toLowerCase();
*/
        NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
        NFVState state = new NFVState(servicesFile, requests);

        ParamsNFV params = new ParamsNFV();
        params.setArchiveSize(100);
        params.setPopulationSize(populationSize);
        params.setNumberGenerations(numberOfGenerations);
        params.setCriteria(ParamsNFV.TerminationCriteria.ITERATION);

        switch (evaluation) {
            case "phimptcp":
                params.setAlgorithm(ParamsNFV.EvaluationAlgorithm.PHI_MPTCP);
                break;
            case "mlumptcp":
                params.setAlgorithm(ParamsNFV.EvaluationAlgorithm.MLU_MPTCP);
                break;
            case "phi":
                params.setAlgorithm(ParamsNFV.EvaluationAlgorithm.PHI);
                break;
            default:
                params.setAlgorithm(ParamsNFV.EvaluationAlgorithm.MLU);
                break;
        }

        JecoliNFV ea = new JecoliNFV(topology,state,lowerBound,upperBound, serviceMapingFile, maxServices,cplexTimeLimit, alpha);
        ea.configureNSGAII(params);
        ea.run();

        // Pareto Front Cenario
        if(maxServices < 1)
        {
            NondominatedPopulation p = new NondominatedPopulation(ea.getSolutionSet());
            int size = p.getNumberOfSolutions();
            List<IntegerSolution> aux = p.getLowestValuedSolutions(size);

            for(IntegerSolution sol : aux)
            {
                int[] solAux = sol.getVariablesArray();
                ConfigurationSolutionSaver.saveServicesLocationConfiguration(solAux,serviceMapingFile,topology,state, params.getAlgorithm(),topology.getDimension());
            }
        }
        else
        {
            Population p = new Population(ea.getSolutionSet());
            int solutions = p.getNumberOfSolutions();
            List<IntegerSolution> solAux = p.getLowestValuedSolutions(solutions);

            for(IntegerSolution sol : solAux)
            {
                int[] solution = sol.getVariablesArray();
                ConfigurationSolutionSaver.saveServicesLocationConfiguration(solution,serviceMapingFile,topology,state, params.getAlgorithm(),topology.getDimension());
            }

        }
    }
}
