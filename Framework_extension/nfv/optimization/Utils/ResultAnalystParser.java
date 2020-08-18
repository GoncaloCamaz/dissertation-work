package pt.uminho.algoritmi.netopt.nfv.optimization.Utils;

import com.opencsv.CSVWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResultAnalystParser
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

    public static ResultAnalystParserObject loadResults(String filename) throws IOException, ParseException {
        JSONObject obj = loadObject(filename);
        JSONArray nodes = (JSONArray) obj.get("NodesLoad");
        double objectiveFunction = Double.parseDouble(String.valueOf(obj.get("objectiveFunction")));
        int numberOfNodes = 0;
        double totalNodesLoad = 0;

        for(Object o : nodes)
        {
            JSONObject nodeInfo = (JSONObject) o;
            double nodeLoad = Double.parseDouble(String.valueOf(nodeInfo.get("Load")));
            double normalizedLoad =nodeLoad;
            if(nodeLoad > 0)
                numberOfNodes++;

            totalNodesLoad += normalizedLoad;
        }
        totalNodesLoad = totalNodesLoad/numberOfNodes;

        JSONArray links = (JSONArray) obj.get("Arc Loads");
        int numberOfLinks = 0;
        double totalLinksLoad = 0;
        for(Object oL : links)
        {
            JSONObject linkInfo = (JSONObject) oL;
            double linkLoad = Double.parseDouble(String.valueOf(linkInfo.get("Load")));
            double normalizedLinkLoad = linkLoad;
            if(linkLoad > 0)
                numberOfLinks++;

            totalLinksLoad += normalizedLinkLoad;
        }
        totalLinksLoad = totalLinksLoad/numberOfLinks;

        int numberOfServicesDeployed = 0;
        JSONArray serviceDeployment = (JSONArray) obj.get("servicesLocationSolution");
        for(Object oServices : serviceDeployment)
        {
            JSONObject availServ = (JSONObject) oServices;
            JSONArray deployed = (JSONArray) availServ.get("AvailableServices");
            numberOfServicesDeployed += deployed.size();
        }

        ResultAnalystParserObject results = new ResultAnalystParserObject(totalLinksLoad, totalNodesLoad, numberOfServicesDeployed,objectiveFunction);

        return results;
    }

    public static void saveToCSV(List<ResultAnalystParserObject> list, String filename)
    {
        File file = new File(filename);
        FileWriter outputfile = null;
        try {
            outputfile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputfile, ';',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            List<String[]> data = new ArrayList<>();
            String[] headers = new String[4];
            headers[0] = "linksLoad";
            headers[1] = "nodesLoad";
            headers[2] = "numberOfServices";
            headers[3] = "objectiveFunction";

            data.add(headers);
            for(ResultAnalystParserObject res : list)
            {
                String[] row = new String[4];
                row[0] = String.valueOf(res.getLinkUtil());
                row[1] = String.valueOf(res.getNodeUtil());
                row[2] = String.valueOf(res.getNumberOfServices());
                row[3] = String.valueOf(res.getObjectiveFunction());
                data.add(row);
            }

            writer.writeAll(data);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
