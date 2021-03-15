package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;

import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.IGPWeightsOptimizationInputObject;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.Request;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.SRSolutionLoader;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Flow;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRSimulator;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import static pt.uminho.algoritmi.netopt.ospf.utils.io.GraphReader.readGML;

public class ResultAnalysis_RandomWeights
{
    private static String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.nodes";// args[0];
    private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.edges";//args[1];
    private static String requestsFile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\NewAnalysisConfigurations\\BT\\SOEA\\Analise\\";// args[3]
    private static String topoFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/BT Europe/BtEurope.gml";

    public ResultAnalysis_RandomWeights() throws Exception {
    }

    public static void main(String[] args) throws Exception {
        int size3 = 300;
        int size12 = 1200;
        String req1200 = "1200";
        String file2 = "EA_PHI_1200.json";
        Boolean low = false;
        String mode = "phi";


        InputStream inputStream = new FileInputStream(topoFile);
        NetGraph netgraph = readGML(inputStream);
       NetworkTopology topology = new NetworkTopology(netgraph);

        if(low)
        {
            double[][] capacity = topology.getNetGraph().createGraph().getCapacitie();
            int nodesNumber = 24;
            for(int i = 0; i < nodesNumber; i++)
                for (int j = 0; j < nodesNumber; j++) {
                    if (capacity[i][j] > 0) {
                        topology.getNetGraph().setBandwidth(i,j,750);
                    }
                }
        }

      //  NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
        double[][] capacity = topology.getNetGraph().createGraph().getCapacitie();
        int nodesNumber = 24;
        for(int i = 0; i < nodesNumber; i++)
            for (int j = 0; j < nodesNumber; j++) {
                if (capacity[i][j] > 0) {
                    System.out.println(capacity[i][j]);
                }
            }
        double phi = 0;
        double mlu = 0;
        for(int i = 0; i<15; i++)
        {
            IGPWeightsOptimizationInputObject req = SRSolutionLoader.loadResultsFromJson(mode,requestsFile+file2);
            double[] result = evaluate(topology,req.getRequestList());
            mlu += result[1];
            phi += result[0];
        }


        System.out.println("MLU: " + mlu/15);
        System.out.println("PHI: " + phi/15);
    }

    public static double[] evaluate(NetworkTopology topology, List<Request> requests) throws Exception
    {
        double[] result = new double[2];
        int nodes = topology.getDimension();
        int numberOfRequests = requests.size();
        int weights[] = random(topology.getNumberEdges());
        OSPFWeights weightsOSPF = new OSPFWeights(nodes);
        weightsOSPF.setWeights(weights,topology);

        SRSimulator simulator = new SRSimulator(topology,weightsOSPF);
        int i = 0;
        for(i = 0; i < numberOfRequests ; i++)
        {
            Request r = requests.get(i);
            for(Flow f : r.getFlow())
                simulator.addFlow(f);
        }
        result[0] = simulator.getCongestionValue();
        result[1] = simulator.getMLU();

        return result;
    }

    private static int[] random(int numberEdges)
    {
        int[] result = new int[numberEdges];
        Random rand = new Random();

        for(int i = 0; i < numberEdges; i++)
        {
            result[i] = 1 + rand.nextInt(20);
        }

        return result;
    }
}
