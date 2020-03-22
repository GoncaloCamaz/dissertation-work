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
        String filepath = configGen.getFilepath();
        boolean allNodesNFV = configGen.isAllNodesWServices();
        boolean allservicesAllNodes = configGen.isNodesWAllServices();
        int nodesWServices = configGen.getMaxNodesWServices();
        int maxNodes = configGen.getMaxNodes();
        int requests = configGen.getRequestnumber();
        List<String> headers = configGen.getHeaders();
        JSONArray array = new JSONArray();

        JSONObject obj = new JSONObject();
        obj.put("filepath", filepath);
        obj.put("allNodesNFV", allNodesNFV);
        obj.put("nodesWServices", nodesWServices);
        obj.put("nodesWAllServices", allservicesAllNodes);
        obj.put("maxNodes", maxNodes);
        obj.put("solutionsToGen", requests);
        for(String s : headers)
        {
            array.add(s);
        }
        obj.put("headers", array);

        try {
            save(obj, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveFrameworkConfigurations(ConfigGen configGen, RandomNumGen random)
    {
        String fileName = "frameworkConfiguration.json";
        int nodesWServices = configGen.getMaxNodesWServices();
        int maxNodes = configGen.getMaxNodes();
        List<String> headers = configGen.getHeaders().subList(4,configGen.getHeaders().size());
        JSONArray servicesArray = new JSONArray();

        JSONObject obj = new JSONObject();
        obj.put("nodesWServices", nodesWServices);
        obj.put("maxNodes", maxNodes);
        List<Integer> servicesID = new ArrayList<>();
        int sID = 1;
        for(String s : headers)
        {
            JSONObject service = new JSONObject();
            service.put("id", s);
            service.put("name", "service " + sID );
            service.put("cost",""+ random.getRandomFromRage(30,50));
            sID++;
            servicesID.add(Integer.parseInt(s));
            servicesArray.add(service);
        }
        obj.put("services", servicesArray);

        JSONArray nodes = getJsonArray(configGen, random, servicesID);
        obj.put("nodes", nodes);

        try {
            save(obj, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Insert nodes section to json
    private JSONArray getJsonArray(ConfigGen configGen, RandomNumGen random, List<Integer> servicesID) {
        JSONArray nodes = new JSONArray();
        int maxNodes = configGen.getMaxNodes();
        int maxNodesWServices = configGen.getMaxNodesWServices();
        for(int i = 1; i<=maxNodes; i++)
        {
            JSONObject nodesObj = new JSONObject();
            nodesObj.put("id", i + "");
            nodesObj.put("capacity", "" + 1000);
            JSONArray serv = new JSONArray();
            if(!configGen.isNodesWAllServices())
            {
                int numberOfServices = random.getRandomFromRage(1,servicesID.size());

                if(i - 1 < maxNodesWServices)
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
                if(i - 1 < maxNodesWServices)
                {
                    for(int k = 0; k < servicesID.size(); k++)
                    {
                        int s = servicesID.get(k);
                        serv.add("" + s);
                    }
                }
            }
            nodesObj.put("availableServices", serv);
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
