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
    private List<Integer> nodesWServices;
    private List<Integer> servicesID;
    private boolean allNodesWServices;
    private boolean nodesWAllServices;
    private int numberOfNodesWServices;
    private int numberOfNodes;
    private int reqsToGen;
    private ArrayList<RequestConfig> configs;
    private RandomNumGen random;
    private Helper helpers;

    public ConfigGen()
    {
        this.filepath = "pedidos.csv";
        this.headers = new ArrayList<>();
        this.nodesWServices = new ArrayList<>();
        this.servicesID = new ArrayList<>();
        this.allNodesWServices = false;
        this.nodesWAllServices = false;
        this.numberOfNodesWServices = 0;
        this.numberOfNodes = 0;
        this.reqsToGen = 0;
        this.configs = new ArrayList<>();
        this.random = new RandomNumGen();
        this.helpers = new Helper();
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
            System.out.println("Insert the number of nodes with services:\n");
            this.numberOfNodesWServices = scan.nextInt();
        }
        System.out.println("Insert the number of services to generate\n");
        int servicesNum = scan.nextInt();
        setNumberOfServices(servicesNum);

        System.out.println("Do you pretend to have NFV nodes with all NFV services available? [Y/N]\n");
        String inputB = scan.next("[a-zA-Z]");
        if(inputB.equals("y") || inputB.equals("Y"))
            this.nodesWAllServices = true;
        else
            this.nodesWAllServices = false;

        System.out.println("Please insert the number of nodes of the topology:\n");
        this.numberOfNodes = scan.nextInt();
        if(this.allNodesWServices == true)
        {
            this.numberOfNodesWServices = this.numberOfNodes;
        }
        System.out.println("Please insert the number of requests to generate:\n");
        this.reqsToGen = scan.nextInt();
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
        if(this.allNodesWServices)
        {
            for(int i = 0; i < numberOfNodes; i++)
            {
                nodesWServices.add(i);
            }
        }
        else
        {
            setNodesWServices(random.genNodesWServices(this.numberOfNodes,this.numberOfNodesWServices));
        }

        if(this.headers.size() == 0){
            data.add(genHeaders(false));
        }
        else
        {
            data.add(genHeaders(true));
        }

        int rows = 0;
        while(rows < reqsToGen)
        {
            String[] row = genRow(rows);
            data.add(row);
            rows++;
        }
        return data;
    }

    private String[] genRow(int row)
    {
        RequestConfig c = new RequestConfig();
        c.setId(row);
        c.setOriginNodeID(random.getRandomFromRage(0, numberOfNodes));
        c.setDestinationNodeID(random.getRandomFromRage(0, numberOfNodes));
        c.setBandwidthConsumption(1 +random.randomDouble(9));
        c.insertRandomServices(random.getRandomFromRage(1, this.getNumberOfServices()+1),servicesID);

        return helpers.convertListToString(c, this.headers.size());
    }

    /**
     * Method responsible for generate the headers for the csv file
     * the configurationAvailable Boolean will determinate if the file config.json is available
     * if so all the configuration will be loaded from it
     * @param configurationAvailable
     * @return
     */
    private String[] genHeaders(boolean configurationAvailable)
    {
        if(!configurationAvailable)
        {
            List<String> headers = new ArrayList<>();
            headers.add("id");
            headers.add("originNodeID");
            headers.add("destinationNodeID");
            headers.add("bandwidth");
            int i = 0;
            while(i < this.getNumberOfServices())
            {
                headers.add(String.valueOf(i));
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
        this.numberOfNodes = c.getNumberOfNodes();
        this.numberOfNodesWServices = c.getNumberOfNodesWServices();
        this.allNodesWServices = c.isAllNodesWServices();
        this.reqsToGen = c.getReqsToGen();
        this.nodesWAllServices = c.isNodesWAllServices();
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

    public ArrayList<RequestConfig> getConfigs() {
        return configs;
    }

    public void setConfigs(ArrayList<RequestConfig> configs) {
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

    public int getNumberOfNodesWServices() {
        return numberOfNodesWServices;
    }

    public void setNumberOfNodesWServices(int numberOfNodesWServices) {
        this.numberOfNodesWServices = numberOfNodesWServices;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public int getReqsToGen() {
        return reqsToGen;
    }

    public void setReqsToGen(int reqsToGen) {
        this.reqsToGen = reqsToGen;
    }

    public boolean isNodesWAllServices() {
        return nodesWAllServices;
    }

    public void setNodesWAllServices(boolean nodesWAllServices) {
        this.nodesWAllServices = nodesWAllServices;
    }

    public List<Integer> getNodesWServices() {
        return nodesWServices;
    }

    public void setNodesWServices(List<Integer> nodesWServices) {
        this.nodesWServices = nodesWServices;
    }

    public int getNumberOfServices() {
        return this.servicesID.size();
    }

    public void setNumberOfServices(int numberOfServices) {
        for(int i = 0; i < numberOfServices; i++)
        {
            this.servicesID.add(i);
        }
    }

    public List<Integer> getServicesID() {
        return servicesID;
    }

    public void setServicesID(List<Integer> servicesID) {
        this.servicesID = servicesID;
    }
}
