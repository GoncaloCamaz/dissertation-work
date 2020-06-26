package pt.uminho.algoritmi.netopt.nfv.optimization.Utils;

import com.opencsv.CSVWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pt.uminho.algoritmi.netopt.cplex.NFV_MCFPMLUSolver;
import pt.uminho.algoritmi.netopt.cplex.NFV_MCFPhiNodeUtilizationSolver;
import pt.uminho.algoritmi.netopt.cplex.utils.Arc;
import pt.uminho.algoritmi.netopt.cplex.utils.Arcs;
import pt.uminho.algoritmi.netopt.cplex.utils.SourceDestinationPair;
import pt.uminho.algoritmi.netopt.nfv.*;
import pt.uminho.algoritmi.netopt.nfv.optimization.NFVRequestConfiguration;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.nfv.optimization.ParamsNFV;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ConfigurationSolutionSaver
{
    /**
     * Gets the best solution and orchestrates the algorithm to run the cplex model again for that configuration
     * @param solution
     * @param filename
     * @param topology
     * @param state
     * @param algorithm
     * @return
     */
    public static String saveServicesLocationConfiguration(int[] solution, String filename, NetworkTopology topology, NFVState state, ParamsNFV.EvaluationAlgorithm algorithm)
    {
        EASolutionParser parser = new EASolutionParser(filename);
        NFNodesMap nodesMap = parser.solutionParser(solution);
        OptimizationResultObject obj = solve(topology, state.getServices(),state.getRequests(), nodesMap, algorithm);

        String savingName = nodesMap.getNodes().size() + "_" +System.currentTimeMillis();

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
        saveToCSV(obj, arcs, nodesMap, savingName);
        saveToJSON(obj,arcs, nodesMap, savingName);

        return savingName;
    }

    /**
     * Transforms an OptimizationResultObject into List[] Strings to save on csv file
     * @param obj
     * @param arcs
     * @param map
     * @param filename
     */
    public static void saveToCSV(OptimizationResultObject obj, Arcs arcs, NFNodesMap map, String filename)
    {
        int arcsSize = arcs.getNumberOfArcs();
        int nodesSize = obj.getNumberOfNodes();

        filename = filename+".csv";
        String[] headersArcs = new String[3];
        headersArcs[0] = "from";
        headersArcs[1] = "to";
        headersArcs[2] = "load";

        String[] headersNodes = new String[2];
        headersNodes[0] = "nodeID";
        headersNodes[1] = "load";

        List<String[]> rowsArcs = new ArrayList<>();
        for(int i = 0; i < arcsSize; i++)
        {
            String[] row = new String[3];
            Arc a = arcs.getArc(i);
            double load = obj.getLoad(a.getFromNode(), a.getToNode());
            double capacity = a.getCapacity();
            double percentage = (load/capacity);
            percentage = Math.floor(percentage*100) / 100;
            row[0] = String.valueOf(a.getFromNode());
            row[1] = String.valueOf(a.getToNode());
            row[2] = String.valueOf(percentage);
            rowsArcs.add(row);
        }
        List<String[]> rowsNodes = new ArrayList<>();
        for(int i = 0; i < nodesSize; i++)
        {
            String[] rowNode = new String[2];
            NFNode node = map.getNodes().get(i);
            double load = obj.getNodeLoad(i);
            double capacity = node.getProcessCapacity();
            double percentage = (load/capacity);
            percentage = Math.floor(percentage*100) / 100;
            rowNode[0] = String.valueOf(i);
            rowNode[1] = String.valueOf(percentage);
            rowsNodes.add(rowNode);
        }

        generateSolutions(headersArcs,rowsArcs,headersNodes,rowsNodes,filename);
    }

    /**
     * Saves the load on arcs and nodes to csv files
     * @param headersA
     * @param rowsA
     * @param headersN
     * @param rowsN
     * @param filename
     */
    private static void generateSolutions(String[] headersA, List<String[]> rowsA,String[] headersN, List<String[]> rowsN, String filename) {

        File fileArcs = new File("Arcs"+filename);
        File fileNodes = new File("Nodes"+filename);
        try
        {
            FileWriter outputfileA = new FileWriter(fileArcs);
            FileWriter outputfileN = new FileWriter(fileNodes);
            CSVWriter writerA = new CSVWriter(outputfileA, ';',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            CSVWriter writerN = new CSVWriter(outputfileN, ';',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            List<String[]> dataArcs = new ArrayList<>();
            dataArcs.add(headersA);
            for(String[] row : rowsA)
            {
                dataArcs.add(row);
            }
            writerA.writeAll(dataArcs);
            writerA.close();

            List<String[]> dataNodes = new ArrayList<>();
            dataNodes.add(headersN);
            for(String[] row : rowsN)
            {
                dataNodes.add(row);
            }
            writerN.writeAll(dataNodes);
            writerN.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to run the Cplex Model on the final iteration of the EA
     * @param topology
     * @param services
     * @param requests
     * @param nodesMap
     * @param algorithm
     * @return
     */
    private static OptimizationResultObject solve(NetworkTopology topology, NFServicesMap services, NFRequestsMap requests, NFNodesMap nodesMap, ParamsNFV.EvaluationAlgorithm algorithm){
        OptimizationResultObject ret = new OptimizationResultObject(nodesMap.getNodes().size());
        if(algorithm.equals(ParamsNFV.EvaluationAlgorithm.PHI))
        {
            NFV_MCFPhiNodeUtilizationSolver solver = new NFV_MCFPhiNodeUtilizationSolver(topology,services,requests,nodesMap);
            solver.setCplexTimeLimit(3600);
            solver.setSaveConfigurations(true);
            ret = solver.optimize();
        }
        else
        {
            NFV_MCFPMLUSolver solver = new NFV_MCFPMLUSolver(topology,services,requests,nodesMap);
            solver.setCplexTimeLimit(3600);
            solver.setSaveConfigurations(true);
            ret = solver.optimize();
        }

        return ret;
    }

    /**
     * Converts the OptimizationResultObject to an JSONObject
     * @param o
     * @param arcs
     * @param map
     * @param filename
     */
    private static void saveToJSON(OptimizationResultObject o, Arcs arcs, NFNodesMap map, String filename)
    {
        String algorithm = "mlu";
        if(o.getMlu() == 0)
        {
            algorithm = "phi";
        }
        filename = algorithm + filename + ".json";
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

    /**
     * Save an JSONObject to JSON file
     * @param obj
     * @param filename
     * @throws IOException
     */
    private static void save(JSONObject obj, String filename) throws IOException {
        FileWriter file = new FileWriter(filename);
        file.write(obj.toJSONString());
        file.flush();
        file.close();
    }

    /**
     * Saves Pareto matrix solution
     * @param res
     * @param size
     * @param filename
     */
    public static void saveParetoToCSV(double[][] res, int size, String filename)
    {
        File pareto = new File("Pareto"+filename);
        try
        {
            FileWriter outputfile = new FileWriter(pareto);
            CSVWriter writer = new CSVWriter(outputfile, ';',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            List<String[]> data = new ArrayList<>();
            for(int i = 0; i < size; i++)
            {
                String[] row = new String[size];
                for(int j = 0; j < size; j++)
                {
                    row[j] = String.valueOf(res[i][j]);
                }
                data.add(row);
            }
            writer.writeAll(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
