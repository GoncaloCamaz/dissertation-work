package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;

import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;

import java.io.FileInputStream;
import java.io.InputStream;

import static pt.uminho.algoritmi.netopt.ospf.utils.io.GraphReader.readGML;

public class TestTopologySize
{
    private static String nodesFileAbi ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";
    private static String edgesFileAbi = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.edges";

    private static String nodesFile30 ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.nodes";
    private static String edgesFile30 = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.edges";

    private static String nodesFile30_3 ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_3/isno_30_3.nodes";
    private static String edgesFile30_3 = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_3/isno_30_3.edges";

    private static String nodesFile30_4 ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_4/isno_30_4.nodes";
    private static String edgesFile30_4 = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_4/isno_30_4.edges";

    private static String nodesFile50_2 ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/50_2/isno_50_2.nodes";
    private static String edgesFile50_2 = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/50_2/isno_50_2.edges";

    private static String fileBTN ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/BTN/btn.gml";

    private static String fileGEANT ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/GEANT/geant.gml";

    private static String fileeurope ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/BT Europe/BtEurope.gml";




    public static void main(String[] args) throws Exception {
        NetworkTopology topologyAbi = new NetworkTopology(nodesFileAbi,edgesFileAbi);
        System.out.println("Abilene: " + topologyAbi.getDimension() +" Nodes; " + topologyAbi.getNumberEdges() + " Edges;\n");

        NetworkTopology topology30 = new NetworkTopology(nodesFile30,edgesFile30);
        System.out.println("30_2: " + topology30.getDimension() +" Nodes; " + topology30.getNumberEdges() + " Edges;\n");

        NetworkTopology topology30_3 = new NetworkTopology(nodesFile30_3,edgesFile30_3);
        System.out.println("30_3: " + topology30_3.getDimension() +" Nodes; " + topology30_3.getNumberEdges() + " Edges;\n");

        NetworkTopology topology30_4 = new NetworkTopology(nodesFile30_4,edgesFile30_4);
        System.out.println("30_4: " + topology30_4.getDimension() +" Nodes; " + topology30_4.getNumberEdges() + " Edges;\n");

        NetworkTopology topology50_2 = new NetworkTopology(nodesFile50_2,edgesFile50_2);
        System.out.println("50_2: " + topology50_2.getDimension() +" Nodes; " + topology50_2.getNumberEdges() + " Edges;\n");

        InputStream inputstream;

        try {
            inputstream = new FileInputStream(fileBTN);
            NetGraph netgraph = readGML(inputstream);
            NetworkTopology topology = new NetworkTopology(netgraph);
            System.out.println("BTN: " + topology.getDimension() +" Nodes; " + topology.getNumberEdges() + " Edges;\n");

            inputstream = new FileInputStream(fileeurope);
            NetGraph netgraphG = readGML(inputstream);
            NetworkTopology topologyG = new NetworkTopology(netgraphG);
            System.out.println("BTEurope: " + topologyG.getDimension() +" Nodes; " + topologyG.getNumberEdges() + " Edges;\n");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
