package pt.uminho.algoritmi.netopt.nfv.optimization.SolutionSaver;

import ilog.concert.IloException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pt.uminho.algoritmi.netopt.cplex.MCFPhiNodeUtilizationSolver;
import pt.uminho.algoritmi.netopt.cplex.utils.SourceDestinationPair;
import pt.uminho.algoritmi.netopt.nfv.NFNodesMap;
import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.optimization.NFVRequestConfiguration;
import pt.uminho.algoritmi.netopt.nfv.optimization.NFVRequestsConfigurationMap;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.SolutionParser;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SolutionSaver
{
    public static void saveServicesLocationConfiguration(int[] solution, String filename, NetworkTopology topology, NFVState state)
    {
        SolutionParser parser = new SolutionParser(filename);
        NFNodesMap nodesMap = parser.solutionParser(solution);
        MCFPhiNodeUtilizationSolver solver = new MCFPhiNodeUtilizationSolver(topology,state.getServices(),state.getRequests(),nodesMap);
        solver.setSaveConfigurations(true);
        try
        {
            OptimizationResultObject obj = solver.optimize();
            NFVRequestsConfigurationMap configurationMap = obj.getNfvRequestsConfigurationMap();
            saveToJSON(configurationMap, solution.length);

        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private static void saveToJSON(NFVRequestsConfigurationMap configurationMap, int length)
    {
        String filename = "Configuration_"+ length + ".json";
        JSONObject obj = new JSONObject();
        Map<Integer, NFVRequestConfiguration> configurations = configurationMap.getConfigurations();
        JSONArray configurationsArray = new JSONArray();
        for(NFVRequestConfiguration configuration : configurations.values())
        {
            int requestID = configuration.getRequestID();
            JSONObject configurationObject = new JSONObject();
            configurationObject.put("RequestID", requestID);
            JSONArray serviceProcessmentLocation = new JSONArray();
            Map<Integer, Integer> services = configuration.getServiceProcessment();
            for(Integer i : services.keySet())
            {
                JSONObject objA = new JSONObject();
                objA.put(i, services.get(i));
                serviceProcessmentLocation.add(objA);
            }
            configurationObject.put("ServiceProcessmentLocation",serviceProcessmentLocation);

            List<SourceDestinationPair> list = configuration.getSrpath();
            JSONArray srpath = new JSONArray();
            for(SourceDestinationPair pair : list)
            {
                JSONObject objB = new JSONObject();
                objB.put("Origin",pair.getSource());
                objB.put("Destination", pair.getDestination());
                srpath.add(objB);
            }
            configurationObject.put("SRPath", srpath);
            configurationsArray.add(configurationObject);
        }
        obj.put("Configurations",configurationsArray);
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
