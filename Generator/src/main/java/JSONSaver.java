import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JSONSaver
{
    public void saveConfigurations(ConfigGen configGen)
    {
        String fileName = "./config.json";
        List<String> headers = configGen.getHeaders();
        List<Integer> nodesWServices= configGen.getNodesWServices();
        List<Integer> servicesID = configGen.getServicesID();
        JSONArray arrayHeaders = new JSONArray();
        JSONArray arrayNodes = new JSONArray();
        JSONArray arrayServicesID = new JSONArray();

        JSONObject obj = new JSONObject();
        obj.put("filepath", configGen.getFilepath());
        obj.put("allNodesWServices", configGen.isAllNodesWServices());
        obj.put("numberOfNodesWServices", configGen.getNumberOfNodesWServices());
        obj.put("nodesWAllServices", configGen.isNodesWAllServices());
        obj.put("numberOfNodes", configGen.getNumberOfNodes());
        obj.put("reqsToGen", configGen.getReqsToGen());
        for(String s : headers)
        {
            arrayHeaders.add(s);
        }
        obj.put("headers", arrayHeaders);
        for(Integer i : nodesWServices)
        {
            arrayNodes.add(i.toString());
        }
        obj.put("nodesWServices", arrayNodes);
        for(Integer i : servicesID)
        {
            arrayServicesID.add(i.toString());
        }
        obj.put("ServicesID", arrayServicesID);

        try {
            save(obj, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveFrameworkConfigurations(ConfigGen configGen, RandomNumGen random)
    {
        String fileName = "frameworkConfiguration.json";
        int nodesWServices = configGen.getNumberOfNodesWServices();
        int maxNodes = configGen.getNumberOfNodes();

        JSONArray servicesArray = new JSONArray();

        JSONObject obj = new JSONObject();
        obj.put("nodesWServices", nodesWServices);
        obj.put("maxNodes", maxNodes);
        List<Integer> servicesID = new ArrayList<>();
        for(Integer id : configGen.getServicesID())
        {
            JSONObject service = new JSONObject();
            service.put("id",id.toString());
            service.put("name", "service " + id);
            service.put("cost",""+ random.getRandomFromRage(30,50));
            servicesID.add(id);
            servicesArray.add(service);
        }
        obj.put("services", servicesArray);

        JSONArray nodes = genNodes(configGen, random, servicesID);
        obj.put("nodes", nodes);

        try {
            save(obj, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Insert nodes section to json
    private JSONArray genNodes(ConfigGen configGen, RandomNumGen random, List<Integer> servicesID) {
        JSONArray nodes = new JSONArray();
        int maxNodes = configGen.getNumberOfNodes();
        int maxNodesWServices = configGen.getNumberOfNodesWServices();
        List<Integer> nodesWServices = configGen.getNodesWServices();
        for(int i = 0; i<maxNodes; i++)
        {
            JSONObject nodesObj = new JSONObject();
            nodesObj.put("id", i + "");
            JSONArray serv = new JSONArray();
            if(!configGen.isNodesWAllServices())
            {
                int numberOfServices = random.getRandomFromRage(1,servicesID.size());

                if(nodesWServices.contains(i))
                {
                    Collections.shuffle(servicesID);
                    for(int k = 0; k < numberOfServices; k++)
                    {
                        int s = servicesID.get(k);
                        serv.add("" + s);
                    }
                }
            }
            else
            {
                if(i < maxNodesWServices)
                {
                    for(int k = 0; k < servicesID.size(); k++)
                    {
                        int s = servicesID.get(k);
                        serv.add("" + s);
                    }
                }
            }
            serv.sort(String.CASE_INSENSITIVE_ORDER);
            nodesObj.put("availableServices", serv);
            if(serv.size() > 0)
            {
                nodesObj.put("capacity", "" + 1000);
            }
            else
            {
                nodesObj.put("capacity", "" + 0);
            }
            nodes.add(nodesObj);
        }

        return nodes;
    }

    private void save(JSONObject obj, String filename) throws IOException {
        FileWriter file = new FileWriter(filename);
        file.write(obj.toJSONString());
        file.flush();
        file.close();
    }
}
