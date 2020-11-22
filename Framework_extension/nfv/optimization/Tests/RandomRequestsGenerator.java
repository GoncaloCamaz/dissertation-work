package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;

import pt.uminho.algoritmi.netopt.nfv.*;
import pt.uminho.algoritmi.netopt.nfv.optimization.NFVRequestConfiguration;
import pt.uminho.algoritmi.netopt.nfv.optimization.NFVRequestsConfigurationMap;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.ConfigurationSolutionSaver;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import static pt.uminho.algoritmi.netopt.ospf.utils.io.GraphReader.readGML;


public class RandomRequestsGenerator
{
    private static String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.nodes";// args[0];
    private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.edges";//args[1];
    private static String servicesFile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\NewAnalysisConfigurations\\30\\SOEA\\Analise\\PHI_300Config.json";
    private static String requests = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\pedidos300.csv";// args[3];
    private static String topoFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/BT Europe/BtEurope.gml";


    public static void main(String[] args) throws Exception {
        NetworkTopology topology = new NetworkTopology(nodesFile,edgesFile);

        InputStream inputStream = new FileInputStream(topoFile);
        NetGraph netgraph = readGML(inputStream);

       // NetworkTopology topology = new NetworkTopology(netgraph);
        NFVState state = new NFVState(servicesFile, requests);

        NFVRequestsConfigurationMap result = genRandomSRPaths(state);
        OptimizationResultObject object = new OptimizationResultObject(topology.getDimension());
        object.setNfvRequestsConfigurationMap(result);
        ConfigurationSolutionSaver.saveRandomSolutionToJSON(object, state.getNodes(),"C:\\Users\\gcama\\Desktop\\Dissertacao\\NewAnalysisConfigurations\\30\\SOEA\\Analise\\RR_PHI_300");
    }

    private static NFVRequestsConfigurationMap genRandomSRPaths(NFVState state)
    {
        NFVRequestsConfigurationMap result = new NFVRequestsConfigurationMap();

        NFRequestsMap requests = state.getRequests();
        for(NFRequest request : requests.getRequestMap().values())
        {
            NFVRequestConfiguration configuration = new NFVRequestConfiguration();
            configuration.setRequestID(request.getId());
            configuration.setRequestOrigin(request.getSource());
            configuration.setRequestDestination(request.getDestination());
            configuration.setBandwidth(request.getBandwidth());
            configuration.setServiceOrder(request.getServiceList());
            Map<Integer,Integer> serviceProcessment = genServiceProcessmentLocation(state.getNodes(), request.getServiceList());
            configuration.setServiceProcessment(serviceProcessment);

            result.addConfiguration(request.getId(), configuration);
        }

        return result;
    }

    private static Map<Integer, Integer> genServiceProcessmentLocation(NFNodesMap nodes, List<Integer> serviceList)
    {
        Map<Integer, List<Integer>> possibleProcessmentNodes = new HashMap<>();
        Map<Integer, Integer> result = new HashMap<>();
        int i = 0;
        Random rand = new Random();

        while(i < serviceList.size())
        {
            List<Integer> emptyList = new ArrayList<>();
            possibleProcessmentNodes.put(serviceList.get(i),emptyList);
            i++;
        }

        for(Integer s : serviceList)
        {
            for(NFNode node : nodes.getNodes().values())
            {
                if(node.getAvailableServices().contains(s))
                {
                    possibleProcessmentNodes.get(s).add(node.getId());
                }
            }
        }

        for(Integer service : serviceList)
        {
            int random = rand.nextInt(possibleProcessmentNodes.get(service).size()-1);
            result.put(service,possibleProcessmentNodes.get(service).get(random));
        }

        return result;
    }
}
