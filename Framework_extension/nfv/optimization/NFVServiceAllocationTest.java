package pt.uminho.algoritmi.netopt.nfv.optimization;

import pt.uminho.algoritmi.netopt.nfv.NFVState;
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
    /*
    private static String nodesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";
    private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.edges";
    private static String requests = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/pedidos.csv";
    private static String servicesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/frameworkConfiguration.json";
    private static String serviceMapingFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/serviceMap.json";
    private static int populationSize = 100;
    private static int numberOfGenerations = 10;
    private static int lowerBound = 0;
    private static int upperBound = 7;
    private static int maxServices = 12;
    private static int cplexTimeLimit =60;
    */
    public static void main(String[] args) throws Exception {

        if(args.length!=11)
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
        int maxServices = Integer.parseInt(args[9]);
        int cplexTimeLimit = Integer.parseInt(args[10]);

        NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
        NFVState state = new NFVState(servicesFile, requests);

        ParamsNFV params = new ParamsNFV();
        params.setArchiveSize(100);
        params.setPopulationSize(populationSize);
        params.setNumberGenerations(numberOfGenerations);

        JecoliNFV ea = new JecoliNFV(topology,state,lowerBound,upperBound, serviceMapingFile, maxServices,cplexTimeLimit);
        ea.configureNSGAII(params);
        ea.run();

        Population p = new NondominatedPopulation(ea.getSolutionSet());
        save(p,maxServices);
    }

    public static void save(Population p, int limit) {
        IntegerSolution sol = p.getLowestValuedSolutions(0, 1).get(0);

        FileWriter f;
        try {
            f = new FileWriter("SERVICES_ServLimit_"+limit +"_"+ System.currentTimeMillis() + ".csv", true);
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
