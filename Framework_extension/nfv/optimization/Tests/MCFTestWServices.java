package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;

import pt.uminho.algoritmi.netopt.cplex.NFV_MCFPMLUSolver;
import pt.uminho.algoritmi.netopt.cplex.NFV_MCFPhiSolver;
import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;

import java.io.FileInputStream;
import java.io.InputStream;

import static pt.uminho.algoritmi.netopt.ospf.utils.io.GraphReader.readGML;


public class MCFTestWServices
{
    private static String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";// args[0];
    private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.edges";//args[1];
    private static String topoFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/BT Europe/BtEurope.gml";
    private static String servicesFile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\frameworkConfiguration_Abilene_EA.json";
    private static String requestsFile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\pedidosAbilene_300.csv";

    public static void main(String[] args) throws Exception {
        NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
        InputStream inputStream = new FileInputStream(topoFile);
        NetGraph netgraph = readGML(inputStream);

       // NetworkTopology topology = new NetworkTopology(netgraph);
        NFVState state = new NFVState(servicesFile, requestsFile);


        NFV_MCFPhiSolver phiSolver = new NFV_MCFPhiSolver(topology,state,60,0.5);
        OptimizationResultObject congestionVal = phiSolver.optimize();

        NFV_MCFPMLUSolver solverMLU = new NFV_MCFPMLUSolver(topology,state,60);
        OptimizationResultObject mluVal = solverMLU.optimize();

        System.out.println("Congestion PHI: " + congestionVal.getPhiValue());
        System.out.println("Congestion MLU: " + mluVal.getMlu());
    }

}
