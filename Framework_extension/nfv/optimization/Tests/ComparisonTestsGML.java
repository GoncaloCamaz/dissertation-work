package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;

import pt.uminho.algoritmi.netopt.nfv.optimization.ParamsNFV;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.IGPWeightsOptimizationInputObject;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.Request;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.SRSolutionLoader;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.WeightsSolutionSaver;
import pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.JecoliWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.NondominatedPopulation;

import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Flow;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRSimulator;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import static pt.uminho.algoritmi.netopt.ospf.utils.io.GraphReader.readGML;

public class ComparisonTestsGML
{
    private static String topoFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/BT Europe/BtEurope.gml";
    private static String requestsFile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\NewAnalysisConfigurations\\BT\\SOEA\\Analise\\";// args[3]
    private static int populationSize = 100;
    private static int numberOfGenerations = 150;

    public static void main(String[] args) throws Exception {
        int size3 = 300;
        int size12 = 1200;
        String req300 = "LC";
        String req1200 = "LC";
        String file1 = "EA_PHI_";
        String file2 = "EA_MLU_";
        Boolean low = true;
        String mode1 = "phi";
        String mode2 = "mlu";

        InputStream inputStream = new FileInputStream(topoFile);
        NetGraph netgraph = readGML(inputStream);
        NetworkTopology topology = new NetworkTopology(netgraph);
        if(low)
        {
            double[][] capacity = topology.getNetGraph().createGraph().getCapacitie();
            int nodesNumber = 24;
            for (int i = 0; i < nodesNumber; i++)
            {
                for (int j = 0; j < nodesNumber; j++) {
                    if (capacity[i][j] > 0) {
                        topology.getNetGraph().setBandwidth(i,j,750);
                    }
                }
            }
        }


        IGPWeightsOptimizationInputObject req = SRSolutionLoader.loadResultsFromJson(mode1,requestsFile+file1+req300+".json");
        ParamsNFV params = new ParamsNFV();
        params.setArchiveSize(100);
        params.setPopulationSize(populationSize);
        params.setNumberGenerations(numberOfGenerations);
        params.setCriteria(ParamsNFV.TerminationCriteria.ITERATION);

        JecoliWeights ea = new JecoliWeights(topology,req.getRequestList(),req.getMilpResult());
        ea.configureEvolutionaryAlgorithm(params);
        ea.run();

        Population p = new Population(ea.getSolutionSet());
        List<IntegerSolution> sol = p.getLowestValuedSolutions(15);
        for(IntegerSolution s : sol)
        {
            double[] result = evaluate(topology,req.getRequestList(),s);
            save(s,topology,result[0],result[1], size3);
        }
        System.out.println("Ended first analysis");


        IGPWeightsOptimizationInputObject req1 = SRSolutionLoader.loadResultsFromJson(mode2,requestsFile+file2+req1200+".json");
        ParamsNFV params1 = new ParamsNFV();
        params1.setArchiveSize(100);
        params1.setPopulationSize(populationSize);
        params1.setNumberGenerations(numberOfGenerations);
        params1.setCriteria(ParamsNFV.TerminationCriteria.ITERATION);

        JecoliWeights ea1 = new JecoliWeights(topology,req1.getRequestList(),req1.getMilpResult());
        ea1.configureEvolutionaryAlgorithm(params1);
        ea1.run();

        Population p1 = new Population(ea1.getSolutionSet());
        List<IntegerSolution> sol1 = p1.getLowestValuedSolutions(15);
        for(IntegerSolution s1 : sol1)
        {
            double[] result1 = evaluate(topology,req1.getRequestList(),s1);
            save(s1,topology,result1[0],result1[1], size12);
        }
        System.out.println("Ended second analysis");

    }

    public static void save(IntegerSolution p, NetworkTopology topology,double phi,double mlu, int s){
        try {
            WeightsSolutionSaver.saveAux(p, topology,phi,mlu,s);
        } catch (DimensionErrorException e) {
            e.printStackTrace();
        }
    }

    public static double[] evaluate(NetworkTopology topology, List<Request> requests, IntegerSolution solutionRepresentation) throws Exception
    {
        double[] result = new double[2];
        int nodes = topology.getDimension();
        int numberOfRequests = requests.size();
        int weights[] = decode(solutionRepresentation, topology.getNumberEdges());
        OSPFWeights weightsOSPF = new OSPFWeights(nodes);
        weightsOSPF.setWeights(weights,topology);

        SRSimulator simulator = new SRSimulator(topology,weightsOSPF);
        for(int i = 0; i < numberOfRequests ; i++)
        {
            Request r = requests.get(i);
            for(Flow f : r.getFlow())
            {
                simulator.addFlow(f);
            }
        }
        result[0] = simulator.getCongestionValue();// new Double(object.getPhiValue());
        result[1] = simulator.getMLU();

        return result;
    }

    public static int[] decode(IntegerSolution solution, int edges)
    {
        int[] result = new int[edges];
        result = solution.getVariablesArray();

        return result;
    }
}
