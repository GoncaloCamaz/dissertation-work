import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class JSONSaver
{

    public void saveConfigurations(ConfigGen configGen)
    {
        String fileName = "./config.json";
        String filepath = configGen.getFilepath();
        boolean allNodesNFV = configGen.isAllNodesWServices();
        int nodesWServices = configGen.getMaxNodesWServices();
        int maxNodes = configGen.getMaxNodes();
        int requests = configGen.getRequestnumber();
        List<String> headers = configGen.getHeaders();
        JSONArray array = new JSONArray();

        JSONObject obj = new JSONObject();
        obj.put("filepath", filepath);
        obj.put("allNodesNFV", allNodesNFV);
        obj.put("nodesWServices", nodesWServices);
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

    private void save(JSONObject obj, String filename) throws IOException {
        FileWriter file = new FileWriter(filename);
        file.write(obj.toJSONString());
        file.flush();
        file.close();
    }
}
