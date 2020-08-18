package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;

import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.optimization.ParamsNFV;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.ConfigurationSolutionSaver;
import pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.JecoliNFV;

import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.NondominatedPopulation;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;

import java.io.*;
import java.util.List;

import static pt.uminho.algoritmi.netopt.ospf.utils.io.GraphReader.readGML;

public class NFVServiceAllocationTest_GML
{
     /**Debug mode
     private static String topoFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/BT Europe/BtEurope.gml";
     private static String servicesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/frameworkConfiguration_BTEurope.json";
     private static String requests = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/pedidosBTEurope_300.csv";//args[3];
     private static String evaluation = "phi";
     private static String serviceMapingFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/serviceMap.json";
     private static int populationSize = 10;
     private static int numberOfGenerations = 1;
     private static int lowerBound = 0;
     private static int upperBound = 7;
     private static double maxServices = 0.5;
     private static int cplexTimeLimit =20;
     private static double alpha = 0.5;
    */

    public static void main(String[] args) throws Exception {

        if(args.length!=12)
            System.exit(1);

        String topoFile = args[0];
        String requests = args[1];
        String servicesFile =args[2];
        String serviceMapingFile = args[3];
        int populationSize = Integer.parseInt(args[4]);
        int numberOfGenerations = Integer.parseInt(args[5]);
        int lowerBound = Integer.parseInt(args[6]);
        int upperBound = Integer.parseInt(args[7]);
        double maxServices = Double.parseDouble(args[8]);
        double alpha = Double.parseDouble(args[9]);
        int cplexTimeLimit = Integer.parseInt(args[10]);
        String evaluation = args[11].toLowerCase();

        InputStream inputStream = new FileInputStream(topoFile);
        NetGraph netgraph = readGML(inputStream);

        NetworkTopology topology = new NetworkTopology(netgraph);
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

        JecoliNFV ea = new JecoliNFV(topology,state,lowerBound,upperBound, serviceMapingFile, maxServices,cplexTimeLimit, alpha);
        ea.configureNSGAII(params);
        ea.run();

        if(maxServices < 1)
        {
            NondominatedPopulation p = new NondominatedPopulation(ea.getSolutionSet());
            int size = p.getNumberOfSolutions();
            List<IntegerSolution> aux = p.getLowestValuedSolutions(size);

            for(IntegerSolution sol : aux)
            {
                int[] solAux = sol.getVariablesArray();
                ConfigurationSolutionSaver.saveServicesLocationConfiguration(solAux,serviceMapingFile,topology,state, params.getAlgorithm());
            }
        }
        else
        {
            Population p = new NondominatedPopulation(ea.getSolutionSet());
            int[] solution = p.getLowestValuedSolutions(0, 1).get(0).getVariablesArray();
            ConfigurationSolutionSaver.saveServicesLocationConfiguration(solution,serviceMapingFile,topology,state, params.getAlgorithm());
        }
    }
}
