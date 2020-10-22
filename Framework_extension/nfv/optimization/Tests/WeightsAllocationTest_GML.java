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
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import static pt.uminho.algoritmi.netopt.ospf.utils.io.GraphReader.readGML;

public class WeightsAllocationTest_GML
{
    /** Debug Mode
    private static String topoFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/BT Europe/BtEurope.gml";
    private static String requestsFile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Resultados\\Random\\BT\\";
    private static int populationSize = 100;
    private static int numberOfGenerations = 100;
    */
    public static void main(String[] args) throws Exception {

        if(args.length!=6)
            System.exit(1);

        String topoFile = args[0];
        String requestsFile = args[1];
        int populationSize = Integer.parseInt(args[2]);
        int numberOfGenerations = Integer.parseInt(args[3]);
        String mode = args[4];
        String lowmode = args[5].toLowerCase();

        InputStream inputStream = new FileInputStream(topoFile);
        NetGraph netgraph = readGML(inputStream);

        NetworkTopology topology = new NetworkTopology(netgraph);

        if(lowmode.equals("low"))
        {
            double[][] capacity = topology.getNetGraph().createGraph().getCapacitie();
            int nodesNumber = topology.getDimension();
            for (int i = 0; i < nodesNumber; i++)
                for (int j = 0; j < nodesNumber; j++) {
                    if (capacity[i][j] > 0) {
                        topology.getNetGraph().setBandwidth(i,j,750);
                    }
                }
        }

        List<Request> req = SRSolutionLoader.loadResultsFromJson(requestsFile);
        double congestion = SRSolutionLoader.loadCongestionval(requestsFile, mode);
        ParamsNFV params = new ParamsNFV();
        params.setArchiveSize(100);
        params.setPopulationSize(populationSize);
        params.setNumberGenerations(numberOfGenerations);
        params.setCriteria(ParamsNFV.TerminationCriteria.ITERATION);

        JecoliWeights ea = new JecoliWeights(topology,req, congestion);
        ea.configureEvolutionaryAlgorithm(params);
        ea.run();

        Population p = new NondominatedPopulation(ea.getSolutionSet());
        save(p, topology, req.size());
    }

    public static void save(Population p, NetworkTopology topology, int s){
        try {
            WeightsSolutionSaver.save(p, topology,1, s);
        } catch (DimensionErrorException e) {
            e.printStackTrace();
        }
    }
}
