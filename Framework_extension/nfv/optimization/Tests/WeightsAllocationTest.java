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

public class WeightsAllocationTest
{
    public static void main(String[] args) throws Exception {

        if(args.length!=6)
            System.exit(1);

        String nodesFile = args[0];
        String edgesFile = args[1];
        String requestsFile = args[2];
        int populationSize = Integer.parseInt(args[3]);
        int numberOfGenerations = Integer.parseInt(args[4]);
        String mode = args[5];

        NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);


        List<Request> req = SRSolutionLoader.loadResultsFromJson(requestsFile);
        double congestionValue = SRSolutionLoader.loadCongestionval(requestsFile, mode);
        ParamsNFV params = new ParamsNFV();
        params.setArchiveSize(100);
        params.setPopulationSize(populationSize);
        params.setNumberGenerations(numberOfGenerations);
        params.setCriteria(ParamsNFV.TerminationCriteria.ITERATION);

        JecoliWeights ea = new JecoliWeights(topology,req, congestionValue);
        ea.configureEvolutionaryAlgorithm(params);
        ea.run();

        Population p = new NondominatedPopulation(ea.getSolutionSet());
        save(p, topology,req.size());
    }

    public static void save(Population p, NetworkTopology topology, int s){
        try {
            WeightsSolutionSaver.save(p, topology,1,s);
        } catch (DimensionErrorException e) {
            e.printStackTrace();
        }
    }
}
