package pt.uminho.algoritmi.netopt.nfv.optimization.Utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Flow;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.LabelPath;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Segment;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SRSolutionLoader
{
    /**
     * Loads the json file into an object
     * @param filename
     * @return
     * @throws IOException
     * @throws ParseException
     */
    private static JSONObject loadObject(String filename) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(filename));
        JSONObject jsonObj = (JSONObject) obj;

        return jsonObj;
    }

    /**
     * Loads content from jsonfile to a list of Request
     * @param filename
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public static ArrayList<Request> loadResultsFromJson(String filename) throws IOException, ParseException {
        JSONObject obj = loadObject(filename);
        JSONArray array = (JSONArray) obj.get("Configurations");
        ArrayList<Request> ret = new ArrayList<>();
        for(Object o : array)
        {
            JSONObject objArray = (JSONObject) o;
            String id = objArray.get("RequestID").toString();
            String origin =objArray.get("Request Origin").toString();
            String destination = objArray.get("Request Destination").toString();
            NetNode source = new NetNode();
            NetNode dest = new NetNode();
            source.setNodeId(Integer.parseInt(origin));
            dest.setNodeId(Integer.parseInt(destination));

            String bandwidth = objArray.get("Request Bandwidth").toString();
            JSONArray arraySegments = (JSONArray) objArray.get("NodeIDPath");
            List<Integer> nodesIDByOrder = new ArrayList<>();
            for(Object oSegments : arraySegments)
            {
                nodesIDByOrder.add(Integer.parseInt(String.valueOf(oSegments)));
            }

            LabelPath path = new LabelPath(source,dest);
            ArrayList<Segment> segments = new ArrayList<>();
            int old = -1;
            for(Integer i : nodesIDByOrder)
            {
                if(i != Integer.parseInt(origin) && i != Integer.parseInt(destination))
                {
                    if(i != old)
                    {
                        Segment s = new Segment(i.toString(), Segment.SegmentType.NODE);
                        s.setDstNodeId(i);
                        if(old != -1)
                            s.setSrcNodeId(old);
                        else
                            s.setSrcNodeId(Integer.parseInt(origin));
                        segments.add(s);
                        old = i;
                    }
                }
            }
            path.setLabels(segments);
            Flow flow = new Flow(Integer.parseInt(id),Integer.parseInt(origin), Integer.parseInt(destination), Flow.FlowType.NFV,false,Double.parseDouble(bandwidth));
            Request request = new Request(Integer.parseInt(id),flow,path);
            ret.add(request);
        }
        return ret;
    }

    public static double loadCongestionval(String filename, String mode) throws IOException, ParseException {
        JSONObject obj = loadObject(filename);
        JSONObject value = (JSONObject) obj.get(mode);
        return Double.parseDouble( value.toJSONString());
    }

    public static Map<Integer, Map<Integer, Integer>> loadNumberOfExecutionsPerNode(String filename) throws IOException, ParseException
    {
        Map<Integer, Map<Integer, Integer>> result = new HashMap<>();

        JSONObject obj = loadObject(filename);
        JSONArray array = (JSONArray) obj.get("Configurations");
        for(Object o : array)
        {
            JSONObject objArray = (JSONObject) o;
            JSONArray arrayAux = (JSONArray) objArray.get("ServiceProcessmentLocation");
            for(Object oAux : arrayAux)
            {
                JSONObject aux = (JSONObject) oAux;
                int serviceID = Integer.parseInt(String.valueOf(aux.get("Service ID")));
                int processment = Integer.parseInt(String.valueOf(aux.get("Node ID")));
                if(result.containsKey(processment))
                {
                    if(result.get(processment).containsKey(serviceID))
                    {
                        int old = result.get(processment).get(serviceID);
                        old += 1;
                        result.get(processment).replace(serviceID, old-1, old);
                    }
                    else
                    {
                        result.get(processment).put(serviceID,1);
                    }
                }
                else
                {
                    Map<Integer, Integer> toAdd = new HashMap<>();
                    toAdd.put(serviceID, 1);
                    result.put(processment,toAdd);
                }
            }
        }

        return result;
    }
}
