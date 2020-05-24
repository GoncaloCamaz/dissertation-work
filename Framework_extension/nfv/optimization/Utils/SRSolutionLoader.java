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
import java.util.List;

public class SRSolutionLoader
{
    /** Debug mode **
    public static void main(String[] args) throws IOException, ParseException {
        List<Request> res = loadResultsFromJson("Configuration_30.json");
        System.out.println("loaded");
    }
    */

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
            for(Integer i : nodesIDByOrder)
            {
                Segment s = new Segment(i.toString(), Segment.SegmentType.NODE);
                s.setDstNodeId(i);
                segments.add(s);
            }
            path.setLabels(segments);
            Flow flow = new Flow(Integer.parseInt(origin), Integer.parseInt(destination), Flow.FlowType.NFV,false,Double.parseDouble(bandwidth));
            Request request = new Request(id,flow,path);
            ret.add(request);
        }
        return ret;
    }
}
