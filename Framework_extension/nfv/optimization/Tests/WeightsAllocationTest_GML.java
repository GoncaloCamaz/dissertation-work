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
    public static void main(String[] args) throws Exception {

        if(args.length!=4)
            System.exit(1);

        String topoFile = args[0];
        String requestsFile = args[1];
        int populationSize = Integer.parseInt(args[2]);
        int numberOfGenerations = Integer.parseInt(args[3]);

        InputStream inputStream = new FileInputStream(topoFile);
        NetGraph netgraph = readGML(inputStream);

        NetworkTopology topology = new NetworkTopology(netgraph);

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
