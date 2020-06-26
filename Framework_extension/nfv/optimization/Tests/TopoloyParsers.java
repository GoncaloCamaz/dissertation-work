package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;

import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static pt.uminho.algoritmi.netopt.ospf.utils.io.GraphReader.readGML;

public class TopoloyParsers
{
    public static void main(String[] args)
    {
        String graphFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/BTN/btn.gml";
        InputStream inputstream;

        try {
            inputstream = new FileInputStream(graphFile);
            NetGraph netgraph = readGML(inputstream);
            NetworkTopology topology = new NetworkTopology(netgraph);

            int edges = netgraph.getNEdges();
            int nodes = netgraph.getNNodes();
            double[][] capacitie = topology.getNetGraph().createGraph().getCapacitie();
            for(int i = 0; i < nodes; i++)
            {
                for(int j = 0; j < nodes; j++)
                {
                    if(capacitie[i][j] > 0)
                    {
                        System.out.println(capacitie[i][j]);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
