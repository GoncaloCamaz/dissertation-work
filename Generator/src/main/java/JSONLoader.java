import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class JSONLoader {

    public ConfigGen getConfiguration()
    {
        ConfigGen c = new ConfigGen();
        try {
            c = loadConfigurations();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return c;
    }

    public ConfigGen loadConfigurations() throws IOException, ParseException {
        ConfigGen c = new ConfigGen();
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader("config.json"));
        JSONObject jsonObj = (JSONObject) obj;
        String filepath = (String) jsonObj.get("filepath");
        boolean allnodesNFV = (boolean) jsonObj.get("allNodesNFV");
        boolean nodesWALLNFV = (boolean) jsonObj.get("nodesWAllServices");
        int nodesNFV = ((Long) jsonObj.get("nodesWServices")).intValue();
        int maxNodes = ((Long) jsonObj.get("maxNodes")).intValue();
        int requestNumber = ((Long) jsonObj.get("solutionsToGen")).intValue();
        JSONArray headers = (JSONArray) jsonObj.get("headers");
        ArrayList<String> headersList = new ArrayList<>();
        for(Object s : headers)
        {
            headersList.add((String) s);
        }
        c.setAllNodesWServices(allnodesNFV);
        c.setHeaders(headersList);
        c.setFilepath(filepath);
        c.setMaxNodes(maxNodes);
        c.setMaxNodesWServices(nodesNFV);
        c.setNodesWAllServices(nodesWALLNFV);
        c.setRequestnumber(requestNumber);

        return c;
    }
}
