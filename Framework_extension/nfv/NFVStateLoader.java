package pt.uminho.algoritmi.netopt.nfv;

import com.opencsv.CSVReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class NFVStateLoader
{
    public static JSONObject loadObject(String filename) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(filename));
        JSONObject jsonObj = (JSONObject) obj;

        return jsonObj;
    }

    public static NFNodesMap loadNodes(String filename) throws IOException, ParseException {
        NFNodesMap nodesMap = new NFNodesMap();
        Map<Integer, NFNode> nodes = new HashMap<>();
        JSONObject jsonObj = loadObject(filename);
        JSONArray nodesArray = (JSONArray) jsonObj.get("nodes");

        for(Object o : nodesArray)
        {
            JSONObject ob = (JSONObject) o;
            int nID = Integer.parseInt((String) ob.get("id"));
            double capacity = Double.parseDouble((String) ob.get("capacity"));
            JSONArray array = (JSONArray) ob.get("availableServices");
            List<Integer> aux = new ArrayList<>();
            for(Object elm : array)
            {
                int id = Integer.parseInt(elm.toString());
                aux.add(id);
            }
            Collections.sort(aux);
            NFNode node = new NFNode(nID,capacity,aux);
            nodes.put(nID, node);
        }
        nodesMap.setNodes(nodes);

        return nodesMap;
    }

    public static NFServicesMap loadServices(String filename) throws IOException, ParseException {
        NFServicesMap servicesMap = new NFServicesMap();
        Map<Integer, NFService> services = new HashMap<>();

        JSONObject jsonObj = loadObject(filename);
        JSONArray nodesArray = (JSONArray) jsonObj.get("services");

        for(Object o : nodesArray)
        {
            JSONObject ob = (JSONObject) o;
            int sID = Integer.parseInt((String) ob.get("id"));
            String name = (String) ob.get("name");
            double cost = Double.parseDouble((String) ob.get("cost"));
            NFService sv = new NFService(sID,name,cost);
            services.put(sID, sv);
        }
        servicesMap.setServices(services);

        return servicesMap;
    }

    public static NFRequestsMap loadRequests(String filename) throws IOException
    {
        NFRequestsMap requestsMap = new NFRequestsMap();
        Map<Integer, NFRequest> requests = new HashMap<>();
        Reader input = Files.newBufferedReader(Paths.get(filename));
        CSVReader reader = new CSVReader(input, ';');
        String[] headers = reader.readNext();
        List<String[]> rows = reader.readAll();
        for(String[] r : rows)
        {
            String[] row = r;
            List<Integer> req = new ArrayList<>();
            int requestID = Integer.parseInt(row[0]);
            int from = Integer.parseInt(row[1]);
            int to = Integer.parseInt(row[2]);
            double bandwidth = Double.valueOf(row[3]);
            int size = row.length;
            int i = 4;
            while(i < size)
            {
                int rq = Integer.parseInt(row[i]);
                if(rq != -1)
                {
                    req.add(rq);
                }
                i++;
            }
            NFRequest request = new NFRequest(requestID, from, to, bandwidth, req);
            requests.put(requestID, request);
        }
        requestsMap.setRequestList(requests);
        return requestsMap;
    }

    public static NFRequestsMap loadRequestsSorted(String filename) throws IOException
    {
        NFRequestsMap requestsMap = new NFRequestsMap();
        Map<Integer, NFRequest> requests = new HashMap<>();
        Reader input = Files.newBufferedReader(Paths.get(filename));
        CSVReader reader = new CSVReader(input, ';');
        String[] headers = reader.readNext();
        List<String[]> rows = reader.readAll();
        for(String[] r : rows)
        {
            String[] row = r;
            List<Integer> req = new ArrayList<>();
            int requestID = Integer.parseInt(row[0]);
            int from = Integer.parseInt(row[1]);
            int to = Integer.parseInt(row[2]);
            double bandwidth = Double.valueOf(row[3]);
            int size = row.length;
            int i = 4;
            while(i < size)
            {
                int rq = Integer.parseInt(row[i]);
                if(rq != -1)
                {
                    req.add(rq);
                }
                i++;
            }
            Collections.sort(req);
            NFRequest request = new NFRequest(requestID, from, to, bandwidth, req);
            requests.put(requestID, request);
        }
        requestsMap.setRequestList(requests);
        return requestsMap;
    }
}
