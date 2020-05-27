package pt.uminho.algoritmi.netopt.nfv.optimization.Utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pt.uminho.algoritmi.netopt.cplex.utils.SourceDestinationPair;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class WeightsSolutionSaver
{
    public static void save(Population p, NetworkTopology topology) throws DimensionErrorException {
        IntegerSolution sol = p.getLowestValuedSolutions(0, 1).get(0);
        double fitness = sol.getFitnessValue(0);
        HashMap<SourceDestinationPair, Double> mapWeights = new HashMap<>();
        int result[] = sol.getVariablesArray();
        OSPFWeights weights = new OSPFWeights(result.length);
        weights.setWeights(result, topology);

        double[][] res = weights.getWeights();

        for(int i = 0; i < topology.getDimension(); i++)
        {
            for(int j = 0; j < topology.getDimension(); j++)
            {
                if(res[i][j] > 0)
                    mapWeights.put(new SourceDestinationPair(i,j),res[i][j]);
            }
        }

        savetoJSon(mapWeights, fitness);
    }

    private static void savetoJSon(HashMap<SourceDestinationPair, Double> mapWeights, double fitness)
    {
        String fileName = "Weights_" + mapWeights.size() + "_" + System.currentTimeMillis() + ".json";
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        for(SourceDestinationPair pair : mapWeights.keySet())
        {
            JSONObject objAux = new JSONObject();
            objAux.put("Origin", pair.getSource());
            objAux.put("Destination", pair.getDestination());
            objAux.put("Weight", mapWeights.get(pair));
            array.add(objAux);
        }
        obj.put("Weights", array);
        obj.put("Congestion", fitness);
        try {
            save(obj, fileName);
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
