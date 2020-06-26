package pt.uminho.algoritmi.netopt.nfv.optimization.Utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pt.uminho.algoritmi.netopt.nfv.NFNode;
import pt.uminho.algoritmi.netopt.nfv.NFNodesMap;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Number of services = 3
0 -> no service
1 -> Service ID 1
2 -> Service ID 2
3 -> Service ID 3
4 -> Service ID 1,2
5 -> Service ID 1,3
6 -> Service ID 2,3
7 -> Service ID 1,2,3
 **/

public class EASolutionParser
{
    private Map<Integer, List<Integer>> serv;
    private String filename;

    public EASolutionParser(String filename)
    {
        this.serv = new HashMap<>();
        this.filename = filename;
        loadJson();
    }

    /**
     * Loads the Codification File
     */
    private void loadJson() {
        HashMap<Integer, List<Integer>> servicesNode = new HashMap<>();
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader("serviceMap.json"));
            JSONObject jsonObj = (JSONObject) obj;
            JSONArray services = (JSONArray) jsonObj.get("services");
            for(Object o : services)
            {
                JSONObject objAux = (JSONObject) o;
                int idS = Integer.parseInt(objAux.get("id").toString());
                JSONArray idList = (JSONArray) objAux.get("List");
                List<Integer> servs = new ArrayList<>();
                for(Object o1 : idList)
                {
                    servs.add(Integer.parseInt(o1.toString()));
                }
                servicesNode.put(idS, servs);
            }
            setServ(servicesNode);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Transforms the result of the EA into NFNodesMap
     * @param results
     * @return
     */
    public NFNodesMap solutionParser(int[] results)
    {
        NFNodesMap nodesMap = new NFNodesMap();
        Map<Integer, NFNode> nodes = new HashMap<>();
        int processCapacity = 0;
        for(int i = 0; i < results.length; i++)
        {
            List<Integer> availableSerices = new ArrayList<>();
            availableSerices = serv.get(results[i]);

            if(availableSerices.size() == 0)
            {
                processCapacity = 0;
            }
            else
            {
                processCapacity = 2500;
            }
            NFNode node = new NFNode(i,processCapacity, availableSerices);
            nodes.put(i, node);
        }
        nodesMap.setNodes(nodes);

        return nodesMap;
    }

    public int[] solutionFromConfiguration(NFNodesMap map)
    {
        Map<Integer, NFNode> nodes = map.getNodes();
        int[] res = new int[nodes.size()];
        for(int i = 0; i < nodes.size(); i++)
        {
            List<Integer> avail = nodes.get(i).getAvailableServices();
            int index = getListServID(avail);
            res[i] = index;
        }

        return res;
    }

    private int getListServID(List<Integer> avail)
    {
        int ret = 0; int i = 0;

        for(List<Integer> list : serv.values())
        {
            if(list.equals(avail))
            {
                ret = i;
            }
            i++;
        }
        return ret;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Map<Integer, List<Integer>> getServ() {
        return serv;
    }

    public void setServ(Map<Integer, List<Integer>> serv) {
        this.serv = serv;
    }
}
