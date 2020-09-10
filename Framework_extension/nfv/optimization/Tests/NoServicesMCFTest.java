package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;

import pt.uminho.algoritmi.netopt.cplex.MCFPhiSolver;
import pt.uminho.algoritmi.netopt.nfv.*;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;


public class NoServicesMCFTest
{
    private static String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.nodes";// args[0];
    private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.edges";//args[1];
    private static String servicesFile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Resultados\\Random\\30\\1200\\frameworkConfiguration.json";
    private static String requestsFile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Resultados\\Random\\30\\300\\pedidos300.csv";

    public static void main(String[] args) throws Exception {
        NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
        NFVState state = new NFVState(servicesFile, requestsFile);
        NFRequestsMap req = state.getRequests();

        double[][] d = convertToDemands(topology, req);
        Demands demands = new Demands(d);

        MCFPhiSolver solver = new MCFPhiSolver(topology, demands);
        double congestionVal = solver.optimize();

        System.out.println("Congestion: " + congestionVal);
    }

    private static double[][] convertToDemands(NetworkTopology topology, NFRequestsMap requests)
    {
        double[][] demands = new double[topology.getDimension()][topology.getDimension()];

        for(int i = 0; i < topology.getDimension(); i++)
        {
            for(int j = 0; j < topology.getDimension(); j++)
            {
                demands[i][j] = 0;
            }
        }

        for(NFRequest req : requests.getRequestMap().values())
        {
            demands[req.getSource()][req.getDestination()] += req.getBandwidth();
        }

        return demands;
    }
}
