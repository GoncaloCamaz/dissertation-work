package pt.uminho.algoritmi.netopt.nfv.optimization;

import pt.uminho.algoritmi.netopt.cplex.MCFPhiNodeUtilizationSolver;
import pt.uminho.algoritmi.netopt.nfv.NFNodesMap;
import pt.uminho.algoritmi.netopt.nfv.NFRequestsMap;
import pt.uminho.algoritmi.netopt.nfv.NFServicesMap;
import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.JecoliNFV;
import pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.PopulationNFV;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class NFVServiceAllocationTest
{
    private static String nodesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";
    private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.edges";
    private static String requests = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/pedidos.csv";
    private static String servicesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/frameworkConfiguration.json";

    public static void main(String[] args) throws Exception {
        //if(args.length!=4)
          //  System.exit(1);

        //String nodesFile = args[0];
        //String edgesFile = args[1];
        //String requestsFile = args[2];
        //String servicesFile =args[3];

        NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
        NFVState state = new NFVState(servicesFile, requests);
        NFServicesMap services = state.getServices();
        NFNodesMap map = state.getNodes();
        NFRequestsMap req = state.getRequests();

        MCFPhiNodeUtilizationSolver solver = new MCFPhiNodeUtilizationSolver(topology, services, req, map);
        solver.setSaveLoads(true);
        solver.optimize();

        ParamsNFV params = new ParamsNFV();
        params.setArchiveSize(100);
        params.setPopulationSize(100);
        params.setNumberGenerations(1);

        JecoliNFV ea = new JecoliNFV(topology, map,req,services,0,7);
        ea.configureNSGAII(params);
        ea.run();

        PopulationNFV p = new NondominatedPopulationNFV(ea.getSolutionSet());
        save(p);
    }

    public static void save(PopulationNFV p) {
        IntegerSolution sol = p.getLowestValuedSolutions(0, 1).get(0);

        FileWriter f;
        try {
            f = new FileWriter("" + System.currentTimeMillis() + ".csv", true);
            BufferedWriter W = new BufferedWriter(f);
            W.write(sol.toString());
            W.write("\n");
            W.flush();
            W.close();
            f.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
