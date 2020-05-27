package pt.uminho.algoritmi.netopt.nfv.optimization.Utils;

import ilog.concert.IloException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pt.uminho.algoritmi.netopt.cplex.NFV_MCFPMLUSolver;
import pt.uminho.algoritmi.netopt.cplex.NFV_MCFPhiNodeUtilizationSolver2;
import pt.uminho.algoritmi.netopt.cplex.utils.Arc;
import pt.uminho.algoritmi.netopt.cplex.utils.Arcs;
import pt.uminho.algoritmi.netopt.cplex.utils.SourceDestinationPair;
import pt.uminho.algoritmi.netopt.nfv.*;
import pt.uminho.algoritmi.netopt.nfv.optimization.NFVRequestConfiguration;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.nfv.optimization.ParamsNFV;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ConfigurationSolutionSaver
{
    public static void saveServicesLocationConfiguration(int[] solution, String filename, NetworkTopology topology, NFVState state, ParamsNFV.EvaluationAlgorithm algorithm)
    {
        EASolutionParser parser = new EASolutionParser(filename);
        NFNodesMap nodesMap = parser.solutionParser(solution);
        OptimizationResultObject obj = solve(topology, state.getServices(),state.getRequests(), nodesMap, algorithm);

        Arcs arcs = new Arcs();
        int nodesNumber = state.getNodes().getNodes().size();
        double[][] capacity = topology.getNetGraph().createGraph().getCapacitie();

        for (int i = 0; i < nodesNumber; i++)
        {
            for (int j = 0; j < nodesNumber; j++)
            {
                if (capacity[i][j] > 0)
                {
                    Arc a = new Arc(i, j, capacity[i][j]);
                    arcs.add(a);
                }
            }
        }

        saveToJSON(obj,arcs,solution.length, nodesMap);
    }

    private static OptimizationResultObject solve(NetworkTopology topology, NFServicesMap services, NFRequestsMap requests, NFNodesMap nodesMap, ParamsNFV.EvaluationAlgorithm algorithm){
        OptimizationResultObject ret = new OptimizationResultObject(nodesMap.getNodes().size());
        if(algorithm.equals(ParamsNFV.EvaluationAlgorithm.PHI))
        {
            NFV_MCFPhiNodeUtilizationSolver2 solver = new NFV_MCFPhiNodeUtilizationSolver2(topology,services,requests,nodesMap);
            solver.setSaveConfigurations(true);
            ret = solver.optimize();
        }
        else
        {
            NFV_MCFPMLUSolver solver = new NFV_MCFPMLUSolver(topology,services,requests,nodesMap);
            solver.setSaveConfigurations(true);
            ret = solver.optimize();
        }

        return ret;
    }

    private static void saveToJSON(OptimizationResultObject o, Arcs arcs, int length, NFNodesMap map)
    {
        String algorithm = "mlu";
        if(o.getMlu() == 0)
        {
            algorithm = "phi";
        }
        String filename = "Configuration_"+ algorithm + "_" +length + ".json";
        Map<Integer, NFNode> nodesMap= map.getNodes();
        JSONObject obj = new JSONObject();
        Map<Integer, NFVRequestConfiguration> configurations = o.getNfvRequestsConfigurationMap().getConfigurations();
        JSONArray configurationsArray = new JSONArray();
        for(NFVRequestConfiguration configuration : configurations.values())
        {
            int requestID = configuration.getRequestID();
            JSONObject configurationObject = new JSONObject();
            configurationObject.put("RequestID", requestID);
            configurationObject.put("Request Origin", configuration.getRequestOrigin());
            configurationObject.put("Request Destination", configuration.getRequestDestination());
            configurationObject.put("Request Bandwidth", configuration.getBandwidth());
            JSONArray serviceProcessmentLocation = new JSONArray();
            Map<Integer, Integer> services = configuration.getServiceProcessment();
            for(Integer i : services.keySet())
            {
                JSONObject objA = new JSONObject();
                objA.put("Service ID", i);
                objA.put("Node ID",services.get(i));
                serviceProcessmentLocation.add(objA);
            }
            configurationObject.put("ServiceProcessmentLocation",serviceProcessmentLocation);
            List<Integer> path = configuration.genSRPath();
            JSONArray finalPath = new JSONArray();
            for(Integer i : path)
            {
                finalPath.add(i);
            }
            configurationObject.put("NodeIDPath", finalPath);
            List<SourceDestinationPair> list = configuration.getSrpath();
            JSONArray srpath = new JSONArray();
            for(SourceDestinationPair pair : list)
            {
                JSONObject objB = new JSONObject();
                objB.put("Origin",pair.getSource());
                objB.put("Destination", pair.getDestination());
                srpath.add(objB);
            }
            configurationObject.put("SegmentPath", srpath);
            configurationsArray.add(configurationObject);
        }

        JSONArray loads = new JSONArray();
        for(Arc arc : arcs)
        {
            int i = arc.getFromNode();
            int j = arc.getToNode();
            JSONObject objLoad = new JSONObject();
            objLoad.put("Origin", i);
            objLoad.put("Destination",j);
            objLoad.put("Load", o.getLoad(i,j));
            loads.add(objLoad);
        }
        obj.put("Arc Loads", loads);


        obj.put("Configurations",configurationsArray);
        if(o.getPhiValue() != 0)
        {
            obj.put("phi", o.getPhiValue());
            obj.put("gamma",o.getGammaValue());
        }
        else
        {
            obj.put("mlu", o.getMlu());
            obj.put("mnu",o.getMnu());
        }
        obj.put("objectiveFunction", o.getLoadValue());

        JSONArray array = new JSONArray();
        for(NFNode node : nodesMap.values())
        {
            List<Integer> servicesAvailable = node.getAvailableServices();
            JSONArray arrayServices = new JSONArray();
            JSONObject objAux = new JSONObject();
            for(Integer i : servicesAvailable)
            {
                arrayServices.add(i);
            }
            objAux.put("nodeID",node.getId());
            objAux.put("AvailableServices",arrayServices);
            array.add(objAux);
        }

        JSONArray loadsNodes = new JSONArray();
        double[] nodesUtilization = o.getNodeUtilization();
        for(int i = 0; i < nodesMap.size(); i++)
        {
            JSONObject objAux = new JSONObject();
            objAux.put("NodeID", i);
            objAux.put("Load", nodesUtilization[i]);
            loadsNodes.add(objAux);
        }
        obj.put("NodesLoad", loadsNodes);

        obj.put("servicesLocationSolution", array);

        try {
            save(obj,filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void save(JSONObject obj, String filename) throws IOException {
        FileWriter file = new FileWriter(filename);
        file.write(obj.toJSONString());
        file.flush();
        file.close();
    }
}
