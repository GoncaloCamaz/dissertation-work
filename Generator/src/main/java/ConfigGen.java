import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConfigGen
{
    private String filepath;
    private List<String> headers;
    private ArrayList<Configuration> configs;
    private RandomNumGen random;
    private Helper helpers;
    private boolean allNodesWServices;
    private boolean nodesWAllServices;
    private int maxNodesWServices;
    private int maxNodes;
    private int requestnumber;

    public ConfigGen()
    {
        this.filepath = "./pedidos.csv";
        this.headers = new ArrayList<String>();
        this.random = new RandomNumGen();
        this.helpers = new Helper();
        this.allNodesWServices = false;
        this.nodesWAllServices = false;
        this.maxNodesWServices = 0;
        this.maxNodes = 0;
        this.requestnumber = 0;
        this.configs = new ArrayList<Configuration>();
    }

    /**
     * IO interaction
     */
    public void startConfiguration()
    {
        Scanner scan = new Scanner(System.in);
        System.out.println("Do you pretend to have all the nodes with NFV services? [Y/N]\n");
        String input = scan.next("[a-zA-Z]");
        if(input.equals("y") || input.equals("Y"))
        {
            this.allNodesWServices = true;
        }
        else
        {
            System.out.println("Insert the maximum number of nodes with services:\n");
            this.maxNodesWServices = scan.nextInt();
        }
        System.out.println("Do you pretend to have NFV nodes with all NFV services available? [Y/N]\n");
        String inputA = scan.next("[a-zA-Z]");
        if(inputA.equals("y") || input.equals("Y"))
            this.nodesWAllServices = true;
        else
            this.nodesWAllServices = false;

        System.out.println("Please insert the number of nodes of the topology:\n");
        this.maxNodes = scan.nextInt();
        if(this.allNodesWServices == true)
        {
            this.maxNodesWServices = this.maxNodes;
        }
        System.out.println("Please insert the number of requests to generate:\n");
        this.requestnumber = scan.nextInt();
        generateSolutions();
    }

    /**
     * This method saves the configuration done by IO to future loadings
     */
    private void saveConfigurations()
    {
        JSONSaver saver = new JSONSaver();
        saver.saveConfigurations(this);
        saver.saveFrameworkConfigurations(this, random);
    }

    /**
     * This method will write in csv the generated data
     */
    public void generateSolutions() {
        File file = new File(filepath);
        try
        {
            FileWriter outputfile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputfile, ';',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            List<String[]> data = new ArrayList<>();
            data = genData();
            writer.writeAll(data);
            writer.close();
            Scanner scan = new Scanner(System.in);
            String input;
            System.out.println("Do you wish to save this configurations for further work? [Y/N]\n");
            input = scan.next("[a-zA-Z]");
            if(input.equals("y") || input.equals("Y"))
            {
                saveConfigurations();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String[]> genData()
    {
        List<String[]> data = new ArrayList<>();
        List<Integer> nodesWServices = new ArrayList<>();
        if(this.allNodesWServices)
        {
            for(int i = 0; i < maxNodes; i++)
            {
                nodesWServices.add(i+1);
            }
        }
        else
        {
            nodesWServices = random.genNodesWServices(this.maxNodes,this.maxNodesWServices);
        }

        if(this.headers.size() == 0){
            data.add(genHeaders(nodesWServices, this.maxNodesWServices, false));
        }
        else
        {
            data.add(genHeaders(nodesWServices, this.maxNodesWServices, true));
        }

        int rows = 0;
        while(rows < requestnumber)
        {
            String[] row = genRow(rows);
            data.add(row);
            rows++;
        }
        return data;
    }

    private String[] genRow(int row)
    {
        Configuration c = new Configuration();
        c.setId(row + 1);
        int originNode = random.getRandomFromRage(1,maxNodes);
        int destinationNode = random.getRandomFromRage(1,maxNodes);
        c.setOriginNodeID(originNode);
        c.setDestinationNodeID(destinationNode);
        c.setBandwidthConsumption(random.getRandomFromRage(1,1000));
        c.insertRandomServices(maxNodesWServices,random.getRandomFromRage(1,maxNodesWServices));

        return helpers.convertListToString(c, this.headers.size());
    }

    /**
     * Method responsible for generate the headers for the csv file
     * the configurationAvailable Boolean will determinate if the file config.json is available
     * if so all the configuration will be loaded from it
     * @param nodesWithServices
     * @param maxNodesWServices
     * @param configurationAvailable
     * @return
     */
    private String[] genHeaders(List<Integer> nodesWithServices, int maxNodesWServices, boolean configurationAvailable)
    {
        if(!configurationAvailable)
        {
            List<String> headers = new ArrayList<>();
            headers.add("id");
            headers.add("originNodeID");
            headers.add("destinationNodeID");
            headers.add("bandwidth");
            int i = 0;
            while(i < maxNodesWServices)
            {
                headers.add(String.valueOf(nodesWithServices.get(i)));
                i++;
            }
            this.setHeaders(headers);
            return helpers.genHeaders(headers);
        }
        else
        {
            return helpers.genHeaders(headers);
        }
    }

    public void genRequests(ConfigGen c)
    {
        this.headers = c.getHeaders();
        this.filepath = c.getFilepath();
        this.maxNodes = c.getMaxNodes();
        this.maxNodesWServices = c.getMaxNodesWServices();
        this.allNodesWServices = c.isAllNodesWServices();
        this.requestnumber = c.getRequestnumber();
        generateSolutions();
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public ArrayList<Configuration> getConfigs() {
        return configs;
    }

    public void setConfigs(ArrayList<Configuration> configs) {
        this.configs = configs;
    }

    public RandomNumGen getRandom() {
        return random;
    }

    public void setRandom(RandomNumGen random) {
        this.random = random;
    }

    public Helper getHelpers() {
        return helpers;
    }

    public void setHelpers(Helper helpers) {
        this.helpers = helpers;
    }

    public boolean isAllNodesWServices() {
        return allNodesWServices;
    }

    public void setAllNodesWServices(boolean allNodesWServices) {
        this.allNodesWServices = allNodesWServices;
    }

    public int getMaxNodesWServices() {
        return maxNodesWServices;
    }

    public void setMaxNodesWServices(int maxNodesWServices) {
        this.maxNodesWServices = maxNodesWServices;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

    public void setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
    }

    public int getRequestnumber() {
        return requestnumber;
    }

    public void setRequestnumber(int requestnumber) {
        this.requestnumber = requestnumber;
    }

    public boolean isNodesWAllServices() {
        return nodesWAllServices;
    }

    public void setNodesWAllServices(boolean nodesWAllServices) {
        this.nodesWAllServices = nodesWAllServices;
    }
}
