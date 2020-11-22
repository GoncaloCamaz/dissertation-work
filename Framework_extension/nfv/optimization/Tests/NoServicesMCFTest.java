package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;

import pt.uminho.algoritmi.netopt.cplex.MCFMLUSolver;
import pt.uminho.algoritmi.netopt.cplex.MCFPhiSolver;
import pt.uminho.algoritmi.netopt.nfv.*;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;

import java.io.FileInputStream;
import java.io.InputStream;

import static pt.uminho.algoritmi.netopt.ospf.utils.io.GraphReader.readGML;


public class NoServicesMCFTest
{
    private static String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";// args[0];
    private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abileneHalfCapacity.edges";//args[1];
    private static String topoFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/BT Europe/BtEurope.gml";
    private static String servicesFile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\frameworkConfiguration_BTEurope.json";
    private static String requestsFile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\pedidosBTEurope_1200.csv";

    public static void main(String[] args) throws Exception {
      //  NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
        InputStream inputStream = new FileInputStream(topoFile);
        NetGraph netgraph = readGML(inputStream);

        NetworkTopology topology = new NetworkTopology(netgraph);

        double cap[][] = topology.getNetGraph().createGraph().getCapacitie();
        for(int i = 0; i < topology.getDimension(); i++)
            for(int j = 0; j < topology.getDimension(); j++)
                if(cap[i][j] > 0)
                    topology.getNetGraph().setBandwidth(i,j,750);

        NFVState state = new NFVState(servicesFile, requestsFile);
        NFRequestsMap req = state.getRequests();

        double[][] d = convertToDemands(topology, req);
        Demands demands = new Demands(d);

        MCFPhiSolver solver = new MCFPhiSolver(topology, demands);
        double congestionVal = solver.optimize();

        MCFMLUSolver solverMLU = new MCFMLUSolver(topology,demands);
        double mluVal = solverMLU.optimize();

        System.out.println("Congestion PHI: " + congestionVal);
        System.out.println("Congestion MLU: " + mluVal);
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
